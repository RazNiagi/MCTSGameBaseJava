package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.OnitamaGameState;

import java.util.List;

public class OnitamaGameMoveService implements BaseGameMoveService<OnitamaGameState> {
    public OnitamaGameState makeMove() {
        return null;
    }

    public List<OnitamaGameState> getAllLegalMoves() {
        return null;
    }

    public List<OnitamaGameState> possibleNextBoards(OnitamaGameState gameState) {
        return List.of();
    }

    public BoardGameScore checkBoardForWins(OnitamaGameState gameState) {
        return null;
    }
}
