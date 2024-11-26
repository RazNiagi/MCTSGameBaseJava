package com.example.mctsbase.dto;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.ConnectFourGameState;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConnectFourGameStateDTO {
    public List<List<String>> board = new ArrayList<>();
    public char currentTurn;
    public BoardGameScore boardGameScore;
    public int level;

    public ConnectFourGameStateDTO(char[][] board, char currentTurn, BoardGameScore boardGameScore, int level) {
        this.currentTurn = currentTurn;
        this.boardGameScore = boardGameScore;
        for (char[] chars : board) {
            List<String> tempList = new ArrayList<>();
            for (char aChar : chars) {
                tempList.add(aChar + "");
            }
            this.board.add(tempList);
        }
        this.level = level;
    }

    public static ConnectFourGameState getGameState(ConnectFourGameStateDTO gameStateDTO) {
        char[][] newBoard = new char[6][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                newBoard[i][j] = gameStateDTO.getBoard().get(i).get(j).charAt(0);
            }
        }
        return ConnectFourGameState.builder()
                .boardGameScore(gameStateDTO.getBoardGameScore())
                .board(newBoard)
                .currentTurn(gameStateDTO.getCurrentTurn())
                .build();
    }
}
