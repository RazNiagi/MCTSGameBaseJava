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
        canMakeMove(gameState, move.getRow(), move.getCol());
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
            throw new Exception("Illegal piece selection: piece " + piece + " is not available");
        }
    }

    public void canMakeMove(QuartoGameState gameState, int row, int col) throws Exception {
        if (row < 0 || row >= gameState.getBoard().length || col < 0 || col >= gameState.getBoard()[0].length) {
            throw new Exception("Illegal move: position out of bounds (" + row + ", " + col + ")");
        }
        if (gameState.getBoard()[row][col] != '-') {
            throw new Exception("Illegal move: target position (" + row + ", " + col + ") is already occupied");
        }
        if (gameState.getSelectedPiece() == '-') {
            throw new Exception("Illegal move: no piece selected");
        }
        if (!gameState.getBoardGameScore().equals(BoardGameScore.UNDETERMINED)) {
            throw new Exception("Illegal move: game already finished");
        }
    }

    public List<QuartoGameMove> getAllLegalMoves(QuartoGameState gameState) {
        List<QuartoGameMove> legalMoves = new ArrayList<>();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (gameState.getBoard()[row][col] == '-') {
                    legalMoves.add(new QuartoGameMove(row, col));
                }
            }
        }
        return legalMoves;
    }

    @Override
    public List<QuartoGameState> possibleNextBoards(QuartoGameState gameState) {
        if (!gameState.getBoardGameScore().equals(BoardGameScore.UNDETERMINED)) {
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
                log.error("Error generating possible boards for move {}: {}", move, e.getMessage(), e);
            }
        }
        return possibleNextBoards;
    }
    
    // Check if giving a specific piece to the opponent would allow them to win immediately
    public boolean isLosingPiece(QuartoGameState gameState, char piece) {
        // Try placing this piece in every empty position to see if any leads to a win
        for (QuartoGameMove move : getAllLegalMoves(gameState)) {
            try {
                QuartoGameState testState = QuartoGameState.cloneBoard(gameState);
                testState.setSelectedPiece(piece);
                testState = makeMove(testState, move);
                if (!testState.getBoardGameScore().equals(BoardGameScore.UNDETERMINED)) {
                    // This piece can create a win - it's a losing piece to give away
                    return true;
                }
            } catch (Exception e) {
                log.error("Error checking losing piece: {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public BoardGameScore checkBoardForWins(QuartoGameState gameState) {
        if (checkAllRows(gameState) || checkAllColumns(gameState) || checkBothDiagonals(gameState)) {
            return currentPlayerWin(gameState);
        }
        if (gameState.isAdvancedMode() && checkAllSquaresForAdvancedWin(gameState)) {
            return currentPlayerWin(gameState);
        }
        if (gameState.getAvailablePieces().isEmpty()) {
            return BoardGameScore.TIE;
        }
        return BoardGameScore.UNDETERMINED;
    }

    private BoardGameScore currentPlayerWin(QuartoGameState gameState) {
        return gameState.getCurrentTurn() == '1' ? BoardGameScore.PLAYER_1_WIN : BoardGameScore.PLAYER_2_WIN;
    }

    private boolean checkAllRows(QuartoGameState gameState) {
        // Check all rows for a win
        for (int i = 0; i < 4; i++) {
            if (checkRow(gameState, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRow(QuartoGameState gameState, int row) {
        // Check a single row for a win
        char[][] board = gameState.getBoard();
        if (board[row][0] != '-' && board[row][1] != '-' && board[row][2] != '-' && board[row][3] != '-') {
            return checkIfAnyCommonAttributes(
                    convertCharToByteUnderSixteen(board[row][0]),
                    convertCharToByteUnderSixteen(board[row][1]),
                    convertCharToByteUnderSixteen(board[row][2]),
                    convertCharToByteUnderSixteen(board[row][3])
            );
        }
        return false;
    }

    private boolean checkAllColumns(QuartoGameState gameState) {
        // Check all columns for a win
        for (int i = 0; i < 4; i++) {
            if (checkColumn(gameState, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkColumn(QuartoGameState gameState, int col) {
        // Check a single column for a win
        char[][] board = gameState.getBoard();
        if (board[0][col] != '-' && board[1][col] != '-' && board[2][col] != '-' && board[3][col] != '-') {
            return checkIfAnyCommonAttributes(
                    convertCharToByteUnderSixteen(board[0][col]),
                    convertCharToByteUnderSixteen(board[1][col]),
                    convertCharToByteUnderSixteen(board[2][col]),
                    convertCharToByteUnderSixteen(board[3][col])
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
        return (b1 & b2 & b3 & b4) != 0 || (flipLowerFourBits(b1) & flipLowerFourBits(b2) & flipLowerFourBits(b3) & flipLowerFourBits(b4)) != 0;
    }

    // This method converts a character representing a piece attribute into a byte value under 16
    private byte convertCharToByteUnderSixteen(char c) {
        // Subtract 'A' to get values from 0 to 15 for characters 'A' to 'P'
        return (byte) (c - 'A');
    }

    // This method gets the xor of a byte with 15 (0x0F)
    // This will flip the lower 4 bits of the byte so we can check for common attributes that might all be 0s
    private byte flipLowerFourBits(byte b) {
        return (byte) (b ^ 0x0F);
    }
}
