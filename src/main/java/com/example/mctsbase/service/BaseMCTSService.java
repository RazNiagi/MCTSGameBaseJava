package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.BaseGameState;
import com.example.mctsbase.model.BaseMCTSNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class BaseMCTSService<T extends BaseGameState> {
    protected final double explorationConstant = Math.sqrt(2.0);
    protected int maxDepth = 0;
    protected List<BaseMCTSNode> nodes = new ArrayList<>();
    protected BaseGameMoveService moveService;

    public BaseMCTSService() {
        this(null);
    }

    public BaseMCTSService(BaseGameMoveService moveService) {
        this.moveService = moveService;
    }

    public BaseMCTSNode monteCarloTreeSearch(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        long startTime = System.currentTimeMillis();
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;

        // While resources left and tree not fully mapped
        while (System.currentTimeMillis() - startTime < maxTime) {
            BaseMCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
        }
        log.info("times visited: " + mctsNode.getTimesVisited());
        return mostVisitedChild(mctsNode);
    }

    // Get the evaluation of the node provided
    public double getNodeEval(BaseMCTSNode mctsNode) {
        double firstTerm = mctsNode.getScore() / mctsNode.getTimesVisited();
        double secondTermUnderSqrt = Math.log(mctsNode.getTimesVisited());
        if (mctsNode.getParent() != null) {
            secondTermUnderSqrt = Math.log(Math.max(1, mctsNode.getParent().getTimesVisited())) / mctsNode.getTimesVisited();
        }
        double secondTerm = explorationConstant * Math.sqrt(secondTermUnderSqrt);
        return firstTerm + secondTerm;
    }

    public void updateNode(BaseMCTSNode mctsNode, BoardGameScore score) {
        // Update the evaluation of the node
        if ((score.equals(BoardGameScore.RED_WIN) && mctsNode.getBoard().getCurrentTurn() == 'y') || (score.equals(BoardGameScore.YELLOW_WIN) && mctsNode.getBoard().getCurrentTurn() == 'r')) {
            mctsNode.setScore(mctsNode.getScore() + 1);
        }
        if (score.equals(BoardGameScore.TIE)) {
            mctsNode.setScore(mctsNode.getScore() + 0.5);
        }
        mctsNode.setTimesVisited(mctsNode.getTimesVisited() + 1);
        updateNodeEval(mctsNode);
    }

    public void updateNodeEval(BaseMCTSNode mctsNode) {
        mctsNode.setCurrentValue(getNodeEval(mctsNode));
    }

    // Do this while all the child nodes are explored, otherwise swap to rollout
    public BaseMCTSNode traverse(BaseMCTSNode mctsNode) {
        // while fully expanded node, traverse to the node with the best eval
        while (mctsNode.getUnexplored().isEmpty()) {
            BaseMCTSNode newNode = bestChild(mctsNode);
            if (newNode != null) {
                mctsNode = newNode;
            } else {
                break;
            }
        }
        return mctsNode;
    }

    // Roll out to max depth, win, or resources left
    public void rollout(BaseMCTSNode mctsNode) {
        while (!isNodeTerminal(mctsNode)) {
            mctsNode = rolloutPolicy(mctsNode);
        }
        backPropagate(mctsNode, mctsNode.getBoard().getBoardGameScore());
    }

    public BaseMCTSNode rolloutPolicy(BaseMCTSNode mctsNode) {
        // Look in to moving this shuffle to when the node is created
        Collections.shuffle(mctsNode.getUnexplored());
        T newBoard = (T) mctsNode.getUnexplored().getFirst();
        BaseMCTSNode newNode = BaseMCTSNode.builder()
                .root(false)
                .score(0.0)
                .currentValue(0.0)
                .depth(mctsNode.getDepth() + 1)
                .board(newBoard)
                .children(new ArrayList<>())
                .timesVisited(0)
                .parent(mctsNode)
                .unexplored(new ArrayList<>(moveService.possibleNextBoards(newBoard)))
                .build();
        mctsNode.getUnexplored().removeFirst();
        mctsNode.getChildren().add(newNode);
        return newNode;
    }

    public boolean isNodeTerminal(BaseMCTSNode mctsNode) {
        // Check for win, tie, or if equals or exceeds max depth
        if (!BoardGameScore.UNDETERMINED.equals(mctsNode.getBoard().getBoardGameScore())) {
            return true;
        }
        if (mctsNode.getUnexplored().isEmpty()) {
            return true;
        }
        return maxDepth != 0 && mctsNode.getDepth() > maxDepth;
    }

    // Returns the best child node from the available ones
    public BaseMCTSNode bestChild(BaseMCTSNode mctsNode) {
        // return the best move here
        double maxVal = Double.NEGATIVE_INFINITY;
        BaseMCTSNode returnNode = null;
        for (BaseMCTSNode child : mctsNode.getChildren()) {
            updateNodeEval(child);
            if (child.getCurrentValue() > maxVal) {
                maxVal = child.getCurrentValue();
                returnNode = child;
            }
        }
        return returnNode;
    }

    // This is the move to make at the end of the MCTS period. Checks score instead of taking into account the exploration value;
    public BaseMCTSNode mostVisitedChild(BaseMCTSNode mctsNode) {
        int maxVisits = Integer.MIN_VALUE;
        for (BaseMCTSNode child : mctsNode.getChildren()) {
            if (child.getTimesVisited() > maxVisits) {
                maxVisits = child.getTimesVisited();
            }
        }
        int finalMaxVisits = maxVisits;
        List<BaseMCTSNode> nodesWithMaxVisits = new ArrayList<>(mctsNode.getChildren().stream().filter(child -> child.getTimesVisited() == finalMaxVisits).toList());
        nodesWithMaxVisits.sort(Comparator.comparingDouble(BaseMCTSNode::getScore).reversed());
        return nodesWithMaxVisits.getFirst();
    }

    // Update the node and then move up the tree, updating all the nodes along the way
    public void backPropagate(BaseMCTSNode mctsNode, BoardGameScore score) {
        updateNode(mctsNode, score);
        if (!mctsNode.isRoot() && mctsNode.getParent() != null) {
            backPropagate(mctsNode.getParent(), score);
        }
    }
}
