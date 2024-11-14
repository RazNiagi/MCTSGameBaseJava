package com.example.mctsbase.model;

import com.example.mctsbase.enums.ConnectFourScore;
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
    private ConnectFourScore connectFourScore = ConnectFourScore.UNDETERMINED;

    public void switchTurn() {
        this.currentTurn = this.currentTurn == 'r' ? 'y' : 'r';
    }

    public boolean equals(ConnectFourGameState other) {
        return Arrays.deepEquals(this.board, other.board) && this.currentTurn == other.currentTurn && this.connectFourScore == other.connectFourScore;
    }

    public static ConnectFourGameState cloneBoard(ConnectFourGameState board) {
        char[][] newBoardArray = new char[6][7];
        for (int i = 0; i < 6; i++) {
            newBoardArray[i] = Arrays.copyOf(board.getBoard()[i], newBoardArray[i].length);
        }
        return ConnectFourGameState.builder()
                .currentTurn(board.getCurrentTurn())
                .connectFourScore(board.getConnectFourScore())
                .board(newBoardArray)
                .build();
    }
}
