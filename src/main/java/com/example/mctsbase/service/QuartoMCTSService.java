package com.example.mctsbase.service;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.BaseGameState;
import com.example.mctsbase.model.BaseMCTSNode;
import com.example.mctsbase.model.QuartoGameMove;
import com.example.mctsbase.model.QuartoGameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class QuartoMCTSService extends BaseMCTSService<QuartoGameState> {
    private final QuartoGameMoveService quartoGameMoveService;
    
    public QuartoMCTSService(QuartoGameMoveService quartoGameMoveService) {
        super(quartoGameMoveService);
        this.quartoGameMoveService = quartoGameMoveService;
    }
    
    @Override
    public BaseMCTSNode rolloutPolicy(BaseMCTSNode mctsNode) {
        Collections.shuffle(mctsNode.getUnexplored());
        QuartoGameState newBoard = (QuartoGameState) mctsNode.getUnexplored().getFirst();
        
        List<BaseGameState> nextUnexplored;
        if (!newBoard.getBoardGameScore().equals(BoardGameScore.UNDETERMINED)) {
            nextUnexplored = List.of();
        } else if (newBoard.getAvailablePieces().isEmpty() && newBoard.getSelectedPiece() == '-') {
            // Board is full and no piece selected - game is a tie
            nextUnexplored = List.of();
        } else {
            // Determine phase: if piece is selected, next phase is placement; otherwise selection
            if (newBoard.getSelectedPiece() != '-') {
                nextUnexplored = new ArrayList<>(possibleNextBoardsAfterPlacement(newBoard));
            } else {
                nextUnexplored = new ArrayList<>(possibleNextBoardsAfterSelection(newBoard));
            }
        }
        
        BaseMCTSNode newNode = BaseMCTSNode.builder()
                .root(false)
                .score(0.0)
                .currentValue(0.0)
                .depth(mctsNode.getDepth() + 1)
                .board(newBoard)
                .children(new ArrayList<>())
                .timesVisited(0)
                .parent(mctsNode)
                .unexplored(nextUnexplored)
                .build();
        mctsNode.getUnexplored().removeFirst();
        mctsNode.getChildren().add(newNode);
        return newNode;
    }
    
    public List<QuartoGameState> possibleNextBoardsAfterPlacement(QuartoGameState gameState) {
        if (!gameState.getBoardGameScore().equals(BoardGameScore.UNDETERMINED)) {
            return List.of();
        }
        
        if (gameState.getSelectedPiece() == '-') {
            return List.of();
        }
        
        List<QuartoGameState> possibleNextBoards = new ArrayList<>();
        
        for (QuartoGameMove move : quartoGameMoveService.getAllLegalMoves(gameState)) {
            try {
                QuartoGameState nextState = QuartoGameState.cloneBoard(gameState);
                nextState = quartoGameMoveService.placePiece(nextState, move);
                possibleNextBoards.add(nextState);
            } catch (Exception e) {
                log.error("Error generating possible boards after placement at ({}, {}): {}", move.getRow(), move.getCol(), e.getMessage(), e);
            }
        }
        
        return possibleNextBoards;
    }
    
    public List<QuartoGameState> possibleNextBoardsAfterSelection(QuartoGameState gameState) {
        if (!gameState.getBoardGameScore().equals(BoardGameScore.UNDETERMINED)) {
            return List.of();
        }
        
        if (gameState.getSelectedPiece() != '-') {
            return List.of();
        }
        
        if (gameState.getAvailablePieces().isEmpty()) {
            return List.of();
        }
        
        List<QuartoGameState> possibleNextBoards = new ArrayList<>();
        
        // Find all losing pieces in a single optimized pass
        List<Character> losingPiecesList = quartoGameMoveService.findAllLosingPieces(gameState);
        Set<Character> losingPieces = new HashSet<>(losingPiecesList);
        
        // Determine safe pieces (those not in the losing pieces set)
        List<Character> safePieces = new ArrayList<>();
        for (char piece : gameState.getAvailablePieces()) {
            if (!losingPieces.contains(piece)) {
                safePieces.add(piece);
            }
        }
        
        // If we have safe pieces, only use those; otherwise we must choose from losing pieces
        List<Character> piecesToConsider = safePieces.isEmpty() ? losingPiecesList : safePieces;
        
        for (char piece : piecesToConsider) {
            try {
                QuartoGameState stateAfterPieceSelection = QuartoGameState.cloneBoard(gameState);
                stateAfterPieceSelection = quartoGameMoveService.selectPiece(stateAfterPieceSelection, piece);
                possibleNextBoards.add(stateAfterPieceSelection);
            } catch (Exception e) {
                log.error("Error selecting piece '{}': {}", piece, e.getMessage(), e);
            }
        }
        
        return possibleNextBoards;
    }

    public BaseMCTSNode monteCarloAndGetGrandchildIfNeeded(BaseMCTSNode rootNode, int level) {
        BaseMCTSNode selectedNode = monteCarloTreeSearchWithLevel(rootNode, level);

        // If the selected node is not a leaf, pick a random child to return
        if (!selectedNode.getChildren().isEmpty()) {
            return selectedNode.getChildren().stream().max(Comparator.comparingInt(BaseMCTSNode::getTimesVisited)).orElse(selectedNode);
        }

        return selectedNode;
    }

    @Override
    public BaseMCTSNode monteCarloTreeSearch(BaseMCTSNode mctsNode, Integer maxDepthIncrease, Integer maxTime) {
        int startingDepth = mctsNode.getDepth();
        // Double the depth for Quarto since each turn has 2 phases (placement + selection)
        int quartoMaxDepth = (maxDepthIncrease != null && maxDepthIncrease != 0) ? (maxDepthIncrease * 2) + startingDepth : 0;
        this.maxDepth = quartoMaxDepth;
        long endTime = System.currentTimeMillis() + maxTime;

        return mctsLoop(mctsNode, endTime);
    }
    
    @Override
    public void updateNode(BaseMCTSNode mctsNode, BoardGameScore score) {
        if ((score.equals(BoardGameScore.PLAYER_1_WIN) && ((QuartoGameState) mctsNode.getBoard()).getCurrentTurn() == '1')
                || (score.equals(BoardGameScore.PLAYER_2_WIN) && ((QuartoGameState) mctsNode.getBoard()).getCurrentTurn() == '2')) {
            mctsNode.setScore(mctsNode.getScore() + 1.0);
        } else if (score.equals(BoardGameScore.TIE)) {
            mctsNode.setScore(mctsNode.getScore() + 0.5);
        }

        mctsNode.setTimesVisited(mctsNode.getTimesVisited() + 1);
        updateNodeEval(mctsNode);
    }
}
