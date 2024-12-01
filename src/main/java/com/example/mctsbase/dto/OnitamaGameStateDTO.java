package com.example.mctsbase.dto;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.OnitamaGameState;
import com.example.mctsbase.model.OnitamaMovementCard;
import com.example.mctsbase.model.OnitamaSimpleMovementCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaGameStateDTO {
    public List<List<String>> board = new ArrayList<>();
    public char currentTurn;
    public BoardGameScore boardGameScore;
    public List<OnitamaMovementCard> bluePlayerMovementCards;
    public List<OnitamaMovementCard> redPlayerMovementCards;
    public OnitamaMovementCard middleCard;
    public int level;

    public OnitamaGameStateDTO(char[][] board, char currentTurn, BoardGameScore boardGameScore,
                               List<OnitamaMovementCard> bluePlayerMovementCards, List<OnitamaMovementCard> redPlayerMovementCards,
                               OnitamaMovementCard middleCard, int level) {
        this.currentTurn = currentTurn;
        this.boardGameScore = boardGameScore;
        for (char[] chars : board) {
            List<String> tempList = new ArrayList<>();
            for (char aChar : chars) {
                tempList.add(aChar + "");
            }
            this.board.add(tempList);
        }
        this.bluePlayerMovementCards = bluePlayerMovementCards;
        this.redPlayerMovementCards = redPlayerMovementCards;
        this.middleCard = middleCard;
        this.level = level;
    }

    public static OnitamaGameState getGameState(OnitamaGameStateDTO gameStateDTO) {
        char[][] newBoard = new char[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                newBoard[i][j] = gameStateDTO.getBoard().get(i).get(j).charAt(0);
            }
        }
        List<OnitamaSimpleMovementCard> redPlayerCards = new ArrayList<>();
        for (OnitamaMovementCard card : gameStateDTO.getRedPlayerMovementCards()) {
            redPlayerCards.add(OnitamaSimpleMovementCard.cloneCard(card));
        }
        List<OnitamaSimpleMovementCard> bluePlayerCards = new ArrayList<>();
        for (OnitamaMovementCard card : gameStateDTO.getBluePlayerMovementCards()) {
            bluePlayerCards.add(OnitamaSimpleMovementCard.cloneCard(card));
        }

        return OnitamaGameState.builder()
                .boardGameScore(gameStateDTO.getBoardGameScore())
                .board(newBoard)
                .currentTurn(gameStateDTO.getCurrentTurn())
                .redPlayerMovementCards(redPlayerCards)
                .bluePlayerMovementCards(bluePlayerCards)
                .middleCard(OnitamaSimpleMovementCard.cloneCard(gameStateDTO.getMiddleCard()))
                .build();
    }
}
