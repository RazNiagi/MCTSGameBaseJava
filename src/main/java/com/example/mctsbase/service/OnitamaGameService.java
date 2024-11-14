package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.OnitamaGameState;
import com.example.mctsbase.model.OnitamaMovementCard;
import com.example.mctsbase.model.OnitamaSimpleMovementCard;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
public class OnitamaGameService implements BaseGameService<OnitamaGameState> {
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        List<OnitamaMovementCard> onitamaMovementCards;
        try {
            File onitamaCardsResourceFile = new ClassPathResource("onitamaCards.json").getFile();
            onitamaMovementCards = objectMapper.readValue(onitamaCardsResourceFile, new TypeReference<>() {});
            onitamaMovementCards = new ArrayList<>(onitamaMovementCards.stream().filter(onitamaMovementCard -> onitamaMovementCard.getExpansion().equals("base")).toList());
            Collections.shuffle(onitamaMovementCards);
            gameState.getRedPlayerMovementCards().add(onitamaMovementCards.removeFirst());
            gameState.getRedPlayerMovementCards().add(onitamaMovementCards.removeFirst());
            gameState.getBluePlayerMovementCards().add(onitamaMovementCards.removeFirst());
            gameState.getBluePlayerMovementCards().add(onitamaMovementCards.removeFirst());
            gameState.setCurrentTurn(onitamaMovementCards.getFirst().getStampColor());
            gameState.setMiddleCard(onitamaMovementCards.removeFirst());
            if (gameState.getCurrentTurn() != gameState.getBoard()[0][0]) {
                gameState.setBoard(rotateBoard(gameState.getBoard()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return gameState;
    }

    public char[][] rotateBoard(char[][] board) {
        char[][] newBoard = new char[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                newBoard[i][j] = board[4 - i][4 - j];
            }
        }
        return newBoard;
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
