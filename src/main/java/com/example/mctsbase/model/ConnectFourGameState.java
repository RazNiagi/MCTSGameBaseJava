package com.example.mctsbase.model;

import com.example.mctsbase.enums.BoardGameScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ConnectFourGameState {
    private char[][] board = new char[6][7];
    private char currentTurn = 'r';
    private BoardGameScore boardGameScore = BoardGameScore.UNDETERMINED;

    public void switchTurn() {
        this.currentTurn = this.currentTurn == 'r' ? 'y' : 'r';
    }

    public boolean equals(ConnectFourGameState other) {
        return Arrays.deepEquals(this.board, other.board) && this.currentTurn == other.currentTurn && this.boardGameScore == other.getBoardGameScore();
    }

    public static ConnectFourGameState cloneBoard(ConnectFourGameState board) {
        char[][] newBoardArray = new char[6][7];
        for (int i = 0; i < 6; i++) {
            newBoardArray[i] = Arrays.copyOf(board.getBoard()[i], newBoardArray[i].length);
        }
        return ConnectFourGameState.builder()
                .currentTurn(board.getCurrentTurn())
                .boardGameScore(board.getBoardGameScore())
                .board(newBoardArray)
                .build();
    }
}
