package com.example.mctsbase.service;

import com.example.mctsbase.enums.ConnectFourScore;
import com.example.mctsbase.model.ConnectFourBoard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectFourService {
    public void initializeBoard(ConnectFourBoard board) {
        char[][] newBoard = new char[6][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                newBoard[i][j] = '-';
            }
        }
        board.setBoard(newBoard);
    }

    public void printBoard(ConnectFourBoard board) {
        for (int i = 5; i >= 0; i--) {
            log.info(String.join(" ", new String(board.getBoard()[i]).split("")));
        }
    }

    public ConnectFourScore checkBoardForWins(ConnectFourBoard board) {
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
        if (redWin && yellowWin) {
            return ConnectFourScore.INVALID_BOARD;
        }
        if (redWin) {
            return ConnectFourScore.RED_WIN;
        }
        if (yellowWin) {
            return ConnectFourScore.YELLOW_WIN;
        }
        return ConnectFourScore.TIE;
    }

    public boolean checkVertical(char[][] board, int i, int j) {
        return areFourSameColor(board[i][j], board[i + 1][j], board[i + 2][j], board[i + 3][j]);
    }

    public boolean checkHorizontalLeft(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i][j - 1], board[i][j - 2], board[i][j - 3]);
    }

    public boolean checkHorizontalRight(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i][j + 1], board[i][j + 2], board[i][j + 3]);
    }

    public boolean checkDiagonalLeft(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i + 1][j - 1], board[i + 2][j - 2], board[i + 3][j - 3]);
    }

    public boolean checkDiagonalRight(char[][]  board, int i, int j) {
        return areFourSameColor(board[i][j], board[i + 1][j + 1], board[i + 2][j + 2], board[i + 3][j + 3]);
    }

    public boolean areFourSameColor(char a, char b, char c, char d) {
        return a == b && a == c && a == d;
    }
}
