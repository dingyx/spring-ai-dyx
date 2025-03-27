package com.lemoon.springaidyx.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AIController {

//    private final ChatClient chatClient;
//
//    public AIController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatModel chatModel;

    @GetMapping("/chat")
    String chat(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        // prompt 提示词
        return this.chatClient.prompt()
                // 用户信息
                .user(message)
                // 请求大模型
                .call()
                // 返回文本
                .content();
    }


    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    Flux<String> stream(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        // prompt 提示词
        Flux<String> output = chatClient.prompt()
                .user(message)
                //.stream()
                .stream()
                .content();
        return output;
    }


    @GetMapping("/chat/model")
    String chatModel(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        // prompt 提示词
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        OpenAiChatOptions.builder()
                                .model("gpt-4o")
                                .temperature(0.4)
                                .build()
                ));
        return response.getResult().getOutput().getText();
    }





}
