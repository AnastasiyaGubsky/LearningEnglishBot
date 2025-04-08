package com.gubsky.LearningEnglishBot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class BotInitializerTest {

    @Mock
    private Bot bot;

    @InjectMocks
    private BotInitializer botInitializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(bot.getBotToken()).thenReturn("testToken");
        when(bot.getBotUsername()).thenReturn("testUsername");
        doReturn(new DefaultBotOptions()).when(bot).getOptions();
    }

    @Test
    void telegramBotsApi_ShouldRegisterBotSuccessfully() throws TelegramApiException {
        TelegramBotsApi result = botInitializer.telegramBotsApi();
        assertNotNull(result);
    }

    @Test
    void telegramBotsApi_ShouldHandleTelegramApiException() throws TelegramApiException {
        when(bot.getBotToken()).thenReturn("");
        try {
            botInitializer.telegramBotsApi();
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }
}