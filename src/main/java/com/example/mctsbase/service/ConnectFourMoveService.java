package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.ConnectFourGameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ConnectFourMoveService implements BaseGameMoveService<ConnectFourGameState> {
    public ConnectFourGameState makeMove(ConnectFourGameState board, int column, char color) throws Exception {
        if (!canMakeMove(board, column)) {
            throw new Exception("Illegal move");
        }
        ConnectFourGameState newBoard = ConnectFourGameState.cloneBoard(board);
        int highestEmptyRowInColumn = 0;
        while (highestEmptyRowInColumn < 6) {
            if (newBoard.getBoard()[highestEmptyRowInColumn][column] == '-') {
                newBoard.getBoard()[highestEmptyRowInColumn][column] = color;
                break;
            }
            highestEmptyRowInColumn++;
        }
        newBoard.switchTurn();
        newBoard.setBoardGameScore(checkBoardForWins(newBoard));
        return newBoard;
    }

    public boolean canMakeMove(ConnectFourGameState board, int column) {
        return board.getBoard()[5][column] == '-';
    }

    public List<Integer> getAllLegalMoves(ConnectFourGameState board) {
        List <Integer> legalMoves = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (canMakeMove(board, i)) {
                legalMoves.add(i);
            }
        }
        return legalMoves;
    }

    public List<ConnectFourGameState> possibleNextBoards(ConnectFourGameState board) {
        if (!BoardGameScore.UNDETERMINED.equals(board.getBoardGameScore())) {
            return new ArrayList<>();
        }
        List<ConnectFourGameState> possibleNextBoards = new ArrayList<>();
        for (Integer col : getAllLegalMoves(board)) {
            try {
                ConnectFourGameState tempBoard = ConnectFourGameState.cloneBoard(board);
                possibleNextBoards.add(makeMove(tempBoard, col, tempBoard.getCurrentTurn()));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return possibleNextBoards;
    }

    public BoardGameScore checkBoardForWins(ConnectFourGameState board) {
        boolean redWin = false;
        boolean yellowWin = false;
        char[][] currentBoard = board.getBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                if (j <= 3) {
                    if (checkDiagonalRight(currentBoard, i, j) || checkHorizontalRight(currentBoard, i, j)) {
                        if (currentBoard[i][j] == 'r') {
                            redWin = true;
                        }
                        if (currentBoard[i][j] == 'y') {
                            yellowWin = true;
                        }
                    }
                }
                if (j >= 3) {
                    if (checkDiagonalLeft(currentBoard, i, j) || checkHorizontalLeft(currentBoard, i, j)) {
                        if (currentBoard[i][j] == 'r') {
                            redWin = true;
                        }
                        if (currentBoard[i][j] == 'y') {
                            yellowWin = true;
                        }
                    }
                }
                if (checkVertical(currentBoard, i, j)) {
                    if (currentBoard[i][j] == 'r') {
                        redWin = true;
                    }
                    if (currentBoard[i][j] == 'y') {
                        yellowWin = true;
                    }
                }
            }
        }
        for (int i = 3; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                if (checkHorizontalRight(currentBoard, i, j)) {
                    if (currentBoard[i][j] == 'r') {
                        redWin = true;
                    }
                    if (currentBoard[i][j] == 'y') {
                        yellowWin = true;
                    }
                }
            }
        }
        if (redWin && yellowWin) {
            return BoardGameScore.INVALID_BOARD;
        }
        if (redWin) {
            return BoardGameScore.RED_WIN;
        }
        if (yellowWin) {
            return BoardGameScore.YELLOW_WIN;
        }
        if (getAllLegalMoves(board).isEmpty()) {
            return BoardGameScore.TIE;
        }
        return BoardGameScore.UNDETERMINED;
    }

    private boolean checkVertical(char[][] board, int i, int j) {
        return areFourSameColor(board[i][j], board[i + 1][j], board[i + 2][j], board[i + 3][j]);
    }

    private boolean checkHorizontalLeft(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i][j - 1], board[i][j - 2], board[i][j - 3]);
    }

    private boolean checkHorizontalRight(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i][j + 1], board[i][j + 2], board[i][j + 3]);
    }

    private boolean checkDiagonalLeft(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i + 1][j - 1], board[i + 2][j - 2], board[i + 3][j - 3]);
    }

    private boolean checkDiagonalRight(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i + 1][j + 1], board[i + 2][j + 2], board[i + 3][j + 3]);
    }

    private boolean areFourSameColor(char a, char b, char c, char d) {
        return a == b && a == c && a == d;
    }
}
