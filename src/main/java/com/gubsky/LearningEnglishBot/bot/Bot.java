package com.gubsky.LearningEnglishBot.bot;

import com.gubsky.LearningEnglishBot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    private final CommandHandler commandHandler;
    private final BotConfig botConfig;

    @Autowired
    public Bot(CommandHandler commandHandler, BotConfig botConfig) {
        this.commandHandler = commandHandler;
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Long chatId = update.getMessage().getChatId();
                String message = update.getMessage().getText();
                logger.info("Received message from {}: {}", chatId, message);
                String response = commandHandler.handleCommand(message, chatId);
                sendMessage(chatId, response);
            }
        } catch (Exception e) {
            logger.error("Error processing update", e);
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to {}: {}", chatId, e.getMessage());
        }
    }
}