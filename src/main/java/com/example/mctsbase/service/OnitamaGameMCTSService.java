package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.BaseMCTSNode;
import com.example.mctsbase.model.OnitamaGameState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

    // TODO look at why this is considering some nodes visited millions of times in the second iteration, maybe look in to atomicinteger for visits or locking nodes as they're worked
    // Insight on this is that the new node does not have any child nodes, need to look at combining all the nodes below that child, maybe a recursive function?
    @SneakyThrows
    public BaseMCTSNode parallelMCTS(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime, Integer numThreads) {
        nodes = Collections.synchronizedList(new ArrayList<>());
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
                    .children(Collections.synchronizedList(new ArrayList<>()))
                    .parent(null)
                    .unexplored(Collections.synchronizedList(new ArrayList<>()))
                    .build();
            onitamaGameMoveService.possibleNextBoards((OnitamaGameState) mctsNode.getBoard()).forEach(board -> newNode.getChildren().add(BaseMCTSNode.builder()
                    .unexplored(Collections.synchronizedList(new ArrayList<>()))
                    .parent(null)
                    .children(Collections.synchronizedList(new ArrayList<>()))
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
            if (mctsNode.getBoard().getCurrentTurn() == 'b') {
                mctsNode.setScore(mctsNode.getScore() + redPieceCount - bluePieceCount);
            }
            if (mctsNode.getBoard().getCurrentTurn() == 'r') {
                mctsNode.setScore(mctsNode.getScore() + bluePieceCount - redPieceCount);
            }
        }
        mctsNode.setTimesVisited(mctsNode.getTimesVisited() + 1);
        updateNodeEval(mctsNode);
    }

    public BaseMCTSNode monteCarloTreeSearch(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        int startingDepth = mctsNode.getDepth();
        maxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? maxDepthIncrease + startingDepth : 0;

        boolean pruned = false;
        return monteCarloTreeSearchWithPruning(mctsNode, maxTime, pruned);
    }
}
