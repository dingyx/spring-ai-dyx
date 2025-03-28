package com.lemoon.springaidyx.controller;

// 导入所需的工具类和库
import com.lemoon.springaidyx.tool.DateTimeTools;
import com.lemoon.springaidyx.tool.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/tool") // 定义基础请求路径
public class ToolCallingController {

    @Autowired
    private ChatModel chatModel; // 自动注入ChatModel对象

    @RequestMapping("/time") // 定义处理时间查询的请求路径
    public String timeCall(@RequestParam(value = "message", defaultValue = "明天是几号?") String message) {
        // 创建ChatClient对象
        ChatClient chatClient = ChatClient.create(chatModel);
        // 使用DateTimeTools工具处理请求
        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(message)
                .tools(new DateTimeTools())
                .call();

        return responseSpec.content(); // 返回响应内容
    }

    @RequestMapping("/weather") // 定义处理天气查询的请求路径
    public String weatherCall(@RequestParam(value = "message", defaultValue = "北京明天的天气怎么样?") String message) {
        // 创建ChatClient对象
        ChatClient chatClient = ChatClient.create(chatModel);
        // 使用WeatherTools工具处理请求
        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(message)
                .tools(new WeatherTools())
                .call();

        return responseSpec.content(); // 返回响应内容
    }

}
