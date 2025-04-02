package com.gubsky.LearningEnglishBot.bot;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private final Bot bot;

    public BotInitializer(Bot bot) {
        this.bot = bot;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            return botsApi;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка регистрации бота", e);
        }
    }
}