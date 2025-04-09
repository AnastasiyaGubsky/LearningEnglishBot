package com.gubsky.LearningEnglishBot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Класс, отвечающий за инициализацию и регистрацию бота в Telegram.
 * Использует TelegramBotsApi для регистрации бота и обработки ошибок.
 */

@Component
public class BotInitializer {

    private static final Logger logger = LoggerFactory.getLogger(BotInitializer.class);

    private final Bot bot;

    public BotInitializer(Bot bot) {
        this.bot = bot;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            logger.info("Bot registered successfully.");
            return botsApi;
        } catch (TelegramApiException e) {
            logger.error("Error registering bot", e);
            throw new RuntimeException("Ошибка регистрации бота", e);
        }
    }
}