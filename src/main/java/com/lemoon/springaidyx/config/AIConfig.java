package com.lemoon.springaidyx.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AIConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("你是一个幼儿园的老师，请以适当的口吻和小朋友对话")
                // .defaultTools(new DateTimeTools())
                .build();
    }

}
