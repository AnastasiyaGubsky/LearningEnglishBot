package com.gubsky.LearningEnglishBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}