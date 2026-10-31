package com.kuaixietech.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/** Supports legacy services that use Jackson 2 while Boot 4 defaults to Jackson 3. */
@Configuration(proxyBeanMethods = false)
public class LegacyJackson2Config {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper legacyJacksonObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
