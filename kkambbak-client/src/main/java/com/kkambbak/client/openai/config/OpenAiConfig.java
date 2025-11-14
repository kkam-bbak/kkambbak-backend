package com.kkambbak.client.openai.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class OpenAiConfig {
    @Value("${openai.api.key}")
    private String openAiKey;

    @Value("${openai.timeout.connect-ms:5000}")
    private int connectTimeoutMs;

    @Value("${openai.timeout.read-s:60}")
    private int readTimeoutS;

    @Value("${openai.timeout.write-s:10}")
    private int writeTimeoutS;

    @Value("${openai.api.base-url}")
    private String baseUrl;

    @Bean("openAiWebClient")
    public WebClient openAiWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofSeconds(readTimeoutS))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeoutS, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeoutS, TimeUnit.SECONDS))
                );

        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + openAiKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
