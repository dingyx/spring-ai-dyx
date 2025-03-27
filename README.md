# Spring AI Project

核心功能实现

### 1. 智能对话接口
```java
@GetMapping("/chat")
String chat(String message) {
    return chatClient.prompt()
            .user(message)
            .call()
            .content();
}

// 流式响应
@GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
Flux<String> stream(String message) {
    return chatClient.prompt()
            .user(message)
            .stream()
            .content();
}
```

### 2. 文生图功能
```java
@GetMapping("/text2Img")
String text2Img(@RequestParam String message) {
    ImageResponse response = openAiImageModel.call(
        new ImagePrompt(message, 
            OpenAiImageOptions.builder()
                .quality("hd")
                .width(1024)
                .height(1024)
                .build())
    );
    return response.getResult().getUrl();
}
```

### 3. 语音互转功能
**文字转语音：**
```java
@GetMapping("/text2Audio")
public String text2Audio(String message) {
    SpeechResponse response = openAiAudioSpeechModel.call(
        new SpeechPrompt(message, 
            OpenAiAudioSpeechOptions.builder()
                .voice(ALLOY)
                .build())
    );
    // 保存音频文件逻辑...
    return "生成成功";
}
```

**语音转文字：**
```java
@GetMapping("/audio2Text")
public String audio2Text() {
    AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(
        new AudioTranscriptionPrompt(
            new ClassPathResource("audio.mp3"),
            transcriptionOptions)
    );
    return response.getResult().getOutput();
}
```

### 4. 多模态交互
```java
@GetMapping("/multi")
public String multiModel(String message) {
    var userMessage = new UserMessage(message, 
        List.of(new Media(IMAGE_JPEG, new ClassPathResource("image.jpg")))
    );
    
    ChatResponse response = chatModel.call(
        new Prompt(userMessage, 
            OpenAiChatOptions.builder()
                .model(GPT_4_O)
                .build())
    );
    return response.getResult().getText();
}
```

## 五、自定义函数调用

### 1. 时间工具类
```java
public class DateTimeTools {
    @Tool(description = "获取当前日期时间")
    String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}
```

### 2. 天气查询工具
```java
public class WeatherTools {
    @Tool(description = "查询城市天气")
    String weather(String city, String time) {
        return "多云 25℃"; // 实际接入天气API
    }
}
```

### 3. 控制器调用
```java
@GetMapping("/time")
public String timeCall(String message) {
    return chatClient.prompt(message)
            .tools(new DateTimeTools())
            .call()
            .content();
}
```

**项目资源**：
- [官方文档](https://spring.io/projects/spring-ai)
