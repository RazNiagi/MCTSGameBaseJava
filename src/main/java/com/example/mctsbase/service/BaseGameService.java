package com.example.mctsbase.service;

import com.example.mctsbase.model.BaseGameState;

public interface BaseGameService<T extends BaseGameState> {
    public T initializeBoard(T gameState);
    public void printBoard(T gameState);
}
