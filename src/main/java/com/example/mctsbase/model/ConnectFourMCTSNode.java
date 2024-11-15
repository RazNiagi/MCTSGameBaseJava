package com.example.mctsbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ConnectFourMCTSNode extends BaseMCTSNode {
    private ConnectFourGameState board;
    private List<ConnectFourMCTSNode> children;
    private ConnectFourMCTSNode parent;
    private List<ConnectFourGameState> unexplored;
}
