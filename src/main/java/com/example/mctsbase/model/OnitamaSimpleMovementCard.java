package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaSimpleMovementCard {
    private List<OnitamaMove> movesAvailable;
    private List<OnitamaMove> studentOnlyMoves;
    private List<OnitamaMove> masterOnlyMoves;
    private String name;
}
