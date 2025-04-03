package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import com.gubsky.LearningEnglishBot.repository.WordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Позволяет добавлять слова (слово + перевод), получать все слова, удалять слово или все слова.
 */
@Service
public class WordService {

    private static final Logger logger = LoggerFactory.getLogger(WordService.class);

    private final WordRepository wordRepository;

    @Autowired
    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public void addWord(Word word) {
        try {
            Word existingWord = wordRepository.findByUserIdAndWord(word.getUserId(), word.getWord());
            if (existingWord != null) {
                String mergedTranslations = mergeTranslations(existingWord.getTranslation(), word.getTranslation());
                existingWord.setTranslation(mergedTranslations);
                wordRepository.save(existingWord);
                logger.info("Updated word {} for user {} with translations: {}", word.getWord(), word.getUserId(), mergedTranslations);
            } else {
                wordRepository.save(word);
                logger.info("Added new word {} for user {}", word.getWord(), word.getUserId());
            }
        } catch (Exception e) {
            logger.error("Error adding word {} for user {}: {}", word.getWord(), word.getUserId(), e.getMessage());
            throw e;
        }
    }

    public List<Word> getWords(Long userId) {
        try {
            return wordRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Error retrieving words for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public boolean deleteWord(Long userId, String word) {
        try {
            List<Word> words = wordRepository.findByUserId(userId);
            for (Word w : words) {
                if (w.getWord().equalsIgnoreCase(word)) {
                    wordRepository.delete(w);
                    logger.info("Deleted word {} for user {}", word, userId);
                    return true;
                }
            }
            logger.warn("Word {} not found for user {}", word, userId);
            return false;
        } catch (Exception e) {
            logger.error("Error deleting word {} for user {}: {}", word, userId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteAllWords(Long userId) {
        try {
            wordRepository.deleteAllByUserId(userId);
            logger.info("Deleted all words for user {}", userId);
        } catch (Exception e) {
            logger.error("Error deleting all words for user {}: {}", userId, e.getMessage());
            throw e;
        }
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

        String result = String.join(", ", translationSet);
        logger.debug("Merged translations: {}", result);
        return result;
    }
}