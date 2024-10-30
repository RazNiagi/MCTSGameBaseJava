package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ConnectFourBoard {
    private char[][] board = new char[6][7];
    private char currentTurn = 'r';

    public void switchTurn() {
        this.currentTurn = this.currentTurn == 'r' ? 'y' : 'r';
    }
}
