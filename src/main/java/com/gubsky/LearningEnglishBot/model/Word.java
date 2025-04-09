package com.gubsky.LearningEnglishBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "word")
@Data
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;
    private String translation;
    private Long userId;

    public Word() {
    }

    public Word(String word, String translation, Long userId) {
        this.word = word;
        this.translation = translation;
        this.userId = userId;
    }

    public String getWord() {
        return word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public Long getUserId() {
        return userId;
    }
}