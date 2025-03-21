package com.gubsky.LearningEnglishBot.bot;

import com.gubsky.LearningEnglishBot.model.Word;
import com.gubsky.LearningEnglishBot.service.TrainingService;
import com.gubsky.LearningEnglishBot.service.WordService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandHandler {

    private final WordService wordService;
    private final TrainingService trainingService;

    public CommandHandler(WordService wordService, TrainingService trainingService) {
        this.wordService = wordService;
        this.trainingService = trainingService;
    }

    public String handleCommand(String message, Long userId) {
        switch (message) {
            case "/start":
                return startMessage();

            case "/add":
                return addWordMessage();

            case "/help":
                return getHelpMessage();

            case "/go":
                return startTraining(userId);

            case "/stop":
                return stopTraining(userId);

            case "/support":
                return getCorrectTranslation(userId);

            case "/check":
                return getWords(userId);

            case "/delete":
                return "Пожалуйста, отправьте слово на английском, которое хотите удалить.";

            case "/deleteall":
                return deleteAllWords(userId);

            default:
                return handleDefaultMessage(message, userId);
        }
    }

    private String startMessage() {
        return "Привет! Я бот для проверки переводов. Добавьте слова через команду /add " +
                "или воспользуйтесь командой /help чтобы узнать мои возможности";
    }

    private String addWordMessage() {
        return """
                Пожалуйста, отправьте пару или несколько пар слов на английском и их перевод через пробел.\n
                Если перевод слова состоит из нескольких, разделите их \"-\"\nПример: cat кошка или\n
                cat кошка\ncan мочь\ncan банка\nworkflow рабочий-процесс т.д.\n""";
    }

    private String startTraining(Long userId) {
        try {
            trainingService.startTraining(userId);
            Word firstWord = wordService.getWords(userId).get(0);
            return "Тренировка началась! Пожалуйста, введите перевод слова: " + firstWord.getWord();
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    private String stopTraining(Long userId) {
        trainingService.stopTraining(userId);
        return "Тренировка завершена.";
    }

    private String getCorrectTranslation(Long userId) {
        return trainingService.getCorrectTranslation(userId);
    }

    private String getWords(Long userId) {
        List<Word> words = wordService.getWords(userId);
        if (words.isEmpty()) {
            return "У вас нет добавленных слов.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Ваши слова:\n");
        for (Word word : words) {
            sb.append(word.getWord()).append(" - ").append(word.getTranslation()).append("\n");
        }
        return sb.toString();
    }

    private String deleteAllWords(Long userId) {
        wordService.deleteAllWords(userId);
        return "Все слова были удалены.";
    }

    private String handleDefaultMessage(String message, Long userId) {
        if (message != null && !message.trim().isEmpty()) {
            if (!message.contains(" ")) {
                return handleSingleWordInput(message.trim(), userId);
            } else {
                return handleWordPairInput(message, userId);
            }
        }
        return "Некорректная команда. Попробуйте снова.";
    }

    private String handleSingleWordInput(String word, Long userId) {
        if (trainingService.isInTraining(userId)) {
            return trainingService.checkAnswer(userId, word);
        }

        boolean deleted = wordService.deleteWord(userId, word);
        if (deleted) {
            return "Слово " + word + " было удалено.";
        } else {
            return "Ошибка. Пожалуйста, добавьте пару слов или введите нужную команду.";
        }
    }

    private String handleWordPairInput(String message, Long userId) {
        StringBuilder response = new StringBuilder();
        String[] parts = message.split("\n");

        for (String part : parts) {
            String[] wordPair = part.split(" ");
            if (wordPair.length == 2) {
                String word = wordPair[0].trim();
                String translation = wordPair[1].trim();
                Word newWord = new Word(word, translation, userId);
                wordService.addWord(newWord);
                response.append("Слово добавлено: ").append(word).append(" - ").append(translation).append("\n");
            } else {
                response.append("Некорректный формат. Используйте: слово перевод.\n");
            }
        }
        return response.toString();
    }

    private String getHelpMessage() {
        return """
                /add - добавляет новое слово/пару слов и его перевод.\n
                /go - начинает тренировку.\n
                /stop - завершает тренировку.\n
                /support - показывает правильный перевод текущего слова.\n
                /check - показать все пары слов.\n
                /delete - удалить слово.\n
                /deleteall - удалить все пары слов.
                """;
    }
}