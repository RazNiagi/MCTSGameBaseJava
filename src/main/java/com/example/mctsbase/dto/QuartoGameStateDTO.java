package com.example.mctsbase.dto;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.QuartoGameState;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Data
public class QuartoGameStateDTO {
    public List<List<String>> board = new ArrayList<>();
    public char currentTurn;
    public BoardGameScore boardGameScore;
    public int level;
    public List<String> availablePieces = new ArrayList<>();
    public char selectedPiece;
    public boolean advancedMode;

    public QuartoGameStateDTO(char[][] board, char currentTurn, BoardGameScore boardGameScore, int level, List<Character> availablePieces, char selectedPiece, boolean advancedMode) {
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
        for (Character piece : availablePieces) {
            this.availablePieces.add(piece + "");
        }
        this.selectedPiece = selectedPiece;
        this.advancedMode = advancedMode;
    }

    public static QuartoGameState getGameState(QuartoGameStateDTO gameStateDTO) {
        char[][] newBoard = new char[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                newBoard[i][j] = gameStateDTO.getBoard().get(i).get(j).charAt(0);
            }
        }
        List<Character> availablePieces = new ArrayList<>();
        for (String piece : gameStateDTO.getAvailablePieces()) {
            availablePieces.add(piece.charAt(0));
        }
        return QuartoGameState.builder()
                .boardGameScore(gameStateDTO.getBoardGameScore())
                .board(newBoard)
                .currentTurn(gameStateDTO.getCurrentTurn())
                .availablePieces(new TreeSet<>(Set.copyOf(availablePieces)))
                .selectedPiece(gameStateDTO.getSelectedPiece())
                .advancedMode(gameStateDTO.isAdvancedMode())
                .build();
    }
}
