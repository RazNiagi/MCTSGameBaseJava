package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaGameState {
    private char[][] board;
    private List<OnitamaSimpleMovementCard> bluePlayerMovementCards = new ArrayList<>();
    private List<OnitamaSimpleMovementCard> redPlayerMovementCards = new ArrayList<>();
    private OnitamaSimpleMovementCard middleCard = null;
    private char currentTurn;
}
