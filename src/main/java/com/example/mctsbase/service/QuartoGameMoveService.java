package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.QuartoGameMove;
import com.example.mctsbase.model.QuartoGameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class QuartoGameMoveService implements BaseGameMoveService<QuartoGameState> {
    // Might be better to separate the choosing of a piece and placing of a piece into two different methods
    // This would also mean splitting the QuartoGameMove back out into a placePiece and choosePiece move
    public QuartoGameState makeMove(QuartoGameState gameState, QuartoGameMove move) throws Exception {
        if (!canMakeMove(gameState, move.getRow(), move.getCol())) {
            throw new Exception("Illegal move");
        }
        gameState.getBoard()[move.getRow()][move.getCol()] = gameState.getSelectedPiece();
        gameState.setSelectedPiece('-');
        BoardGameScore newScore = checkBoardForWins(gameState);
        gameState.setBoardGameScore(newScore);
        if (!newScore.equals(BoardGameScore.UNDETERMINED)) {
            return gameState;
        }
        return gameState;
    }

    public QuartoGameState selectPiece(QuartoGameState gameState, char piece) throws Exception {
        if (gameState.getAvailablePieces().contains(piece)) {
            gameState.getAvailablePieces().remove(Character.valueOf(piece));
            gameState.setSelectedPiece(piece);
            gameState.switchTurn();
            return gameState;
        } else {
            throw new Exception("Illegal piece selection");
        }
    }

    public boolean canMakeMove(QuartoGameState gameState, int row, int col) {
        return gameState.getBoard()[row][col] == '-'
                && gameState.getSelectedPiece() != '-'
                && gameState.getBoardGameScore().equals(com.example.mctsbase.enums.BoardGameScore.UNDETERMINED);
    }

    public List<QuartoGameMove> getAllLegalMoves(QuartoGameState gameState) {
        List<QuartoGameMove> legalMoves = new ArrayList<>();
        // Implementation for generating all legal moves goes here
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (gameState.getBoard()[row][col] == '-') {
                    legalMoves.add(new QuartoGameMove(row, col));
                }
            }
        }
        return legalMoves;
    }

    public List<QuartoGameState> possibleNextBoards(QuartoGameState gameState) {
        if (!gameState.getBoardGameScore().equals(com.example.mctsbase.enums.BoardGameScore.UNDETERMINED)) {
            return List.of();
        }
        List<QuartoGameState> possibleNextBoards = new ArrayList<>();
        for (QuartoGameMove move : getAllLegalMoves(gameState)) {
            try {
                QuartoGameState nextState = QuartoGameState.cloneBoard(gameState);
                nextState = makeMove(nextState, move);
                for (char piece : nextState.getAvailablePieces()) {
                    QuartoGameState nextStateClone = QuartoGameState.cloneBoard(nextState);
                    QuartoGameState stateAfterPieceSelection = selectPiece(nextStateClone, piece);
                    possibleNextBoards.add(stateAfterPieceSelection);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return possibleNextBoards;
    }

    public BoardGameScore checkBoardForWins(QuartoGameState gameState) {
        // Implementation for checking the board for wins goes here
        if (checkAllRows(gameState) || checkAllColumns(gameState) || checkBothDiagonals(gameState)) {
            return currentPlayerWin(gameState);
        }
        if (gameState.isAdvancedMode() && checkAllSquaresForAdvancedWin(gameState)) {
            return currentPlayerWin(gameState);
        }
        if (gameState.getAvailablePieces().isEmpty()) {
            return BoardGameScore.TIE;
        }
        return com.example.mctsbase.enums.BoardGameScore.UNDETERMINED;
    }

    private BoardGameScore currentPlayerWin(QuartoGameState gameState) {
        return gameState.getCurrentTurn() == '1' ? BoardGameScore.PLAYER_1_WIN : BoardGameScore.PLAYER_2_WIN;
    }

    private boolean checkAllRows(QuartoGameState gameState) {
        // Check all rows for a win
        for (int i = 0; i < 4; i++) {
            if (checkRow(gameState)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRow(QuartoGameState gameState) {
        // Check a single row for a win
        char[][] board = gameState.getBoard();
        if (board[0][0] != '-' && board[0][1] != '-' && board[0][2] != '-' && board[0][3] != '-') {
            return checkIfAnyCommonAttributes(
                    convertCharToByteUnderSixteen(board[0][0]),
                    convertCharToByteUnderSixteen(board[0][1]),
                    convertCharToByteUnderSixteen(board[0][2]),
                    convertCharToByteUnderSixteen(board[0][3])
            );
        }
        return false;
    }

    private boolean checkAllColumns(QuartoGameState gameState) {
        // Check all columns for a win
        for (int i = 0; i < 4; i++) {
            if (checkColumn(gameState)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkColumn(QuartoGameState gameState) {
        // Check a single column for a win
        char[][] board = gameState.getBoard();
        if (board[0][0] != '-' && board[1][0] != '-' && board[2][0] != '-' && board[3][0] != '-') {
            return checkIfAnyCommonAttributes(
                    convertCharToByteUnderSixteen(board[0][0]),
                    convertCharToByteUnderSixteen(board[1][0]),
                    convertCharToByteUnderSixteen(board[2][0]),
                    convertCharToByteUnderSixteen(board[3][0])
            );
        }
        return false;
    }

    private boolean checkBothDiagonals(QuartoGameState gameState) {
        // Check both diagonals for a win
        return checkDiagonalLeft(gameState) || checkDiagonalRight(gameState);
    }

    private boolean checkDiagonalLeft(QuartoGameState gameState) {
        // Check left diagonal for a win
        char[][] board = gameState.getBoard();
        if (board[0][0] != '-' && board[1][1] != '-' && board[2][2] != '-' && board[3][3] != '-') {
            return checkIfAnyCommonAttributes(
                    convertCharToByteUnderSixteen(board[0][0]),
                    convertCharToByteUnderSixteen(board[1][1]),
                    convertCharToByteUnderSixteen(board[2][2]),
                    convertCharToByteUnderSixteen(board[3][3])
            );
        }
        return false;
    }

    private boolean checkDiagonalRight(QuartoGameState gameState) {
        // Check right diagonal for a win
        char[][] board = gameState.getBoard();
        if (board[0][3] != '-' && board[1][2] != '-' && board[2][1] != '-' && board[3][0] != '-') {
            return checkIfAnyCommonAttributes(
                convertCharToByteUnderSixteen(board[0][3]),
                convertCharToByteUnderSixteen(board[1][2]),
                convertCharToByteUnderSixteen(board[2][1]),
                convertCharToByteUnderSixteen(board[3][0])
            );
        }
        return false;
    }

    private boolean checkAllSquaresForAdvancedWin(QuartoGameState gameState) {
        // Check all 2x2 squares for a win
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (checkSquare(gameState, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSquare(QuartoGameState gameState, int row, int col) {
        // Check a single 2x2 square for a win
        char[][] board = gameState.getBoard();
        if (board[row][col] != '-' && board[row][col + 1] != '-' &&
            board[row + 1][col] != '-' && board[row + 1][col + 1] != '-') {
            return checkIfAnyCommonAttributes(
                convertCharToByteUnderSixteen(board[row][col]),
                convertCharToByteUnderSixteen(board[row][col + 1]),
                convertCharToByteUnderSixteen(board[row + 1][col]),
                convertCharToByteUnderSixteen(board[row + 1][col + 1])
            );
        }
        return false;
    }

    // This method checks if there are any common attributes among four pieces
    // This is only used to check for wins as we do not need to check for losing pieces
    private boolean checkIfAnyCommonAttributes(byte b1, byte b2, byte b3, byte b4) {
        return (b1 & b2 & b3 & b4) != 0 || (flipLowerFourBytes(b1) & flipLowerFourBytes(b2) & flipLowerFourBytes(b3) & flipLowerFourBytes(b4)) != 0;
    }

    // This method converts a character representing a piece attribute into a byte value under 16
    private byte convertCharToByteUnderSixteen(char c) {
        // Subtract 'A' to get values from 0 to 15 for characters 'A' to 'P'
        return (byte) (c - 'A');
    }

    // This method gets the xor of a byte with 15 (0x0F)
    // This will flip the lower 4 bits of the byte so we can check for common attributes that might all be 0s
    private byte flipLowerFourBytes(byte b) {
        return (byte) (b ^ 0x0F);
    }
}
