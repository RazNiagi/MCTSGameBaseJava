package com.example.mctsbase.service;

import com.example.mctsbase.model.BaseMCTSNode;
import com.example.mctsbase.model.ConnectFourGameState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Service
public class ConnectFourMCTSService extends BaseMCTSService<ConnectFourGameState> {
    private final ConnectFourMoveService connectFourMoveService;
    public ConnectFourMCTSService(ConnectFourMoveService baseGameMoveService) {
        super(baseGameMoveService);
        connectFourMoveService = baseGameMoveService;
    }

    @SneakyThrows
    public ConnectFourGameState parallelMCTS(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime, Integer numThreads) {
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
            BaseMCTSNode newNode = BaseMCTSNode.builder()
                    .root(mctsNode.isRoot())
                    .depth(mctsNode.getDepth())
                    .timesVisited(mctsNode.getTimesVisited())
                    .currentValue(mctsNode.getCurrentValue())
                    .score(mctsNode.getScore())
                    .board(ConnectFourGameState.cloneBoard((ConnectFourGameState) mctsNode.getBoard()))
                    .children(new ArrayList<>())
                    .parent(null)
                    .unexplored(new ArrayList<>())
                    .build();
            connectFourMoveService.possibleNextBoards((ConnectFourGameState) mctsNode.getBoard()).forEach(board -> newNode.getChildren().add(BaseMCTSNode.builder()
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
            return (ConnectFourGameState) mostVisitedChild(newNode).getBoard();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void connectFourMCTSForParallelization(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        mctsNode = BaseMCTSNode.builder()
                .root(mctsNode.isRoot())
                .depth(mctsNode.getDepth())
                .timesVisited(mctsNode.getTimesVisited())
                .currentValue(mctsNode.getCurrentValue())
                .score(mctsNode.getScore())
                .board(ConnectFourGameState.cloneBoard((ConnectFourGameState) mctsNode.getBoard()))
                .children(new ArrayList<>(mctsNode.getChildren()))
                .parent(null)
                .unexplored(new ArrayList<>(mctsNode.getUnexplored()))
                .build();
        long startTime = System.currentTimeMillis();
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;

        // While resources left and tree not fully mapped
        while (System.currentTimeMillis() - startTime < maxTime) {
            BaseMCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
        }

        nodes.add(mctsNode);
    }

    public BaseMCTSNode monteCarloTreeSearchWithLevel(BaseMCTSNode mctsNode, Integer level) {
        if (mctsNode.getChildren().size() == 1) {
            return mctsNode.getChildren().getFirst();
        }
        int chanceForRandom = (int)(Math.random() * 100) + 1;
        if (chanceForRandom > 10 * level) {
            log.info("Returning random move");
            Random rand = new Random();
            ArrayList<BaseMCTSNode> unexploredAndChildren = new ArrayList<>(mctsNode.getChildren());
            mctsNode.getUnexplored().forEach(unexploredNode -> unexploredAndChildren.add(BaseMCTSNode.builder()
                    .root(false)
                    .depth(mctsNode.getDepth() + 1)
                    .timesVisited(1)
                    .currentValue(0.0)
                    .score(0.0)
                    .board(unexploredNode)
                    .children(new ArrayList<>())
                    .parent(mctsNode)
                    .unexplored(new ArrayList<>())
                    .build()));
            return unexploredAndChildren.get(rand.nextInt(unexploredAndChildren.size()));
        }
        log.info("Making move for board with level {}", level);
        int maxTime = level * 200;
        long startTime = System.currentTimeMillis();
        maxDepth = 0;
        boolean pruned = level <= 7;

        // While resources left and tree not fully mapped
        while (!pruned && System.currentTimeMillis() - startTime < maxTime) {
            BaseMCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
            pruned = pruneLosingBranches(mctsNode);
        }

        while (System.currentTimeMillis() - startTime < maxTime) {
            BaseMCTSNode leaf = traverse(mctsNode);
            rollout(leaf);
        }
        return mostVisitedChild(mctsNode);
    }
}
