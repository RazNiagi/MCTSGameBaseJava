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
    protected List<BaseMCTSNode> nodes = Collections.synchronizedList(new ArrayList<>());
    protected BaseGameMoveService moveService;

    public BaseMCTSService() {
        this(null);
    }

    public BaseMCTSService(BaseGameMoveService moveService) {
        this.moveService = moveService;
    }

    public BaseMCTSNode monteCarloTreeSearchWithPruning(BaseMCTSNode mctsNode, Integer maxTime, boolean pruned) {
        long endTime = System.currentTimeMillis() + maxTime;

        // While resources left and tree not fully mapped
        while (!pruned && System.currentTimeMillis() < endTime) {
            BaseMCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
            pruned = pruneLosingBranches(mctsNode);
        }

        return mctsLoop(mctsNode, endTime);
    }

    public BaseMCTSNode mctsLoop(BaseMCTSNode mctsNode, long endTime) {
        // While resources left and tree not fully mapped
        while (System.currentTimeMillis() < endTime) {
            BaseMCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
        }
        log.info("times visited: {}", mctsNode.getTimesVisited());
        return mostVisitedChild(mctsNode);
    }

    public BaseMCTSNode monteCarloTreeSearch(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;
        long endTime = System.currentTimeMillis() + maxTime;

        return mctsLoop(mctsNode, endTime);
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
        char currentTurn = mctsNode.getBoard().getCurrentTurn();

        if ((score.equals(BoardGameScore.RED_WIN) && currentTurn == 'y') || 
            (score.equals(BoardGameScore.YELLOW_WIN) && currentTurn == 'r')) {
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
        if (mctsNode.getChildren().isEmpty()) {
            return mctsNode;
        }
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

    public boolean pruneLosingBranches(BaseMCTSNode mctsNode) {
        boolean areAllChildrenFullyExpanded = mctsNode.getUnexplored().isEmpty() && mctsNode.getChildren().stream().filter(child -> child.getUnexplored().isEmpty()).toList().size() == mctsNode.getChildren().size();
        if (!areAllChildrenFullyExpanded) {
            return false;
        }
        if (mctsNode.getChildren().size() == 1) {
            return true;
        }
        List<Boolean> shouldBePruned = new ArrayList<>();
        mctsNode.getChildren().forEach(child -> {
            if (mctsNode.getBoard().getCurrentTurn() == 'r') {
                shouldBePruned.add(child.getChildren().stream().anyMatch(childChild -> childChild.getBoard().getBoardGameScore() == BoardGameScore.BLUE_WIN));
            } else {
                shouldBePruned.add(child.getChildren().stream().anyMatch(childChild -> childChild.getBoard().getBoardGameScore() == BoardGameScore.RED_WIN));
            }
        });
        List<BaseMCTSNode> prunedChildren = new ArrayList<>();
        for (int i = shouldBePruned.size() - 1; i >= 0; i--) {
            if (shouldBePruned.get(i) && mctsNode.getChildren().size() > 1) {
                prunedChildren.add(mctsNode.getChildren().get(i));
                mctsNode.getChildren().remove(i);
            }
        }
        if (!prunedChildren.isEmpty()) {
            mctsNode.setPrunedChildren(prunedChildren);
        }
        return true;
    }

    public BaseMCTSNode monteCarloTreeSearchWithLevel(BaseMCTSNode mctsNode, Integer level) {
        if (mctsNode.getChildren().size() == 1) {
            return mctsNode.getChildren().getFirst();
        }
        int chanceForRandom = (int)(Math.random() * 100) + 1;
        log.info("Making move for board with level {}", level);
        int maxTime = level * 200;
        maxDepth = 20;
        boolean pruned = level <= 7;
        BaseMCTSNode returnNode = monteCarloTreeSearchWithPruning(mctsNode, maxTime, pruned);
        if (chanceForRandom <= 10 * level) {
            return returnNode;
        }
        log.info("Choosing random move within top {}% of other moves and best", level * 10);
        BaseMCTSNode parentNode = returnNode.getParent();
        return getRandomTopPercentileChild(parentNode, level);
    }

    public BaseMCTSNode getRandomTopPercentileChild(BaseMCTSNode mctsNode, Integer level) {
        List<BaseMCTSNode> childrenAndPruned = new ArrayList<>();
        if (Objects.nonNull(mctsNode.getPrunedChildren())) {
            childrenAndPruned.addAll(mctsNode.getPrunedChildren());
        }
        childrenAndPruned.addAll(mctsNode.getChildren());
        if (childrenAndPruned.size() == 1) {
            return childrenAndPruned.getFirst();
        }
        List<BaseMCTSNode> children = childrenAndPruned.stream().sorted(Comparator.comparingInt(BaseMCTSNode::getTimesVisited)).toList().reversed();
        double percentile = (double) (100 - (10 * level)) / 100;
        int numNodesToConsider = (int) Math.ceil((children.size() - 1) * percentile) + 1;
        Random rand = new Random();
        return children.get(rand.nextInt(numNodesToConsider));
    }
}
