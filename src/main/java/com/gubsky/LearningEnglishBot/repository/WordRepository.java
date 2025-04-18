package com.gubsky.LearningEnglishBot.repository;

import com.gubsky.LearningEnglishBot.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByUserId(Long userId);

    Word findByUserIdAndWord(Long userId, String word);

    void deleteAllByUserId(Long userId);

}