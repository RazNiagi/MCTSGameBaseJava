package com.example.mctsbase.model;

import com.example.mctsbase.enums.BoardGameScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaGameState {
    private char[][] board = new char[5][5];
    private char currentTurn = 'r';
    private List<OnitamaSimpleMovementCard> bluePlayerMovementCards = new ArrayList<>();
    private List<OnitamaSimpleMovementCard> redPlayerMovementCards = new ArrayList<>();
    private OnitamaSimpleMovementCard middleCard = null;
    private BoardGameScore boardGameScore = BoardGameScore.UNDETERMINED;

    public void switchTurn() {
        this.currentTurn = this.currentTurn == 'r' ? 'b' : 'r';
    }

    public boolean equals(OnitamaGameState other) {
        return Arrays.deepEquals(this.board, other.board) && this.currentTurn == other.currentTurn;
    }

    public static OnitamaGameState cloneBoard(OnitamaGameState board) {
        char[][] newBoardArray = new char[5][5];
        for (int i = 0; i < 5; i++) {
            newBoardArray[i] = Arrays.copyOf(board.getBoard()[i], newBoardArray[i].length);
        }
        return OnitamaGameState.builder()
                .currentTurn(board.getCurrentTurn())
                .boardGameScore(board.getBoardGameScore())
                .board(newBoardArray)
                .bluePlayerMovementCards(board.getBluePlayerMovementCards())
                .redPlayerMovementCards(board.getRedPlayerMovementCards())
                .middleCard(board.getMiddleCard())
                .build();
    }
}
