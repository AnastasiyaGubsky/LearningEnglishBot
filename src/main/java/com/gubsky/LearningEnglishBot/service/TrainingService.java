package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для проведения тренировки.
 * Управляет списком слов, текущим индексом и проверкой ответов.
 */
@Service
public class TrainingService {

    private final WordService wordService;
    private final Map<Long, List<Word>> ongoingTraining = new HashMap<>();
    private final Map<Long, Integer> currentWordIndex = new HashMap<>();

    public TrainingService(WordService wordService) {
        this.wordService = wordService;
    }

    public void startTraining(Long userId) {
        List<Word> words = wordService.getWords(userId);
        if (words.isEmpty()) {
            throw new IllegalStateException("У вас нет слов для тренировки. Добавьте слова.");
        }
        ongoingTraining.put(userId, words);
        currentWordIndex.put(userId, 0);
    }

    public String checkAnswer(Long userId, String translation) {
        List<Word> words = ongoingTraining.get(userId);
        Integer currentIndex = currentWordIndex.get(userId);
        if (words == null || currentIndex == null) {
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
            String correctTranslationMessage = "Правильный перевод: " + currentWord.getTranslation();
            return advanceTraining(userId, correctTranslationMessage);
        } else {
            return "Неправильно! Попробуйте снова.\nЕсли хотите увидеть правильный перевод, напишите /support.";
        }
    }

    public String getCorrectTranslation(Long userId) {
        List<Word> words = ongoingTraining.get(userId);
        Integer currentIndex = currentWordIndex.get(userId);
        if (words == null || currentIndex == null) {
            return "Тренировка завершена. Используйте команду /go для начала новой тренировки.";
        }
        Word currentWord = words.get(currentIndex);
        String correctTranslationMessage = "Правильный перевод: " + currentWord.getTranslation();
        return advanceTraining(userId, correctTranslationMessage);
    }

    public void stopTraining(Long userId) {
        ongoingTraining.remove(userId);
        currentWordIndex.remove(userId);
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
            return correctTranslationMessage + "\nТренировка завершена. Поздравляем!";
        } else {
            currentWordIndex.put(userId, currentIndex + 1);
            return correctTranslationMessage + "\nСледующее слово: " + words.get(currentIndex + 1).getWord();
        }
    }
}
