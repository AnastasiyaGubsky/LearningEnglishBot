package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (currentWord.getTranslation().equalsIgnoreCase(translation)) {
            currentWordIndex.put(userId, currentIndex + 1);

            if (currentIndex + 1 >= words.size()) {
                ongoingTraining.remove(userId);
                currentWordIndex.remove(userId);
                return "Правильно! Тренировка завершена. Поздравляем!";
            }
            return "Правильно! Следующее слово: " + words.get(currentIndex + 1).getWord();
        } else {
            return "Неправильно! Попробуйте снова.\nЕсли хотите увидеть правильный перевод, напишите /support.";
        }
    }

    public String getCorrectTranslation(Long userId) {
        List<Word> words = ongoingTraining.get(userId);
        int currentIndex = currentWordIndex.get(userId);
        Word currentWord = words.get(currentIndex);

        String correctTranslationMessage = "Правильный перевод: " + currentWord.getTranslation();

        if (currentIndex + 1 < words.size()) {
            currentWordIndex.put(userId, currentIndex + 1);
            return correctTranslationMessage + "\nСледующее слово: " + words.get(currentIndex + 1).getWord();
        } else {
            ongoingTraining.remove(userId);
            currentWordIndex.remove(userId);
            return correctTranslationMessage + "\nТренировка завершена. Поздравляем!";
        }
    }

    public void stopTraining(Long userId) {
        ongoingTraining.remove(userId);
        currentWordIndex.remove(userId);
    }

    public String getNextWord(Long userId) {
        List<Word> words = ongoingTraining.get(userId);
        Integer currentIndex = currentWordIndex.get(userId);

        if (words == null || currentIndex == null) {
            return "Ошибка: тренировка не была начата. Используйте команду /go для начала.";
        }

        return words.get(currentIndex).getWord();
    }

    public boolean isInTraining(Long userId) {
        return ongoingTraining.containsKey(userId);
    }
}