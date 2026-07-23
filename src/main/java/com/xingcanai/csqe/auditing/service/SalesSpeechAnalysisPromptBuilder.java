package com.xingcanai.csqe.auditing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingcanai.csqe.auditing.config.SalesSpeechAnalysisProperties;
import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformance;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SalesSpeechAnalysisPromptBuilder {

    private static final DateTimeFormatter MESSAGE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final int MIN_TEMPLATE_CODE_POINTS = 100;
    private static final int MIN_TEMPLATE_OCCURRENCES = 6;

    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final ZoneId zoneId;

    @Autowired
    public SalesSpeechAnalysisPromptBuilder(
            ResourceLoader resourceLoader,
            SalesSpeechAnalysisProperties properties) {
        this(
                new ObjectMapper(),
                readPrompt(resourceLoader.getResource(properties.getPromptResource())),
                ZoneId.of(properties.getZoneId()));
    }

    SalesSpeechAnalysisPromptBuilder(ObjectMapper objectMapper, String systemPrompt) {
        this(objectMapper, systemPrompt, ZoneId.of("Asia/Shanghai"));
    }

    SalesSpeechAnalysisPromptBuilder(ObjectMapper objectMapper, String systemPrompt, ZoneId zoneId) {
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
        this.zoneId = zoneId;
    }

    public SalesSpeechAnalysisPrompt build(
            String employeeQwId,
            LocalDate startDate,
            LocalDate endDate,
            List<WxChatMessage> messages,
            List<CampCustomerDailyPerformance> performances) {

        CustomerAliases aliases = new CustomerAliases();
        List<PreparedMessage> preparedMessages = new ArrayList<>(messages.size());
        Map<RepeatedMessageKey, Integer> repeatedMessages = new LinkedHashMap<>();
        for (WxChatMessage message : messages) {
            boolean sentBySales = employeeQwId.equals(message.getFromId());
            String realCustomerId = sentBySales ? message.getAcceptId() : message.getFromId();
            String customerAlias = aliases.aliasFor(realCustomerId, "chat:" + message.getMsgId());
            PreparedMessage preparedMessage =
                    new PreparedMessage(message, sentBySales, customerAlias);
            preparedMessages.add(preparedMessage);

            if (isTemplateCandidate(preparedMessage)) {
                RepeatedMessageKey key =
                        new RepeatedMessageKey(message.getMsgType(), message.getContent());
                repeatedMessages.merge(key, 1, Integer::sum);
            }
        }

        Map<RepeatedMessageKey, String> templateIds = new LinkedHashMap<>();
        StringBuilder repeatedMessageTemplates = new StringBuilder();
        for (Map.Entry<RepeatedMessageKey, Integer> entry : repeatedMessages.entrySet()) {
            if (entry.getValue() < MIN_TEMPLATE_OCCURRENCES) {
                continue;
            }

            String templateId = "T%03d".formatted(templateIds.size() + 1);
            templateIds.put(entry.getKey(), templateId);

            Map<String, Object> template = new LinkedHashMap<>();
            template.put("template_id", templateId);
            template.put("type", entry.getKey().type());
            template.put("content", entry.getKey().content());
            appendJsonLine(repeatedMessageTemplates, template);
        }

        StringBuilder chatRecords = new StringBuilder();
        for (PreparedMessage preparedMessage : preparedMessages) {
            WxChatMessage message = preparedMessage.message();
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("customer", preparedMessage.customerAlias());
            record.put("time", formatTime(message.getMsgTime()));
            record.put("speaker", preparedMessage.sentBySales() ? "销售" : "客户");
            record.put("type", message.getMsgType());
            String templateId = preparedMessage.sentBySales()
                    ? templateIds.get(new RepeatedMessageKey(
                            message.getMsgType(),
                            message.getContent()))
                    : null;
            if (templateId == null) {
                record.put("content", message.getContent());
            } else {
                record.put("content_ref", templateId);
            }
            appendJsonLine(chatRecords, record);
        }

        StringBuilder performanceRecords = new StringBuilder();
        for (CampCustomerDailyPerformance performance : performances) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("customer", aliases.aliasFor(
                    performance.getExternalUserid(),
                    "performance:" + performance.getId()));
            record.put("stat_date", formatDate(performance.getStatDate()));
            record.put("camp_tag", performance.getCampTag());
            record.put("gmv_amount", amount(performance.getGmvAmount()));
            record.put("refund_amount", amount(performance.getRefundAmount()));
            appendJsonLine(performanceRecords, record);
        }

        String userPrompt = """
            请分析以下同一名销售在指定自然日范围内的全部原始记录。

            分析日期（含首尾）：%s 至 %s
            销售标签：被分析销售

            <repeated_message_templates>
            %s</repeated_message_templates>

            <chat_records>
            %s</chat_records>

            <performance_records>
            %s</performance_records>

            现在开始分析。直接输出完整中文 Markdown 报告，不要重复任务说明。
            """.formatted(
                startDate,
                endDate,
                repeatedMessageTemplates,
                chatRecords,
                performanceRecords);

        return new SalesSpeechAnalysisPrompt(systemPrompt, userPrompt);
    }

    private void appendJsonLine(StringBuilder target, Map<String, Object> record) {
        try {
            target.append(objectMapper.writeValueAsString(record)).append('\n');
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize sales speech analysis input", exception);
        }
    }

    private static String readPrompt(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load sales speech analysis prompt resource", exception);
        }
    }

    private String formatTime(ZonedDateTime value) {
        return value == null
                ? null
                : MESSAGE_TIME_FORMAT.format(value.withZoneSameInstant(zoneId));
    }

    private static String formatDate(LocalDate value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal amount(BigDecimal value) {
        return value;
    }

    private static boolean isTemplateCandidate(PreparedMessage message) {
        String content = message.message().getContent();
        return message.sentBySales()
                && content != null
                && content.codePointCount(0, content.length()) >= MIN_TEMPLATE_CODE_POINTS;
    }

    private record PreparedMessage(
            WxChatMessage message,
            boolean sentBySales,
            String customerAlias) {
    }

    private record RepeatedMessageKey(String type, String content) {
    }

    private static final class CustomerAliases {
        private final Map<String, String> aliases = new LinkedHashMap<>();

        private String aliasFor(String realCustomerId, String fallbackKey) {
            String key = realCustomerId == null || realCustomerId.isBlank()
                    ? fallbackKey
                    : "customer:" + realCustomerId;
            return aliases.computeIfAbsent(key, ignored -> "C%03d".formatted(aliases.size() + 1));
        }
    }
}
