package com.gubsky.LearningEnglishBot.service;

import com.gubsky.LearningEnglishBot.model.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserStateManagerTest {

    private UserStateService userStateManager;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        userStateManager = new UserStateService();
    }

    @Test
    void testGetState_NewUser_ShouldReturnWaiting() {
        UserState state = userStateManager.getState(userId);

        assertEquals(UserState.WAITING, state, "Состояние по-умолчинию должно быть WAITING");
    }

    @Test
    void testSetState_ShouldUpdateState() {
        userStateManager.setState(userId, UserState.TRAINING);

        UserState state = userStateManager.getState(userId);

        assertEquals(UserState.TRAINING, state, "Состояние должно обновиться на TRAINING");
    }

    @Test
    void testMultipleUsers_ShouldStoreStatesSeparately() {
        Long userId2 = 2L;

        userStateManager.setState(userId, UserState.TRAINING);
        userStateManager.setState(userId2, UserState.DELETING);

        assertEquals(UserState.TRAINING, userStateManager.getState(userId));

        assertEquals(UserState.DELETING, userStateManager.getState(userId2));
    }
}