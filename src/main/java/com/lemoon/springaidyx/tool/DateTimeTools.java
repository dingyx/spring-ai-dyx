package com.lemoon.springaidyx.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTools {

    @Tool(description = "获取用户时区内的当前日期和时间")
    String getCurrentDateTime() {
        // 获取当前时间并转换为用户时区的时间
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "为给定的时间（ISO-8601 格式）设置用户闹钟")
    void setAlarm(@ToolParam(description = "ISO-8601 格式的时间字符串") String time) {
        // 解析传入的时间字符串为 LocalDateTime 对象
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        // 输出设置的闹钟时间
        System.out.println("闹钟设置为 " + alarmTime);
    }

}