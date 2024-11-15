package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
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

    public static OnitamaSimpleMovementCard cloneCard(OnitamaSimpleMovementCard other) {
        List<OnitamaMove> newMovesAvailable = new ArrayList<>();
        for (OnitamaMove move : other.getMovesAvailable()) {
            newMovesAvailable.add(OnitamaMove.builder().x(move.getX()).y(move.getY()).build());
        }
        List<OnitamaMove> newStudentOnlyMoves = null;
        if (other.getStudentOnlyMoves() != null) {
            newStudentOnlyMoves = new ArrayList<>();
            for (OnitamaMove move : other.getStudentOnlyMoves()) {
                newStudentOnlyMoves.add(OnitamaMove.builder().x(move.getX()).y(move.getY()).build());
            }
        }
        List<OnitamaMove> newMasterOnlyMoves = null;
        if (other.getMasterOnlyMoves() != null) {
            newMasterOnlyMoves = new ArrayList<>();
            for (OnitamaMove move : other.getMasterOnlyMoves()) {
                newMasterOnlyMoves.add(OnitamaMove.builder().x(move.getX()).y(move.getY()).build());
            }
        }
        return OnitamaSimpleMovementCard.builder()
                .movesAvailable(newMovesAvailable)
                .studentOnlyMoves(newStudentOnlyMoves)
                .masterOnlyMoves(newMasterOnlyMoves)
                .name(other.getName())
                .build();
    }
}
