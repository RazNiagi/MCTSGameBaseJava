package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.ConnectFourGameState;
import com.example.mctsbase.model.MCTSNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class MCTSService {
    private final double explorationConstant = Math.sqrt(2.0);
    private int maxDepth = 0;
    private List<MCTSNode> nodes = new ArrayList<>();
    @Autowired
    private ConnectFourMoveService connectFourMoveService;

    @SneakyThrows
    public ConnectFourGameState parallelMCTS(MCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime, Integer numThreads) {
        nodes = new ArrayList<>();
        try {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numThreads; i++) {
                threads.add(new Thread(() -> connectFourMCTSForParallelization(mctsNode, maxDepthIncrease, maxTime)));
            }
            threads.forEach(Thread::start);
            for (Thread thread : threads) {
                thread.join();
            }
            MCTSNode newNode = MCTSNode.builder()
                    .root(mctsNode.isRoot())
                    .depth(mctsNode.getDepth())
                    .timesVisited(mctsNode.getTimesVisited())
                    .currentValue(mctsNode.getCurrentValue())
                    .score(mctsNode.getScore())
                    .board(ConnectFourGameState.cloneBoard(mctsNode.getBoard()))
                    .children(new ArrayList<>())
                    .parent(null)
                    .unexplored(new ArrayList<>())
                    .build();
            connectFourMoveService.possibleNextBoards(mctsNode.getBoard()).forEach(board -> newNode.getChildren().add(MCTSNode.builder()
                            .unexplored(null)
                            .parent(null)
                            .children(null)
                            .board(board)
                            .score(0)
                            .currentValue(0)
                            .depth(mctsNode.getDepth() + 1)
                            .root(false)
                            .timesVisited(0)
                    .build()));
            nodes.forEach(node -> {
                if (Objects.nonNull(node)) {
                    newNode.setTimesVisited(node.getTimesVisited() + newNode.getTimesVisited());
                    node.getChildren().forEach(child -> newNode.getChildren().stream().filter(childNode -> childNode.getBoard().equals(child.getBoard())).findFirst()
                            .ifPresent(correctChild -> correctChild.setTimesVisited(correctChild.getTimesVisited() + child.getTimesVisited())));
                }

            });
            log.info("times visited {}", newNode.getTimesVisited());
            return mostVisitedChild(newNode).getBoard();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void connectFourMCTSForParallelization(MCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        mctsNode = MCTSNode.builder()
                .root(mctsNode.isRoot())
                .depth(mctsNode.getDepth())
                .timesVisited(mctsNode.getTimesVisited())
                .currentValue(mctsNode.getCurrentValue())
                .score(mctsNode.getScore())
                .board(ConnectFourGameState.cloneBoard(mctsNode.getBoard()))
                .children(new ArrayList<>(mctsNode.getChildren()))
                .parent(null)
                .unexplored(new ArrayList<>(mctsNode.getUnexplored()))
                .build();
        long startTime = System.currentTimeMillis();
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;

        // While resources left and tree not fully mapped
        while (System.currentTimeMillis() - startTime < maxTime) {
            MCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
        }

        nodes.add(mctsNode);
    }

    public ConnectFourGameState connectFourMCTS(MCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        long startTime = System.currentTimeMillis();
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;

        // While resources left and tree not fully mapped
        while (System.currentTimeMillis() - startTime < maxTime) {
            MCTSNode leaf = traverse(mctsNode);
            rollout(leaf);

        }
        log.info("times visited: " + mctsNode.getTimesVisited());
        return mostVisitedChild(mctsNode).getBoard();
    }

    // Get the evaluation of the node provided
    public double getNodeEval(MCTSNode mctsNode) {
        double firstTerm = mctsNode.getScore() / mctsNode.getTimesVisited();
        double secondTermUnderSqrt = Math.log(mctsNode.getTimesVisited());
        if (mctsNode.getParent() != null) {
            secondTermUnderSqrt = Math.log(Math.max(1, mctsNode.getParent().getTimesVisited())) / mctsNode.getTimesVisited();
        }
        double secondTerm = explorationConstant * Math.sqrt(secondTermUnderSqrt);
        return firstTerm + secondTerm;
    }

    public void updateNode(MCTSNode mctsNode, BoardGameScore score) {
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

    public void updateNodeEval(MCTSNode mctsNode) {
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
        backPropagate(mctsNode, mctsNode.getBoard().getBoardGameScore());
    }

    public MCTSNode rolloutPolicy(MCTSNode mctsNode) {
        // Look in to moving this shuffle to when the node is created
        Collections.shuffle(mctsNode.getUnexplored());
        ConnectFourGameState newBoard = mctsNode.getUnexplored().getFirst();
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
        if (!BoardGameScore.UNDETERMINED.equals(mctsNode.getBoard().getBoardGameScore())) {
            return true;
        }
        if (mctsNode.getUnexplored().isEmpty()) {
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
            updateNodeEval(child);
            if (child.getCurrentValue() > maxVal) {
                maxVal = child.getCurrentValue();
                returnNode = child;
            }
        }
        return returnNode;
    }

    // This is the move to make at the end of the MCTS period. Checks score instead of taking into account the exploration value;
    public MCTSNode mostVisitedChild(MCTSNode mctsNode) {
        int maxVisits = Integer.MIN_VALUE;
        for (MCTSNode child : mctsNode.getChildren()) {
            if (child.getTimesVisited() > maxVisits) {
                maxVisits = child.getTimesVisited();
            }
        }
        int finalMaxVisits = maxVisits;
        List<MCTSNode> nodesWithMaxVisits = new ArrayList<>(mctsNode.getChildren().stream().filter(child -> child.getTimesVisited() == finalMaxVisits).toList());
        nodesWithMaxVisits.sort(Comparator.comparingDouble(MCTSNode::getScore).reversed());
        return nodesWithMaxVisits.getFirst();
    }

    // Update the node and then move up the tree, updating all the nodes along the way
    public void backPropagate(MCTSNode mctsNode, BoardGameScore score) {
        updateNode(mctsNode, score);
        if (!mctsNode.isRoot() && mctsNode.getParent() != null) {
            backPropagate(mctsNode.getParent(), score);
        }
    }
}
