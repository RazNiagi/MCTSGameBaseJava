package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.OnitamaGameState;
import com.example.mctsbase.model.OnitamaMove;
import com.example.mctsbase.model.OnitamaMovementCard;
import com.example.mctsbase.model.OnitamaSimpleMovementCard;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OnitamaGameMoveService implements BaseGameMoveService<OnitamaGameState> {
    @Autowired
    private OnitamaMovementCardService onitamaMovementCardService;

    public OnitamaGameState makeMove(OnitamaGameState state, int startX, int startY, int deltaColumns, int deltaRows, String cardName) throws Exception {
        OnitamaGameState newState = OnitamaGameState.cloneBoard(state);
        newState.getBoard()[startX + deltaRows][startY + deltaColumns] = newState.getBoard()[startX][startY];
        newState.getBoard()[startX][startY] = '-';
        List<OnitamaSimpleMovementCard> movementCards = newState.getCurrentTurn() == 'r'
                ? newState.getRedPlayerMovementCards()
                : newState.getBluePlayerMovementCards();
        movementCards.add(newState.getMiddleCard());
        newState.setMiddleCard(movementCards.stream().filter(card -> card.getName().equals(cardName)).findFirst().get());
        movementCards.remove(movementCards.stream().filter(card -> card.getName().equals(cardName)).findFirst().get());
        newState.switchTurn();
        newState.setBoard(rotateBoard(newState.getBoard()));
        checkBoardForWins(newState);
        return newState;
    }

    public boolean canMakeMove(OnitamaGameState state, int startX, int startY, int deltaColumns, int deltaRows, String cardName) {
        if (startX > 4 || startX < 0 || startY > 4 || startY < 0) {
            return false;
        }
        OnitamaMove newMove = OnitamaMove.builder().x(deltaColumns).y(deltaRows).build();
        OnitamaMovementCard relatedCard = onitamaMovementCardService.getCardFromName(cardName);
        if (Objects.isNull(relatedCard)) {
            return false;
        }
        if (!relatedCard.getMovesAvailable().contains(newMove)) {
            return false;
        }
        if (Character.toLowerCase(state.getBoard()[startX][startY]) != state.getCurrentTurn()) {
            return false;
        }
        if (startY + deltaColumns > 4 || startX + deltaRows > 4 || startY + deltaColumns < 0 || startX + deltaRows < 0) {
            return false;
        }
        return Character.toLowerCase(state.getBoard()[startX + deltaRows][startY + deltaColumns]) != state.getCurrentTurn();
    }

    @SneakyThrows
    public List<OnitamaGameState> possibleNextBoards(OnitamaGameState gameState) {
        List<OnitamaGameState> possibleNextBoards = new ArrayList<>();
        List<Point> piecePositions = new ArrayList<>();
        char currentTurn = gameState.getCurrentTurn();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (currentTurn == Character.toLowerCase(gameState.getBoard()[i][j])) {
                    piecePositions.add(new Point(i, j));
                }
            }
        }
        List<OnitamaSimpleMovementCard> movementCards = currentTurn == 'r'
                ? gameState.getRedPlayerMovementCards()
                : gameState.getBluePlayerMovementCards();
        for (OnitamaSimpleMovementCard movementCard : movementCards) {
            for (OnitamaMove move : movementCard.getMovesAvailable()) {
                for (Point piecePosition : piecePositions) {
                    if (canMakeMove(gameState, piecePosition.x, piecePosition.y, move.getX(), move.getY(), movementCard.getName())) {
                        possibleNextBoards.add(makeMove(gameState, piecePosition.x, piecePosition.y, move.getX(), move.getY(), movementCard.getName()));
                    }
                }
            }
        }
        return possibleNextBoards;
    }

    public BoardGameScore checkBoardForWins(OnitamaGameState gameState) {
        if (gameState.getCurrentTurn() == 'r' && gameState.getBoard()[0][2] == 'B' || gameState.getCurrentTurn() == 'b' && gameState.getBoard()[4][2] == 'B') {
            gameState.setBoardGameScore(BoardGameScore.BLUE_WIN);
            return BoardGameScore.BLUE_WIN;
        }
        if (gameState.getCurrentTurn() == 'b' && gameState.getBoard()[0][2] == 'R' || gameState.getCurrentTurn() == 'r' && gameState.getBoard()[4][2] == 'R') {
            gameState.setBoardGameScore(BoardGameScore.RED_WIN);
            return BoardGameScore.RED_WIN;
        }
        boolean redMasterExists = false;
        boolean blueMasterExists = false;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (gameState.getBoard()[i][j] == 'R') {
                    redMasterExists = true;
                }
                if (gameState.getBoard()[i][j] == 'B') {
                    blueMasterExists = true;
                }
            }
        }
        if (!redMasterExists && !blueMasterExists) {
            return BoardGameScore.INVALID_BOARD;
        }
        if (!redMasterExists) {
            return BoardGameScore.BLUE_WIN;
        }
        if (!blueMasterExists) {
            return BoardGameScore.RED_WIN;
        }
        return BoardGameScore.UNDETERMINED;
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
}
