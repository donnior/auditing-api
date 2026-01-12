package com.xingcanai.csqe.auditing.service;

import java.util.Arrays;
import java.util.Map;

import org.springframework.stereotype.Component;

import java.util.List;

import com.xingcanai.csqe.auditing.entity.EvaluationDetail;
import com.xingcanai.csqe.util.Maps;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SimpleResponseEvaluationParser {

    private interface DetailSetting {
        void set(EvaluationDetail e, String kw);
    }

    private static final String YES_TEXT = "是";

    private boolean asBoolean(String kw, List<String> lines) {
        return lines.stream()
                .filter(line -> line.contains(kw))
                .findFirst()
                .map(line -> line.split(kw))
                .map(line -> line.length > 1 ? line[1] : "")
                .map(text -> text.contains(YES_TEXT))
                .orElse(false);
    }

    public EvaluationDetail parseResponse(String response, String reportType) {
        log.info("Parse response: {}", response);
        var lines = Arrays.asList(response.split("\n"));

        Map<String, DetailSetting> part1 = Map.of(
            "完成资料发送", (e, kw) -> e.setHasMaterialSend(asBoolean(kw, lines) ? 1 : 0),
            "完成到课提醒", (e, kw) -> e.setHasCourseRemind(asBoolean(kw, lines) ? 1 : 0),
            "完成课后作业发布", (e, kw) -> e.setHasHomeworkPublish(asBoolean(kw, lines) ? 1 : 0),
            "下周资料发送", (e, kw) -> e.setHasWeekMaterialSend(asBoolean(kw, lines) ? 1 : 0),
            "完成课后学习感受追踪", (e, kw) -> e.setHasFeedbackTrack(asBoolean(kw, lines) ? 1 : 0),
            "周日螳螂销转链接", (e, kw) -> e.setHasSundayLinkSend(asBoolean(kw, lines) ? 1 : 0),
            "风险词触发", (e, kw) -> e.setHasRiskWordTrigger(asBoolean(kw, lines) ? 1 : 0)
        );

        Map<String, DetailSetting> part2 = Map.of(
            "完成看课指导", (e, kw) -> e.setHasIntroduceCourse(asBoolean(kw, lines) ? 1 : 0),
            "完成老师介绍", (e, kw) -> e.setHasIntroduceTeacher(asBoolean(kw, lines) ? 1 : 0),
            "完成课表介绍", (e, kw) -> e.setHasIntroduceSchedule(asBoolean(kw, lines) ? 1 : 0),
            "完成上课时间介绍", (e, kw) -> e.setHasIntroduceCourseTime(asBoolean(kw, lines) ? 1 : 0),
            "索要订单号并核对", (e, kw) -> e.setHasOrderCheck(asBoolean(kw, lines) ? 1 : 0)
        );

        var ed = new EvaluationDetail();
        Maps.merge(part1, part2).forEach((kw, setting) -> setting.set(ed, kw));
        return ed;
    }
}
