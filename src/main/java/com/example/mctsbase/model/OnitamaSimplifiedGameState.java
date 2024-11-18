package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OnitamaSimplifiedGameState extends BaseGameState {
    @Builder.Default
    private List<String> bluePlayerMovementCards = new ArrayList<>();
    @Builder.Default
    private List<String> redPlayerMovementCards = new ArrayList<>();
    @Builder.Default
    private String middleCard = "";
    @Builder.Default
    private String boardString = "-------------------------";
}
