package com.lemoon.springaidyx.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WeatherTools {

    @Tool(description = "查询某个城市某一天的天气")
    String weather(@ToolParam(description = "城市信息") String city, @ToolParam(description = "ISO-8601 格式的时间字符串") String time) {
        System.out.println("查询 " + city + " " + time + " 的天气");
        return "多云转晴 25摄氏度";
    }

}
