package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.EvaluationDetail;
import com.xingcanai.csqe.auditing.entity.EvaluationDetailRepository;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

/**
 * 评估明细相关接口
 */
@RestController
@RequestMapping("/evaluation-details")
public class EvaluationDetailController {

    @Autowired
    private EvaluationDetailRepository evaluationDetailRepository;

    @Autowired
    private WxChatMessageRepository wxChatMessageRepository;

    /**
     * 根据评估明细ID查询对应的聊天记录
     *
     * @param id 评估明细ID
     */
    @GetMapping("/{id}/chat-messages")
    public List<WxChatMessage> listChatMessagesByEvaluationDetailId(@PathVariable String id) {
        EvaluationDetail detail = evaluationDetailRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation detail not found"));

        if (detail.getChatStartTime() == null || detail.getChatEndTime() == null) {
            return Collections.emptyList();
        }

        return wxChatMessageRepository.findChatBetweenEmployeeAndCustomer(
                detail.getEmployeeQwId(),
                detail.getCustomerId(),
                detail.getChatStartTime(),
                detail.getChatEndTime()
        );
    }
}

