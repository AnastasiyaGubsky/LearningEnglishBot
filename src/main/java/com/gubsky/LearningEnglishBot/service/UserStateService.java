package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.UserState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для управления состояниями пользователей.
 */
@Service
public class UserStateService {

    private final Map<Long, UserState> userStates = new HashMap<>();

    public UserState getState(Long userId) {
        return userStates.getOrDefault(userId, UserState.WAITING);
    }

    public void setState(Long userId, UserState state) {
        userStates.put(userId, state);
    }
}