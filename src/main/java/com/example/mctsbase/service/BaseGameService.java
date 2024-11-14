package com.example.mctsbase.service;

import com.example.mctsbase.model.BaseGameState;

public interface BaseGameService<T extends BaseGameState> {
    T initializeGameState(T gameState);
    void printBoard(T gameState);
}
