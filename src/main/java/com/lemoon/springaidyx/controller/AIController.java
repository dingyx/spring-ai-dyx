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

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private OpenAiImageModel openAiImageModel;

    @Autowired
    private OpenAiAudioSpeechModel openAiAudioSpeechModel;

    @Autowired
    private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

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
        Flux<String> output = chatClient.prompt().user(message)
                //.stream()
                .stream().content();
        return output;
    }


    @GetMapping("/chat/model")
    String chatModel(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        // prompt 提示词
        ChatResponse response = chatModel.call(new Prompt(message, OpenAiChatOptions.builder().model("gpt-4o").temperature(0.4).build()));
        return response.getResult().getOutput().getText();
    }


    @GetMapping("/text2Img")
    String text2Img(@RequestParam(value = "message", defaultValue = "画一只兔子") String message) {
        ImageResponse response = openAiImageModel.call(
                new ImagePrompt(message,
                        OpenAiImageOptions.builder()
                                .quality("hd")
                                .withModel(OpenAiImageApi.DEFAULT_IMAGE_MODEL)
                                // 图片数量
                                .N(1)
                                .height(1024)
                                .width(1024).build())

        );
        return response.getResult().getOutput().getUrl();
    }


    @GetMapping(value = "/text2Audio")
    public String text2Audio(@RequestParam(value = "message", defaultValue = "Hello 大家好") String message) {
        // 配置项
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
            writeByteArrayToMP3File(output, System.getProperty("user.dir"));
        } catch (IOException e) {
            e.printStackTrace(); // 或记录日志
            return "音频文件生成失败: " + e.getMessage();
        }

        return "音频文件生成成功";
    }

    @GetMapping(value = "/audio2Text")
    public String audio2Text() {

        // 翻译配置
        var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .temperature(0f)
                .language("zh")
                .build();

        var audioFile = new ClassPathResource("54723862_tts.mp3");

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(transcriptionRequest);

        return response.getResult().getOutput();
    }


    @GetMapping(value = "/multi")
    public String multiModel(@RequestParam(value = "message", defaultValue = "你从图片中获取到了什么信息") String message) {

        ClassPathResource imgResource = new ClassPathResource("avatar.jpg");

        var userMessage =new UserMessage(message,
                List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imgResource)));

        ChatResponse response = chatModel.call(new Prompt(userMessage,
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O)
                        .build()));

        return response.getResult().getOutput().getText();
    }





        // byte[] 写入mp3文件
    public static void writeByteArrayToMP3File(byte[] audioBytes, String outputFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(
                outputFilePath + FileSystems.getDefault().getSeparator() + new Random().nextInt(99999999) + "_tts.mp3")) {
            fos.write(audioBytes);
        }
    }


}
