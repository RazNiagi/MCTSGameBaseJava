package com.example.mctsbase.service;

import com.example.mctsbase.model.ConnectFourBoard;
import org.springframework.stereotype.Service;

@Service
public class ConnectFourMoveService {
    public ConnectFourBoard makeMove(ConnectFourBoard board, int column, char color) throws Exception {
        if (!canMakeMove(board, column)) {
            throw new Exception("Illegal move");
        }
        ConnectFourBoard newBoard = board.toBuilder().build();
        int highestEmptyRowInColumn = 0;
        while (highestEmptyRowInColumn < 6) {
            if (newBoard.getBoard()[highestEmptyRowInColumn][column] == '-') {
                newBoard.getBoard()[highestEmptyRowInColumn][column] = color;
                break;
            }
            highestEmptyRowInColumn++;
        }
        return newBoard;
    }

    public boolean canMakeMove(ConnectFourBoard board, int column) {
        return board.getBoard()[5][column] == '-';
    }
}
