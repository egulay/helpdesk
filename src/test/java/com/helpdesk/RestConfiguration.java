package com.helpdesk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestConfiguration {
    public static final String LOCALHOST = "http://localhost:";

    @Bean
    RestTemplate restTemplate(ProtobufHttpMessageConverter hmc) {
        return new RestTemplate(Collections.singletonList(hmc));
    }
}
