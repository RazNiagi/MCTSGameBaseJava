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
import java.util.List;

@Slf4j
@Service
public class QuartoMCTSService extends BaseMCTSService<QuartoGameState> {
    private final QuartoGameMoveService quartoGameMoveService;
    
    public QuartoMCTSService(QuartoGameMoveService baseGameMoveService) {
        super(baseGameMoveService);
        quartoGameMoveService = baseGameMoveService;
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
                nextState = quartoGameMoveService.makeMove(nextState, move);
                
                // If game ended after placement, return the terminal state
                if (!nextState.getBoardGameScore().equals(BoardGameScore.UNDETERMINED)) {
                    possibleNextBoards.add(nextState);
                } else if (nextState.getAvailablePieces().isEmpty()) {
                    // No pieces left to select, game is a tie
                    nextState.setBoardGameScore(BoardGameScore.TIE);
                    possibleNextBoards.add(nextState);
                } else {
                    // Game continues, select a piece for opponent (complete the turn)
                    for (char piece : nextState.getAvailablePieces()) {
                        QuartoGameState stateWithSelection = QuartoGameState.cloneBoard(nextState);
                        stateWithSelection = quartoGameMoveService.selectPiece(stateWithSelection, piece);
                        possibleNextBoards.add(stateWithSelection);
                    }
                }
            } catch (Exception e) {
                log.error("Error generating possible boards: {}", e.getMessage());
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
        
        for (char piece : gameState.getAvailablePieces()) {
            try {
                QuartoGameState stateAfterPieceSelection = QuartoGameState.cloneBoard(gameState);
                stateAfterPieceSelection = quartoGameMoveService.selectPiece(stateAfterPieceSelection, piece);
                possibleNextBoards.add(stateAfterPieceSelection);
            } catch (Exception e) {
                log.error("Error selecting piece: {}", e.getMessage());
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
}
