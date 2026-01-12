package com.xingcanai.csqe.auditing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.ChatPoll;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.client.connversations.message.model.MessageType;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import com.xingcanai.csqe.llm.supports.coze.CozeConfigProperties;


@Component
public class Coze {

    @Autowired CozeConfigProperties cozeConfigProperties;

    public static void main(String[] args) throws Exception {
        Coze coze = new Coze();
        var response = coze.chat("7589909383191543817", "2025-11-18 15:35:23+08 | 客服: 真是费家长\n2025-11-18 16:20:36+08 | 家长: 沙沙老师是清华的老师？？\n2025-11-18 16:20:48+08 | 客服: 清华毕业的\n2025-11-18 16:21:01+08 | 家长: 哦\n");
        System.out.println(response);
    }

    public String chat(String botId, String text)  {
        // System.out.println("Coze Chat: " + botId + " : " + text);

        // String token = cozeConfigProperties.getApikey();
        String token = "sat_lHVT8MBuo0hC71uO5r6BtuW8zbJES1gmWUprWX4Lfq4Qli6fEpThfmrHSAOWXSWT";
        String uid = "some user id";

        TokenAuth authCli = new TokenAuth(token);

        // Init the Coze client through the access_token.
        CozeAPI coze = new CozeAPI.Builder()
                .baseURL("https://api.coze.cn")
                .auth(authCli)
                .readTimeout(10000)
                .build();

        // var bots = coze.bots().list(ListBotReq.builder().spaceID("7531602532226433051").build());
        // System.out.println(bots);

        CreateChatReq req = CreateChatReq.builder()
                .botID(botId)
                .userID(uid)
                .messages(List.of(Message.buildUserQuestionText(text)))
                .build();

        try {
            ChatPoll chat2 = coze.chat().createAndPoll(req);
            var msgs = chat2.getMessages();
            return msgs.stream()
                .filter(message -> message.getType() == MessageType.ANSWER)
                .map(message -> message.getContent())
                .findFirst()
                .orElse(null);
                // .forEach(message -> {
                //     System.out.println(message.getRole() + ":" + message.getType().getValue());
                //     System.out.println(message.getContent());
                // });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
