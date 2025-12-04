package com.example.mctsbase.service;

import com.example.mctsbase.dto.QuartoGameStateDTO;
import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.QuartoGameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;

@Slf4j
@Service
public class QuartoGameService implements BaseGameService<QuartoGameState> {
    public QuartoGameState initializeGameState(QuartoGameState gameState) {
        char[][] newBoard = new char[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                newBoard[i][j] = '-';
            }
        }
        gameState.setAvailablePieces(new TreeSet<>(Set.of(
                'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H',
                'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P'
        )));
        gameState.setBoard(newBoard);
        gameState.setCurrentTurn('1');
        gameState.setBoardGameScore(BoardGameScore.UNDETERMINED);
        gameState.setSelectedPiece('-');
        return gameState;
    }

    public void printBoard(QuartoGameState gameState) {
        for (int i = 0; i < 4; i++) {
            log.info(String.join(" ", new String(gameState.getBoard()[i]).split("")));
        }
    }

    public QuartoGameStateDTO convertToDTO(QuartoGameState gameState, int level) {
        return new QuartoGameStateDTO(
                gameState.getBoard(),
                gameState.getCurrentTurn(),
                gameState.getBoardGameScore(),
                level,
                gameState.getAvailablePieces().stream().toList(),
                gameState.getSelectedPiece()
        );
    }
}
