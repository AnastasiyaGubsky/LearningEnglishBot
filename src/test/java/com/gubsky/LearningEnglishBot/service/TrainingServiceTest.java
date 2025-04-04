package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TrainingServiceTest {

    @Mock
    private WordService wordService;

    @InjectMocks
    private TrainingService trainingService;

    private final Long userId = 1L;
    private List<Word> sampleWords;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Word word1 = new Word("cat", "кошка", userId);
        Word word2 = new Word("dog", "собака", userId);
        sampleWords = Arrays.asList(word1, word2);
    }

    @Test
    void startTraining_NoWords_ShouldThrowException() {
        when(wordService.getWords(userId)).thenReturn(Collections.emptyList());
        Exception exception = assertThrows(IllegalStateException.class, () -> trainingService.startTraining(userId));
        assertEquals("У вас нет слов для тренировки. Добавьте слова.", exception.getMessage());
    }

    @Test
    void startTraining_WithWords_ShouldStartTraining() {
        when(wordService.getWords(userId)).thenReturn(sampleWords);
        trainingService.startTraining(userId);
        assertTrue(trainingService.isInTraining(userId));
    }

    @Test
    void checkAnswer_CorrectAnswer_ShouldAdvanceTraining() {
        when(wordService.getWords(userId)).thenReturn(sampleWords);
        trainingService.startTraining(userId);
        String response = trainingService.checkAnswer(userId, "кошка");
        assertTrue(response.contains("Следующее слово: dog") || response.contains("Тренировка завершена"));
    }

    @Test
    void checkAnswer_IncorrectAnswer_ShouldReturnErrorMessage() {
        when(wordService.getWords(userId)).thenReturn(sampleWords);
        trainingService.startTraining(userId);
        String response = trainingService.checkAnswer(userId, "забыл");
        assertTrue(response.contains("Неправильно"));
    }

    @Test
    void getCorrectTranslation_TrainingActive_ShouldReturnCorrectTranslationAndAdvance() {
        when(wordService.getWords(userId)).thenReturn(sampleWords);
        trainingService.startTraining(userId);
        String response = trainingService.getCorrectTranslation(userId);
        assertTrue(response.contains("Правильный перевод:"));
        if (!trainingService.isInTraining(userId)) {
            assertTrue(response.contains("Тренировка завершена"));
        }
    }

    @Test
    void stopTraining_ShouldRemoveTrainingData() {
        when(wordService.getWords(userId)).thenReturn(sampleWords);
        trainingService.startTraining(userId);
        assertTrue(trainingService.isInTraining(userId));
        trainingService.stopTraining(userId);
        assertFalse(trainingService.isInTraining(userId));
    }
}