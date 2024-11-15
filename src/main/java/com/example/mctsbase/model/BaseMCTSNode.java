package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BaseMCTSNode {
    private boolean root;
    private int depth;
    private int timesVisited;
    private double currentValue;
    private double score;
}
