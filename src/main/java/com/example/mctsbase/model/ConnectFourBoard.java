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
public class ConnectFourBoard {
    private char[][] board = new char[6][7];
    private char currentTurn = 'r';
    private int turnNumber;
    private ConnectFourScore connectFourScore = ConnectFourScore.UNDETERMINED;

    public void switchTurn() {
        this.currentTurn = this.currentTurn == 'r' ? 'y' : 'r';
    }

    public boolean equals(ConnectFourBoard other) {
        return Arrays.deepEquals(this.board, other.board) && this.currentTurn == other.currentTurn && this.turnNumber == other.turnNumber && this.connectFourScore == other.connectFourScore;
    }

    public static ConnectFourBoard cloneBoard(ConnectFourBoard board) {
        char[][] newBoardArray = new char[6][7];
        for (int i = 0; i < 6; i++) {
            newBoardArray[i] = Arrays.copyOf(board.getBoard()[i], newBoardArray[i].length);
        }
        return ConnectFourBoard.builder()
                .turnNumber(board.getTurnNumber())
                .currentTurn(board.getCurrentTurn())
                .connectFourScore(board.getConnectFourScore())
                .board(newBoardArray)
                .build();
    }
}
