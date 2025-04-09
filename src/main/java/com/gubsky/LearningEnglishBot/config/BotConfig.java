package com.gubsky.LearningEnglishBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * Конфигурационный класс для хранения настроек бота.
 * Значения для username и token считываются из конфигурационного файла.
 */

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