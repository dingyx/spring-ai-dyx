package com.lemoon.springaidyx.controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/ai")
public class AIController {

//    private final ChatClient chatClient;
//
//    public AIController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }

    // 注入 ChatClient
    @Autowired
    private ChatClient chatClient;

    // 注入 ChatModel
    @Autowired
    private ChatModel chatModel;

    // 注入 OpenAiImageModel
    @Autowired
    private OpenAiImageModel openAiImageModel;

    // 注入 OpenAiAudioSpeechModel
    @Autowired
    private OpenAiAudioSpeechModel openAiAudioSpeechModel;

    // 注入 OpenAiAudioTranscriptionModel
    @Autowired
    private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    @GetMapping("/chat")
    String chat(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        // 使用 ChatClient 进行聊天
        return this.chatClient.prompt()
                .user(message) // 用户输入信息
                .call() // 调用大模型
                .content(); // 返回文本内容
    }

    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    Flux<String> stream(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        // 使用 ChatClient 进行流式聊天
        Flux<String> output = chatClient.prompt().user(message)
                .stream().content(); // 流式返回内容
        return output;
    }

    @GetMapping("/chat/model")
    String chatModel(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        // 使用 ChatModel 进行聊天
        ChatResponse response = chatModel.call(new Prompt(message, OpenAiChatOptions.builder().model("gpt-4o").temperature(0.4).build()));
        return response.getResult().getOutput().getText(); // 返回聊天结果
    }

    @GetMapping("/text2Img")
    String text2Img(@RequestParam(value = "message", defaultValue = "画一只兔子") String message) {
        // 使用 OpenAiImageModel 生成图像
        ImageResponse response = openAiImageModel.call(
                new ImagePrompt(message,
                        OpenAiImageOptions.builder()
                                .quality("hd") // 图像质量
                                .withModel(OpenAiImageApi.DEFAULT_IMAGE_MODEL)
                                .N(1) // 图片数量
                                .height(1024) // 图片高度
                                .width(1024).build()) // 图片宽度
        );
        return response.getResult().getOutput().getUrl(); // 返回图像 URL
    }

    @GetMapping(value = "/text2Audio")
    public String text2Audio(@RequestParam(value = "message", defaultValue = "Hello 大家好") String message) {
        // 配置语音合成选项
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0f)
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(message, speechOptions);
        SpeechResponse response = openAiAudioSpeechModel.call(speechPrompt);
        byte[] output = response.getResult().getOutput();

        try {
            writeByteArrayToMP3File(output, System.getProperty("user.dir")); // 写入 MP3 文件
        } catch (IOException e) {
            e.printStackTrace(); // 打印异常信息
            return "音频文件生成失败: " + e.getMessage(); // 返回错误信息
        }

        return "音频文件生成成功"; // 返回成功信息
    }

    @GetMapping(value = "/audio2Text")
    public String audio2Text() {
        // 配置音频转文本选项
        var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .temperature(0f)
                .language("zh") // 语言设置为中文
                .build();

        var audioFile = new ClassPathResource("54723862_tts.mp3");

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(transcriptionRequest);

        return response.getResult().getOutput(); // 返回转录结果
    }

    @GetMapping(value = "/multi")
    public String multiModel(@RequestParam(value = "message", defaultValue = "你从图片中获取到了什么信息") String message) {
        // 使用多模型进行处理
        ClassPathResource imgResource = new ClassPathResource("avatar.jpg");

        var userMessage = new UserMessage(message,
                List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imgResource)));

        ChatResponse response = chatModel.call(new Prompt(userMessage,
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O)
                        .build()));

        return response.getResult().getOutput().getText(); // 返回处理结果
    }

    // 将 byte[] 写入 MP3 文件
    public static void writeByteArrayToMP3File(byte[] audioBytes, String outputFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(
                outputFilePath + FileSystems.getDefault().getSeparator() + new Random().nextInt(99999999) + "_tts.mp3")) {
            fos.write(audioBytes);
        }
    }
}
