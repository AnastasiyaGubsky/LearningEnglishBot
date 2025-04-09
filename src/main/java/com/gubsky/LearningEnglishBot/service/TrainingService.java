package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для проведения тренировки.
 * Управляет списком слов, текущим индексом и проверкой ответов.
 */

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private final WordService wordService;
    private final Map<Long, List<Word>> ongoingTraining = new HashMap<>();
    private final Map<Long, Integer> currentWordIndex = new HashMap<>();

    public TrainingService(WordService wordService) {
        this.wordService = wordService;
    }

    public void startTraining(Long userId) {
        List<Word> words = wordService.getWords(userId);

        if (words.isEmpty()) {
            logger.warn("User {} has no words for training", userId);
            throw new IllegalStateException("У вас нет слов для тренировки. Добавьте слова.");
        }

        ongoingTraining.put(userId, words);
        currentWordIndex.put(userId, 0);
        logger.info("Training started for user {} with {} words", userId, words.size());
    }

    public String checkAnswer(Long userId, String translation) {
        List<Word> words = ongoingTraining.get(userId);
        Integer currentIndex = currentWordIndex.get(userId);

        if (words == null || currentIndex == null) {
            logger.warn("User {} attempted to check answer without active training", userId);
            return "Ошибка: тренировка не была начата. Используйте команду /go для начала.";
        }

        Word currentWord = words.get(currentIndex);

        Set<String> storedTranslations = Arrays.stream(currentWord.getTranslation().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> userTranslations = Arrays.stream(translation.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (storedTranslations.containsAll(userTranslations)) {
            String correctTranslationMessage = "Верно!";
            logger.info("User {} answered correctly for word {}", userId, currentWord.getWord());
            return advanceTraining(userId, correctTranslationMessage);
        } else {
            logger.info("User {} answered incorrectly for word {}", userId, currentWord.getWord());
            return "Неправильно! Попробуйте снова.\nЕсли хотите увидеть правильный перевод, напишите /support.";
        }
    }

    public String getCorrectTranslation(Long userId) {
        List<Word> words = ongoingTraining.get(userId);
        Integer currentIndex = currentWordIndex.get(userId);

        if (words == null || currentIndex == null) {
            logger.warn("User {} requested correct translation but training is not active", userId);
            return "Тренировка завершена. Используйте команду /go для начала новой тренировки.";
        }

        Word currentWord = words.get(currentIndex);
        String correctTranslationMessage = "Правильный перевод: " + currentWord.getTranslation();
        logger.info("User {} requested correct translation for word {}", userId, currentWord.getWord());
        return advanceTraining(userId, correctTranslationMessage);
    }

    public void stopTraining(Long userId) {
        ongoingTraining.remove(userId);
        currentWordIndex.remove(userId);
        logger.info("Training stopped for user {}", userId);
    }

    public boolean isInTraining(Long userId) {
        return ongoingTraining.containsKey(userId);
    }

    /**
     * Вспомогательный метод для обновления индекса тренировки и формирования сообщения о переходе.
     * @param userId идентификатор пользователя
     * @param correctTranslationMessage сообщение с правильным переводом текущего слова
     * @return строка с информацией о следующем слове или сообщением о завершении тренировки
     */

    private String advanceTraining(Long userId, String correctTranslationMessage) {
        List<Word> words = ongoingTraining.get(userId);
        Integer currentIndex = currentWordIndex.get(userId);

        if (currentIndex + 1 >= words.size()) {
            ongoingTraining.remove(userId);
            currentWordIndex.remove(userId);
            logger.info("Training finished for user {}", userId);
            return correctTranslationMessage + "\nТренировка завершена. Поздравляем!";
        } else {
            currentWordIndex.put(userId, currentIndex + 1);
            return correctTranslationMessage + "\nСледующее слово: " + words.get(currentIndex + 1).getWord();
        }
    }
}