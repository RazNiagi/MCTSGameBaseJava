package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.ConnectFourGameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectFourService implements BaseGameService<ConnectFourGameState> {
    public ConnectFourGameState initializeGameState(ConnectFourGameState gameState) {
        char[][] newBoard = new char[6][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                newBoard[i][j] = '-';
            }
        }
        gameState.setBoard(newBoard);
        gameState.setCurrentTurn('r');
        gameState.setBoardGameScore(BoardGameScore.UNDETERMINED);
        return gameState;
    }

    public void printBoard(ConnectFourGameState gameState) {
        for (int i = 5; i >= 0; i--) {
            log.info(String.join(" ", new String(gameState.getBoard()[i]).split("")));
        }
    }
}
