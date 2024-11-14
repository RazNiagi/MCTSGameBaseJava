package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.BaseGameState;

import java.util.List;

public interface BaseGameMoveService<T extends BaseGameState> {
    List<T> possibleNextBoards(T gameState);
    BoardGameScore checkBoardForWins(T gameState);
}
