package com.example.mctsbase.model;

import com.example.mctsbase.enums.BoardGameScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BaseGameState {
    protected char[][] board;
    protected char currentTurn;
    @Builder.Default
    protected BoardGameScore boardGameScore = BoardGameScore.UNDETERMINED;
}
