package com.kkambbak.client.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Configuration
public class KakaoPayConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("KakaoPay RestTemplate initialized");
        return builder.build();
    }
}