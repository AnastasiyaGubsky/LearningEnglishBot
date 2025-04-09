package com.gubsky.LearningEnglishBot.bot;

import com.gubsky.LearningEnglishBot.model.UserState;
import com.gubsky.LearningEnglishBot.model.Word;
import com.gubsky.LearningEnglishBot.service.TrainingService;
import com.gubsky.LearningEnglishBot.service.UserStateService;
import com.gubsky.LearningEnglishBot.service.WordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CommandHandlerTest {

    @Mock
    private WordService wordService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserStateService userStateManager;

    @InjectMocks
    private CommandHandler commandHandler;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processWaitingCommand_StartCommand_ShouldReturnStartMessage() {
        when(userStateManager.getState(userId)).thenReturn(UserState.WAITING);
        String response = commandHandler.handleCommand("/start", userId);
        assertTrue(response.contains("Привет!"), "Ответ должен содержать приветственное сообщение");
        verify(userStateManager).setState(userId, UserState.WAITING);
    }

    @Test
    void processWaitingCommand_HelpCommand_ShouldReturnHelpMessage() {
        when(userStateManager.getState(userId)).thenReturn(UserState.WAITING);
        String response = commandHandler.handleCommand("/help", userId);
        assertTrue(response.contains("/add"), "Ответ должен содержать сообщение со списоком всех команд");
        verify(userStateManager).setState(userId, UserState.WAITING);
    }

    @Test
    void processWaitingCommand_AddCommand_ThenValidInput_ShouldReturnAddedWordMessage() {
        when(userStateManager.getState(userId)).thenReturn(UserState.WAITING);
        String addCommandResponse = commandHandler.handleCommand("/add", userId);
        assertTrue(addCommandResponse.contains("отправьте пару"), "Ответ должен содержать сообщение - как добавлять слова");
        verify(userStateManager).setState(userId, UserState.ADDING);

        when(userStateManager.getState(userId)).thenReturn(UserState.ADDING);
        String sampleInput = "cat кошка";
        String addWordResponse = commandHandler.handleCommand(sampleInput, userId);
        assertTrue(addWordResponse.contains("Слово добавлено:"), "Ответ должен содержать сообщение, что слово добавлено");
        verify(userStateManager).setState(userId, UserState.WAITING);
    }

    @Test
    void processWaitingCommand_GoCommand_ShouldStartTraining() {
        when(userStateManager.getState(userId)).thenReturn(UserState.WAITING);
        Word sampleWord = new Word("can", "мочь, банка", userId);
        when(wordService.getWords(userId)).thenReturn(Collections.singletonList(sampleWord));
        doNothing().when(trainingService).startTraining(userId);
        String response = commandHandler.handleCommand("/go", userId);
        assertTrue(response.contains("Тренировка началась"), "Ответ должен содержать сообщение, что тренировка началась");
        verify(userStateManager).setState(userId, UserState.TRAINING);
    }

    @Test
    void processTrainingCommand_StopCommand_ShouldStopTraining() {
        when(userStateManager.getState(userId)).thenReturn(UserState.TRAINING);
        when(trainingService.isInTraining(userId)).thenReturn(true);
        doNothing().when(trainingService).stopTraining(userId);
        String response = commandHandler.handleCommand("/stop", userId);
        assertTrue(response.contains("Тренировка завершена"), "Ответ должен содержать сообщение, что тренировка завершена");
        verify(userStateManager).setState(userId, UserState.WAITING);
    }

    @Test
    void processTrainingCommand_SupportCommand_ShouldReturnCorrectTranslation() {
        when(userStateManager.getState(userId)).thenReturn(UserState.TRAINING);
        when(trainingService.isInTraining(userId)).thenReturn(true);
        String translationResponse = "Правильный перевод: кот, кошка\nТренировка завершена. Поздравляем!";
        when(trainingService.getCorrectTranslation(userId)).thenReturn(translationResponse);
        String response = commandHandler.handleCommand("/support", userId);
        assertTrue(response.contains("Правильный перевод:"), "Ответ должен содержать сообщение с правильным переводом");
        verify(userStateManager).setState(userId, UserState.WAITING);
        verify(trainingService).stopTraining(userId);
    }

    @Test
    void processWaitingCommand_DeleteCommand_ShouldSetDeletingState() {
        when(userStateManager.getState(userId)).thenReturn(UserState.WAITING);
        String response = commandHandler.handleCommand("/delete", userId);
        assertEquals("Пожалуйста, отправьте слово на английском, которое хотите удалить.", response);
        verify(userStateManager).setState(userId, UserState.DELETING);
    }

    @Test
    void processWaitingCommand_NonCommandInput_ShouldReturnUnknownCommand() {
        when(userStateManager.getState(userId)).thenReturn(UserState.WAITING);
        String response = commandHandler.handleCommand("random text", userId);
        assertTrue(response.contains("Неизвестная команда"), "Ответ должен содержать сообщение - Неизвестная команда");
    }
}