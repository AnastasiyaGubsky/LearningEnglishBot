package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.Word;
import com.gubsky.LearningEnglishBot.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private WordService wordService;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addWord_NewWord_ShouldSaveWord() {
        Word word = new Word("cat", "кошка", userId);
        when(wordRepository.findByUserIdAndWord(userId, "cat")).thenReturn(null);

        wordService.addWord(word);

        verify(wordRepository, times(1)).save(word);
    }

    @Test
    void addWord_ExistingWord_ShouldMergeTranslations() {
        Word existingWord = new Word("cat", "кошка", userId);
        Word newWord = new Word("cat", "кот", userId);
        when(wordRepository.findByUserIdAndWord(userId, "cat")).thenReturn(existingWord);

        wordService.addWord(newWord);

        verify(wordRepository, times(1)).save(argThat(w ->
                w.getWord().equals("cat") &&
                        w.getTranslation().equals("кошка, кот")
        ));
    }

    @Test
    void getWords_ShouldReturnUserWords() {
        List<Word> words = Arrays.asList(new Word("cat", "кошка", userId), new Word("dog", "собака", userId));
        when(wordRepository.findByUserId(userId)).thenReturn(words);

        List<Word> result = wordService.getWords(userId);

        assertEquals(2, result.size());
        assertEquals("cat", result.get(0).getWord());
        assertEquals("dog", result.get(1).getWord());
    }

    @Test
    void deleteWord_ShouldDeleteExistingWord() {
        Word word = new Word("cat", "кошка", userId);
        when(wordRepository.findByUserId(userId)).thenReturn(Collections.singletonList(word));

        boolean result = wordService.deleteWord(userId, "cat");

        assertTrue(result);
        verify(wordRepository, times(1)).delete(word);
    }

    @Test
    void deleteWord_WordNotFound_ShouldReturnFalse() {
        when(wordRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        boolean result = wordService.deleteWord(userId, "cat");

        assertFalse(result);
    }

    @Test
    void deleteAllWords_ShouldDeleteAllUserWords() {
        wordService.deleteAllWords(userId);

        verify(wordRepository, times(1)).deleteAllByUserId(userId);
    }
}