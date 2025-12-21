package com.example.demo.common;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ChatContexts {

    public static String dateContext() {
        StringBuilder dateContext = new StringBuilder();
        dateContext
                .append("现在的日期和时间是: ")
                .append(ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)))
                .append(". 如果用户的问题跟时间相关，使用这个日期作为当前日期来回答问题。");
        return dateContext.toString();
    }

}
