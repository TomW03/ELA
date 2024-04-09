package com.elasticsearch.application.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

@Configuration
public class AppConfig {

    @Bean
    public Logger logger() {
        return Logger.getLogger("ElasticLogger");
    }
}
