package com.helpdesk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.codec.protobuf.ProtobufDecoder;
import org.springframework.http.codec.protobuf.ProtobufEncoder;

@Configuration
public class RestConfiguration {
    public static final String LOCALHOST = "http://localhost:";

    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .codecs(cfg -> {
                    cfg.defaultCodecs().protobufDecoder(new ProtobufDecoder());
                    cfg.defaultCodecs().protobufEncoder(new ProtobufEncoder());
                }).build();
    }
}
