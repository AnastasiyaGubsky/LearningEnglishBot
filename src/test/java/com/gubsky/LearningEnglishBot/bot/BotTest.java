package com.gubsky.LearningEnglishBot.bot;

import com.gubsky.LearningEnglishBot.config.BotConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BotTest {

    @Mock
    private CommandHandler commandHandler;

    @Mock
    private BotConfig botConfig;

    @Spy
    @InjectMocks
    private Bot bot;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(botConfig.getUsername()).thenReturn("testUsername");
        when(botConfig.getToken()).thenReturn("testToken");
    }

    private Update createUpdate(String text, Long chatId) {
        Update update = new Update();
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(text);
        when(message.hasText()).thenReturn(true);
        update.setMessage(message);
        return update;
    }

    @Test
    void getBotUsername_ShouldReturnCorrectUsername() {
        assertEquals("testUsername", bot.getBotUsername());
    }

    @Test
    void getBotToken_ShouldReturnCorrectToken() {
        assertEquals("testToken", bot.getBotToken());
    }

    @Test
    void onUpdateReceived_ShouldSendMessage() throws Exception {
        Update update = createUpdate("/start", userId);
        when(commandHandler.handleCommand("/start", userId)).thenReturn("Привет!");
        doReturn(null).when(bot).execute(any(SendMessage.class));
        bot.onUpdateReceived(update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        verify(bot, times(1)).execute(captor.capture());

        SendMessage captured = captor.getValue();

        assertEquals("Привет!", captured.getText());
        assertEquals(String.valueOf(userId), captured.getChatId());
        verify(commandHandler, times(1)).handleCommand("/start", userId);
    }

    @Test
    void onUpdateReceived_ShouldHandleException() {
        Update update = createUpdate("Привет!", userId);
        when(commandHandler.handleCommand("Привет!", userId))
                .thenThrow(new RuntimeException("Тестовое исключение"));

        bot.onUpdateReceived(update);

        verify(bot, never()).sendMessage(anyLong(), anyString());
    }

    @Test
    void sendMessage_ShouldExecuteSendMessage() throws TelegramApiException {
        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(userId);
        expectedMessage.setText("Привет!");
        doReturn(null).when(bot).execute(any(SendMessage.class));

        bot.sendMessage(userId, "Привет!");
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        verify(bot, times(1)).execute(captor.capture());

        SendMessage captured = captor.getValue();

        assertEquals(String.valueOf(userId), captured.getChatId());
        assertEquals("Привет!", captured.getText());
    }

    @Test
    void sendMessage_ShouldHandleTelegramApiException() throws TelegramApiException {
        doThrow(new TelegramApiException("Тестовое исключение")).when(bot).execute(any(SendMessage.class));

        bot.sendMessage(userId, "Привет!");

        verify(bot, times(1)).execute(any(SendMessage.class));
    }
}