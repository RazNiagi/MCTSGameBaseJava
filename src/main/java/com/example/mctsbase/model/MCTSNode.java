package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MCTSNode {
    private boolean root;
    private int depth;
    private int timesVisited;
    private double currentValue;
    private ConnectFourBoard board;
    private List<MCTSNode> children;
    private MCTSNode parent;
    private List<ConnectFourBoard> unexplored;
    private double score;
}
