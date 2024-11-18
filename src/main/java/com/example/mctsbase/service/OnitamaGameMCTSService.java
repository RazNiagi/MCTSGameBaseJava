package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.BaseMCTSNode;
import com.example.mctsbase.model.OnitamaGameState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OnitamaGameMCTSService extends BaseMCTSService<OnitamaGameState> {
    private final OnitamaGameMoveService onitamaGameMoveService;
    public OnitamaGameMCTSService(OnitamaGameMoveService baseGameMoveService) {
        super(baseGameMoveService);
        onitamaGameMoveService = baseGameMoveService;
    }

    @SneakyThrows
    public BaseMCTSNode parallelMCTS(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime, Integer numThreads) {
        nodes = new ArrayList<>();
        try {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numThreads; i++) {
                threads.add(new Thread(() -> onitamaMCTSForParallelization(mctsNode, maxDepthIncrease, maxTime)));
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
                    .board(OnitamaGameState.cloneBoard((OnitamaGameState) mctsNode.getBoard()))
                    .children(new ArrayList<>())
                    .parent(null)
                    .unexplored(new ArrayList<>())
                    .build();
            onitamaGameMoveService.possibleNextBoards((OnitamaGameState) mctsNode.getBoard()).forEach(board -> newNode.getChildren().add(BaseMCTSNode.builder()
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
            return mostVisitedChild(newNode);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void onitamaMCTSForParallelization(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        mctsNode = BaseMCTSNode.builder()
                .root(mctsNode.isRoot())
                .depth(mctsNode.getDepth())
                .timesVisited(mctsNode.getTimesVisited())
                .currentValue(mctsNode.getCurrentValue())
                .score(mctsNode.getScore())
                .board(OnitamaGameState.cloneBoard((OnitamaGameState) mctsNode.getBoard()))
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

    public void updateNode(BaseMCTSNode mctsNode, BoardGameScore score) {
        // Update the evaluation of the node
        if ((score.equals(BoardGameScore.RED_WIN) && mctsNode.getBoard().getCurrentTurn() == 'b') || (score.equals(BoardGameScore.BLUE_WIN) && mctsNode.getBoard().getCurrentTurn() == 'r')) {
            mctsNode.setScore(mctsNode.getScore() + 5);
        }
        if (score.equals(BoardGameScore.UNDETERMINED)) {
            int redPieceCount = onitamaGameMoveService.numPiecesForColor(mctsNode.getBoard().getBoard(), 'r');
            int bluePieceCount = onitamaGameMoveService.numPiecesForColor(mctsNode.getBoard().getBoard(), 'b');
            if (mctsNode.getBoard().getCurrentTurn() == 'b' && redPieceCount > bluePieceCount) {
                mctsNode.setScore(mctsNode.getScore() + 2.5 + (double) (redPieceCount - bluePieceCount) / 2);
            }
            if (mctsNode.getBoard().getCurrentTurn() == 'r' && bluePieceCount > redPieceCount) {
                mctsNode.setScore(mctsNode.getScore() + 2.5 + (double) (bluePieceCount - redPieceCount) / 2);
            }
        }
        mctsNode.setTimesVisited(mctsNode.getTimesVisited() + 1);
        updateNodeEval(mctsNode);
    }

    public BaseMCTSNode monteCarloTreeSearch(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        long startTime = System.currentTimeMillis();
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;

        boolean pruned = false;

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
        log.info("times visited: " + mctsNode.getTimesVisited());
        return mostVisitedChild(mctsNode);
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
        for (int i = shouldBePruned.size() - 1; i >= 0; i--) {
            if (shouldBePruned.get(i) && mctsNode.getChildren().size() > 1) {
                mctsNode.getChildren().remove(i);
            }
        }
        return true;
    }
}
