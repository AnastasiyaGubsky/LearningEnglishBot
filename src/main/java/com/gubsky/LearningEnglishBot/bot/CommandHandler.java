package com.gubsky.LearningEnglishBot.bot;

import com.gubsky.LearningEnglishBot.model.UserState;
import com.gubsky.LearningEnglishBot.model.Word;
import com.gubsky.LearningEnglishBot.service.TrainingService;
import com.gubsky.LearningEnglishBot.service.UserStateService;
import com.gubsky.LearningEnglishBot.service.WordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Обработчик команд, полученных от пользователя.
 * Разделяет логику по режимам: добавление, тренировка, удаление и режим ожидания.
 */
@Component
public class CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private static final String UNKNOWN_COMMAND = "Неизвестная команда. Пожалуйста, введите команду из списка:\n" + getHelpMessage();
    private static final String DELETE_COMMAND = "Пожалуйста, отправьте слово на английском, которое хотите удалить.";

    private final WordService wordService;
    private final TrainingService trainingService;
    private final UserStateService userStateManager;

    public CommandHandler(WordService wordService, TrainingService trainingService, UserStateService userStateManager) {
        this.wordService = wordService;
        this.trainingService = trainingService;
        this.userStateManager = userStateManager;
    }

    /**
     * Основной метод обработки входящих команд.
     *
     * @param message сообщение от пользователя
     * @param userId  идентификатор пользователя
     * @return ответ бота
     */
    public String handleCommand(String message, Long userId) {
        try {
            // Если пользователь в режиме TRAINING, но тренировка завершена, сбрасываем состояние
            if (userStateManager.getState(userId) == UserState.TRAINING && !trainingService.isInTraining(userId)) {
                logger.info("User {} training finished, resetting state to WAITING", userId);
                userStateManager.setState(userId, UserState.WAITING);
            }

            UserState state = userStateManager.getState(userId);
            logger.info("User {} state: {}", userId, state);

            if (message.startsWith("/")) {
                logger.debug("Processing command: {}", message);
                return processCommand(message, userId, state);
            } else {
                logger.debug("Processing input: {}", message);
                return processInput(message, userId, state);
            }
        } catch (Exception e) {
            logger.error("Error handling command for user {}: {}", userId, e.getMessage());
            return "Произошла ошибка при обработке команды.";
        }
    }

    private String processCommand(String message, Long userId, UserState state) {
        // Обработка команд зависит от текущего режима
        switch (state) {
            case ADDING:
                logger.warn("User {} in ADDING state entered command: {}", userId, message);
                return "Добавьте пару или несколько пар слов";
            case TRAINING:
                return processTrainingCommand(message, userId);
            case DELETING:
                return DELETE_COMMAND;
            case WAITING:
            default:
                return processWaitingCommand(message, userId);
        }
    }

    private String processWaitingCommand(String message, Long userId) {
        // Обработка команд, когда пользователь в режиме ожидания (WAITING)
        switch (message) {
            case "/start":
                userStateManager.setState(userId, UserState.WAITING);
                return startMessage();
            case "/help":
                userStateManager.setState(userId, UserState.WAITING);
                return getHelpMessage();
            case "/add":
                userStateManager.setState(userId, UserState.ADDING);
                return addWordMessage();
            case "/go":
                userStateManager.setState(userId, UserState.TRAINING);
                return processStartTraining(userId);
            case "/delete":
                userStateManager.setState(userId, UserState.DELETING);
                return DELETE_COMMAND;
            case "/deleteall":
                userStateManager.setState(userId, UserState.WAITING);
                return processDeleteAllWords(userId);
            case "/check":
                userStateManager.setState(userId, UserState.WAITING);
                return processShowWords(userId);
            default:
                return UNKNOWN_COMMAND;
        }
    }

    private String processTrainingCommand(String message, Long userId) {
        // Обработка команд в режиме тренировки
        if (message.equals("/stop")) {
            userStateManager.setState(userId, UserState.WAITING);
            logger.info("User {} stopped training", userId);
            return processStopTraining(userId);
        } else if (message.equals("/support")) {
            String supportMessage = processCorrectTranslation(userId);
            logger.info("User {} requested support: {}", userId, supportMessage);
            if (supportMessage.contains("Тренировка завершена")) {
                userStateManager.setState(userId, UserState.WAITING);
                trainingService.stopTraining(userId);
            }
            return supportMessage;
        } else {
            return "В режиме тренировки доступны только команды /stop и /support, или введите перевод слова.";
        }
    }

    private String processInput(String message, Long userId, UserState state) {
        // Обработка текстового ввода (без слеша) в зависимости от режима
        switch (state) {
            case ADDING:
                String addResponse = processAddWord(message, userId);
                if (!addResponse.contains("Некорректный формат")) {
                    userStateManager.setState(userId, UserState.WAITING);
                }
                return addResponse;
            case TRAINING:
                return trainingService.checkAnswer(userId, message);
            case DELETING:
                String delResponse = processDeleteWord(message, userId);
                if (!delResponse.contains("нет в вашем списке")) {
                    userStateManager.setState(userId, UserState.WAITING);
                }
                return delResponse;
            case WAITING:
            default:
                return UNKNOWN_COMMAND;
        }
    }

    private String processStartTraining(Long userId) {
        try {
            trainingService.startTraining(userId);
            Word firstWord = wordService.getWords(userId).get(0);
            return "Тренировка началась! Пожалуйста, введите перевод слова: " + firstWord.getWord();
        } catch (IllegalStateException e) {
            logger.error("Error starting training for user {}: {}", userId, e.getMessage());
            return e.getMessage();
        }
    }

    private String processStopTraining(Long userId) {
        trainingService.stopTraining(userId);
        return "Тренировка завершена.";
    }

    private String processCorrectTranslation(Long userId) {
        return trainingService.getCorrectTranslation(userId);
    }

    private String processShowWords(Long userId) {
        List<Word> words = wordService.getWords(userId);
        if (words.isEmpty()) {
            return "У вас нет добавленных слов.";
        }
        StringBuilder sb = new StringBuilder("Ваши слова:\n");
        for (Word word : words) {
            sb.append(word.getWord()).append(" - ").append(word.getTranslation()).append("\n");
        }
        return sb.toString();
    }

    private String processAddWord(String message, Long userId) {
        StringBuilder response = new StringBuilder();
        String[] parts = message.split("\n");

        for (String part : parts) {
            String[] wordPair = part.split(" ", 2);
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

    private String processDeleteAllWords(Long userId) {
        wordService.deleteAllWords(userId);
        return "Все слова были удалены.";
    }

    private String processDeleteWord(String word, Long userId) {
        if (trainingService.isInTraining(userId)) {
            return trainingService.checkAnswer(userId, word);
        }
        boolean deleted = wordService.deleteWord(userId, word);
        if (deleted) {
            return "Слово " + word + " было удалено.";
        } else {
            return "Ошибка. Слова " + word + " нет в вашем списке.";
        }
    }

    private String startMessage() {
        return """
                Привет! Я бот, который поможет тебе запомнить английские слова.
                Чтобы узнать мои возможности, воспользуйся командой /help.
                """;
    }

    private String addWordMessage() {
        return """
                Пожалуйста, отправьте пару или несколько пар слов на английском и их перевод через пробел.
                Если переводов несколько, напишите их через ",".
                Если перевод состоит из нескольких слов, разделите их "-".
                Каждую пару пишите с новой строки.
                                
                Пример:
                cat кошка
                dog собака
                can банка, мочь, способный
                workflow рабочий-процесс""";
    }

    private static String getHelpMessage() {
        return """
                /add - добавить слова.
                /go - начать тренировку.
                /stop - завершить тренировку.
                /check - показать все пары слов.
                /delete - удалить одну пару слов.
                /deleteall - удалить все пары слов.
                """;
    }
}