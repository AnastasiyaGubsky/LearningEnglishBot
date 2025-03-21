package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import com.gubsky.LearningEnglishBot.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WordService {

    private final WordRepository wordRepository;

    @Autowired
    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public void addWord(Word word) {
        wordRepository.save(word);
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
}