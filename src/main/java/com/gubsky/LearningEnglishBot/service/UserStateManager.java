package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.UserState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для управления состояниями пользователей.
 */
@Service
public class UserStateManager {
    private final Map<Long, UserState> userStates = new HashMap<>();

    /**
     * Получить состояние пользователя.
     * @param userId идентификатор пользователя
     * @return состояние пользователя, по умолчанию WAITING
     */
    public UserState getState(Long userId) {
        return userStates.getOrDefault(userId, UserState.WAITING);
    }

    /**
     * Установить состояние пользователя.
     * @param userId идентификатор пользователя
     * @param state новое состояние
     */
    public void setState(Long userId, UserState state) {
        userStates.put(userId, state);
    }
}
