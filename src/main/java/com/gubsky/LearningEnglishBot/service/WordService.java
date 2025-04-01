package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import com.gubsky.LearningEnglishBot.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для работы со словами.
 * Позволяет добавлять слова (с объединением переводов), получать слова, удалять слово или все слова.
 */
@Service
public class WordService {

    private final WordRepository wordRepository;

    @Autowired
    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public void addWord(Word word) {
        Word existingWord = wordRepository.findByUserIdAndWord(word.getUserId(), word.getWord());
        if (existingWord != null) {
            String mergedTranslations = mergeTranslations(existingWord.getTranslation(), word.getTranslation());
            existingWord.setTranslation(mergedTranslations);
            wordRepository.save(existingWord);
        } else {
            wordRepository.save(word);
        }
    }

    public List<Word> getWords(Long userId) {
        return wordRepository.findByUserId(userId);
    }

    @Transactional
    public boolean deleteWord(Long userId, String word) {
        List<Word> words = wordRepository.findByUserId(userId);
        for (Word w : words) {
            if (w.getWord().equalsIgnoreCase(word)) {
                wordRepository.delete(w);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void deleteAllWords(Long userId) {
        wordRepository.deleteAllByUserId(userId);
    }

    private String mergeTranslations(String existingTranslations, String newTranslations) {
        Set<String> translationSet = new LinkedHashSet<>();
        translationSet.addAll(Arrays.stream(existingTranslations.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet()));
        translationSet.addAll(Arrays.stream(newTranslations.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet()));
        return String.join(", ", translationSet);
    }
}