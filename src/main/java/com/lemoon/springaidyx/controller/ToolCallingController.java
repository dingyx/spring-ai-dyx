package com.lemoon.springaidyx.controller;

import com.lemoon.springaidyx.tool.DateTimeTools;
import com.lemoon.springaidyx.tool.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/tool")
public class ToolCallingController {

    @Autowired
    private ChatModel chatModel;

    @RequestMapping("/time")
    public String timeCall(@RequestParam(value = "message", defaultValue = "明天是几号?") String message) {

        ChatClient chatClient = ChatClient.create(chatModel);
        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(message)
                .tools(new DateTimeTools())
                .call();

        return responseSpec.content();
    }

    @RequestMapping("/weather")
    public String weatherCall(@RequestParam(value = "message", defaultValue = "北京明天的天气怎么样?") String message) {

        ChatClient chatClient = ChatClient.create(chatModel);
        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(message)
                .tools(new WeatherTools())
                .call();

        return responseSpec.content();
    }

}
