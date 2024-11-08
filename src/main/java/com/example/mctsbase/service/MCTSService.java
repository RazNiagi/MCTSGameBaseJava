package com.example.mctsbase.service;

import com.example.mctsbase.enums.ConnectFourScore;
import com.example.mctsbase.model.ConnectFourBoard;
import com.example.mctsbase.model.MCTSNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Service
public class MCTSService {
    private final double explorationConstant = Math.sqrt(2.0);
    private int maxDepth = 0;
    @Autowired
    private ConnectFourMoveService connectFourMoveService;

    // TODO: Make a function for monte carlo tree search
    public ConnectFourBoard connectFourMCTS(MCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        long startTime = System.currentTimeMillis();
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;

        // While resources left and tree not fully mapped
        while (System.currentTimeMillis() - startTime < maxTime) {
            MCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
        }
        return bestChild(mctsNode).getBoard();
    }

    // Get the evaluation of the node provided
    public double getNodeEval(MCTSNode mctsNode) {
        double firstTerm = mctsNode.getScore() / mctsNode.getTimesVisited();
        double secondTermUnderSqrt = Math.log(mctsNode.getTimesVisited());
        if (mctsNode.getParent() != null) {
            secondTermUnderSqrt = Math.log(Math.max(1, mctsNode.getParent().getTimesVisited())) / mctsNode.getTimesVisited();
        }
        double secondTerm = explorationConstant * Math.sqrt(secondTermUnderSqrt);
        double returnValue = firstTerm + secondTerm;
        mctsNode.setCurrentValue(returnValue);
        return returnValue;
    }

    public void updateNode(MCTSNode mctsNode, ConnectFourScore score) {
        // Update the evaluation of the node
        if ((score.equals(ConnectFourScore.RED_WIN) && mctsNode.getBoard().getCurrentTurn() == 'r') || (score.equals(ConnectFourScore.YELLOW_WIN) && mctsNode.getBoard().getCurrentTurn() == 'y')) {
            mctsNode.setScore(mctsNode.getScore() - 1);
        }
        if ((score.equals(ConnectFourScore.RED_WIN) && mctsNode.getBoard().getCurrentTurn() == 'y') || (score.equals(ConnectFourScore.YELLOW_WIN) && mctsNode.getBoard().getCurrentTurn() == 'r')) {
            mctsNode.setScore(mctsNode.getScore() + 1);
        }
        if (score.equals(ConnectFourScore.TIE)) {
            mctsNode.setScore(mctsNode.getScore() + 0.5);
        }
        mctsNode.setTimesVisited(mctsNode.getTimesVisited() + 1);
        mctsNode.setCurrentValue(getNodeEval(mctsNode));
    }

    // Do this while all the child nodes are explored, otherwise swap to rollout
    public MCTSNode traverse(MCTSNode mctsNode) {
        // while fully expanded node, traverse to the node with the best eval
        while (mctsNode.getUnexplored().isEmpty()) {
            MCTSNode newNode = bestChild(mctsNode);
            if (newNode != null) {
                mctsNode = newNode;
            } else {
                break;
            }
        }
        return mctsNode;
    }

    // Roll out to max depth, win, or resources left
    public void rollout(MCTSNode mctsNode) {
        while (!isNodeTerminal(mctsNode)) {
            mctsNode = rolloutPolicy(mctsNode);
        }
        backPropagate(mctsNode, mctsNode.getBoard().getConnectFourScore());
    }

    public MCTSNode rolloutPolicy(MCTSNode mctsNode) {
        Collections.shuffle(mctsNode.getUnexplored());
        ConnectFourBoard newBoard = mctsNode.getUnexplored().getFirst();
        MCTSNode newNode = MCTSNode.builder()
                .root(false)
                .score(0.0)
                .currentValue(0.0)
                .depth(mctsNode.getDepth() + 1)
                .board(newBoard)
                .children(new ArrayList<>())
                .timesVisited(0)
                .parent(mctsNode)
                .unexplored(new ArrayList<>(connectFourMoveService.possibleNextBoards(newBoard)))
                .build();
        mctsNode.getUnexplored().removeFirst();
        mctsNode.getChildren().add(newNode);
        return newNode;
    }

    public boolean isNodeTerminal(MCTSNode mctsNode) {
        // Check for win, tie, or if equals or exceeds max depth
        if (!ConnectFourScore.UNDETERMINED.equals(mctsNode.getBoard().getConnectFourScore())) {
            return true;
        }
        return maxDepth != 0 && mctsNode.getDepth() > maxDepth;
    }

    // Returns the best child node from the available ones
    public MCTSNode bestChild(MCTSNode mctsNode) {
        // return the best move here
        double maxVal = Double.NEGATIVE_INFINITY;
        MCTSNode returnNode = null;
        for (MCTSNode child : mctsNode.getChildren()) {
            if (child.getCurrentValue() > maxVal) {
                maxVal = child.getCurrentValue();
                returnNode = child;
            }
        }
        return returnNode;
    }

    // Update the node and then move up the tree, updating all the nodes along the way
    public void backPropagate(MCTSNode mctsNode, ConnectFourScore score) {
        updateNode(mctsNode, score);
        if (!mctsNode.isRoot() && mctsNode.getParent() != null) {
            backPropagate(mctsNode.getParent(), score);
        }
    }
}
