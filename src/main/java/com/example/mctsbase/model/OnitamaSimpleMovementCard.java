package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaSimpleMovementCard {
    private List<OnitamaMove> movesAvailable;
    private List<OnitamaMove> studentOnlyMoves;
    private List<OnitamaMove> masterOnlyMoves;
    private String name;
}
