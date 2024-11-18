package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.enums.OnitamaExpansion;
import com.example.mctsbase.model.OnitamaGameState;
import com.example.mctsbase.model.OnitamaMovementCard;
import com.example.mctsbase.model.OnitamaSimpleMovementCard;
import com.example.mctsbase.model.OnitamaSimplifiedGameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
public class OnitamaGameService implements BaseGameService<OnitamaGameState> {
    @Autowired
    private OnitamaMovementCardService onitamaMovementCardService;
    @Autowired
    private OnitamaGameMoveService onitamaGameMoveService;

    public OnitamaGameState initializeGameState(OnitamaGameState gameState) {
        char[][] newBoard = new char[5][5];
        for (int i = 1; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                newBoard[i][j] = '-';
            }
        }
        IntStream.range(0, 5).forEachOrdered(i -> {
            newBoard[0][i] = 'r';
            newBoard[4][i] = 'b';
            if (i == 2) {
                newBoard[0][i] = Character.toUpperCase(newBoard[0][i]);
                newBoard[4][i] = Character.toUpperCase(newBoard[4][i]);
            }
        });
        gameState.setBoard(newBoard);
        gameState.setCurrentTurn('r');
        gameState.setBoardGameScore(BoardGameScore.UNDETERMINED);
        List<OnitamaMovementCard> possibleMovementCards = onitamaMovementCardService.getOnitamaMovementCardsFromExpansions(Collections.singletonList(OnitamaExpansion.BASE));
        Collections.shuffle(possibleMovementCards);
        gameState.getRedPlayerMovementCards().add(OnitamaSimpleMovementCard.cloneCard(possibleMovementCards.removeFirst()));
        gameState.getRedPlayerMovementCards().add(OnitamaSimpleMovementCard.cloneCard(possibleMovementCards.removeFirst()));
        gameState.getBluePlayerMovementCards().add(OnitamaSimpleMovementCard.cloneCard(possibleMovementCards.removeFirst()));
        gameState.getBluePlayerMovementCards().add(OnitamaSimpleMovementCard.cloneCard(possibleMovementCards.removeFirst()));
        gameState.setCurrentTurn(possibleMovementCards.getFirst().getStampColor());
        gameState.setMiddleCard(OnitamaSimpleMovementCard.cloneCard(possibleMovementCards.removeFirst()));
        if (gameState.getCurrentTurn() != gameState.getBoard()[0][0]) {
            gameState.setBoard(onitamaGameMoveService.rotateBoard(gameState.getBoard()));
        }

        return gameState;
    }

    public OnitamaGameState initializeGameState(OnitamaGameState gameState, List<String> validCards) {
        char[][] newBoard = new char[5][5];
        for (int i = 1; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                newBoard[i][j] = '-';
            }
        }
        IntStream.range(0, 5).forEachOrdered(i -> {
            newBoard[0][i] = 'r';
            newBoard[4][i] = 'b';
            if (i == 2) {
                newBoard[0][i] = Character.toUpperCase(newBoard[0][i]);
                newBoard[4][i] = Character.toUpperCase(newBoard[4][i]);
            }
        });
        gameState.setBoard(newBoard);
        gameState.setCurrentTurn('r');
        gameState.setBoardGameScore(BoardGameScore.UNDETERMINED);
        List<OnitamaMovementCard> possibleMovementCards = new ArrayList<>(onitamaMovementCardService.getFilteredCardsFromNames(validCards));
        Collections.shuffle(possibleMovementCards);
        gameState.getRedPlayerMovementCards().add(possibleMovementCards.removeFirst());
        gameState.getRedPlayerMovementCards().add(possibleMovementCards.removeFirst());
        gameState.getBluePlayerMovementCards().add(possibleMovementCards.removeFirst());
        gameState.getBluePlayerMovementCards().add(possibleMovementCards.removeFirst());
        gameState.setCurrentTurn(possibleMovementCards.getFirst().getStampColor());
        gameState.setMiddleCard(possibleMovementCards.removeFirst());
        if (gameState.getCurrentTurn() != gameState.getBoard()[0][0]) {
            gameState.setBoard(onitamaGameMoveService.rotateBoard(gameState.getBoard()));
        }

        return gameState;
    }

    public OnitamaGameState initializeGameStateFromSimplified(OnitamaSimplifiedGameState gameState) {
        OnitamaGameState newGameState = new OnitamaGameState();
        newGameState.setBoard(convertBoardStringToBoard(gameState.getBoardString()));
        newGameState.setCurrentTurn(gameState.getCurrentTurn());
        newGameState.setBoardGameScore(gameState.getBoardGameScore());
        for (String s : gameState.getRedPlayerMovementCards()) {
            newGameState.getRedPlayerMovementCards().add(OnitamaSimpleMovementCard.cloneCard(onitamaMovementCardService.getCardFromName(s)));
        }
        for (String s : gameState.getBluePlayerMovementCards()) {
            newGameState.getBluePlayerMovementCards().add(OnitamaSimpleMovementCard.cloneCard(onitamaMovementCardService.getCardFromName(s)));
        }
        newGameState.setMiddleCard(OnitamaSimpleMovementCard.cloneCard(onitamaMovementCardService.getCardFromName(gameState.getMiddleCard())));
        if (newGameState.getCurrentTurn() != newGameState.getBoard()[0][0]) {
            newGameState.setBoard(onitamaGameMoveService.rotateBoard(newGameState.getBoard()));
        }

        return newGameState;
    }

    public char[][] convertBoardStringToBoard(String boardString) {
        char[][] board = new char[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                board[i][j] = boardString.charAt(5 * i + j);
            }
        }
        return board;
    }

    public void printBoard(OnitamaGameState gameState) {
        for (int i = 4; i >= 0; i--) {
            log.info(String.join(" ", new String(gameState.getBoard()[i]).split("")));
        }
        log.info("Current turn: {}", gameState.getCurrentTurn());
        log.info("Red movement cards: {}", String.join(" ", gameState.getRedPlayerMovementCards().stream().map(OnitamaSimpleMovementCard::getName).toList()));
        log.info("Blue movement cards: {}", String.join(" ", gameState.getBluePlayerMovementCards().stream().map(OnitamaSimpleMovementCard::getName).toList()));
        log.info("Middle movement card: {}", gameState.getMiddleCard().getName());
    }
}
