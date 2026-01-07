package com.example.demo.llm;

public interface LlmRequestable {

    boolean isTextMessage();
    boolean isVoiceMessage();
    boolean isImageIdMessage();
    boolean isImageLinkMessage();

    String getMessageText();
    String getMessageVoiceTranlated();
    String getMessageImageId();
    String getMessageImageLink();
    String getImageBackupUrl();

    boolean isFromUser();
    boolean isFromRobot();
    boolean isFromTool();

}
