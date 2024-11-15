package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaGameState extends BaseGameState {
    @Builder.Default
    private List<OnitamaSimpleMovementCard> bluePlayerMovementCards = new ArrayList<>();
    @Builder.Default
    private List<OnitamaSimpleMovementCard> redPlayerMovementCards = new ArrayList<>();
    @Builder.Default
    private OnitamaSimpleMovementCard middleCard = null;

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
        List<OnitamaSimpleMovementCard> newBluePlayerMovementCards = new ArrayList<>();
        List<OnitamaSimpleMovementCard> newRedPlayerMovementCards = new ArrayList<>();
        board.getBluePlayerMovementCards().forEach(card -> newBluePlayerMovementCards.add(OnitamaSimpleMovementCard.cloneCard(card)));
        board.getRedPlayerMovementCards().forEach(card -> newRedPlayerMovementCards.add(OnitamaSimpleMovementCard.cloneCard(card)));
        return OnitamaGameState.builder()
                .currentTurn(board.getCurrentTurn())
                .boardGameScore(board.getBoardGameScore())
                .board(newBoardArray)
                .bluePlayerMovementCards(newBluePlayerMovementCards)
                .redPlayerMovementCards(newRedPlayerMovementCards)
                .middleCard(OnitamaSimpleMovementCard.cloneCard(board.getMiddleCard()))
                .build();
    }
}
