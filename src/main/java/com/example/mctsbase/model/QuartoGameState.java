package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class QuartoGameState extends BaseGameState {
    private char selectedPiece;
    private SortedSet<Character> availablePieces;
    private boolean advancedMode;

    // Override Lombok-generated getter to return unmodifiable copy for defensive copying
    public SortedSet<Character> getAvailablePieces() {
        return availablePieces != null ? Collections.unmodifiableSortedSet(availablePieces) : null;
    }

    public void switchTurn() {
        this.currentTurn = this.currentTurn == '1' ? '2' : '1';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        QuartoGameState other = (QuartoGameState) obj;
        return Arrays.deepEquals(this.board, other.board)
                && this.currentTurn == other.currentTurn
                && this.boardGameScore == other.getBoardGameScore()
                && this.selectedPiece == other.selectedPiece
                && ((this.availablePieces == null && other.availablePieces == null) ||
                (this.availablePieces != null && this.availablePieces.equals(other.availablePieces)))
                && this.advancedMode == other.advancedMode;
    }

    public static QuartoGameState cloneBoard(QuartoGameState board) {
        char[][] newBoardArray = new char[4][4];
        for (int i = 0; i < 4; i++) {
            newBoardArray[i] = Arrays.copyOf(board.getBoard()[i], newBoardArray[i].length);
        }
        return QuartoGameState.builder()
                .currentTurn(board.getCurrentTurn())
                .boardGameScore(board.getBoardGameScore())
                .board(newBoardArray)
                .selectedPiece(board.getSelectedPiece())
                .availablePieces(new TreeSet<>(board.getAvailablePieces()))
                .advancedMode(board.isAdvancedMode())
                .build();
    }
}
