package com.example.mctsbase.controller;

import com.example.mctsbase.dto.QuartoGameStateDTO;
import com.example.mctsbase.model.BaseMCTSNode;
import com.example.mctsbase.model.QuartoGameState;
import com.example.mctsbase.service.QuartoGameMoveService;
import com.example.mctsbase.service.QuartoGameService;
import com.example.mctsbase.service.QuartoMCTSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quarto")
public class QuartoController {

    @Autowired
    private QuartoGameService quartoGameService;
    @Autowired
    private QuartoGameMoveService quartoGameMoveService;
    @Autowired
    private QuartoMCTSService quartoMCTSService;

    @PostMapping(value="/test-quarto")
    public ResponseEntity testQuarto() {
        QuartoGameState board = quartoGameService.initializeGameState(QuartoGameState.builder().build());
        QuartoGameStateDTO boardDTO = quartoGameService.convertToDTO(board, 1);
        return ResponseEntity.status(200).body(boardDTO);
    }

    @PostMapping(value="/place-piece")
    public ResponseEntity<QuartoGameStateDTO> placePiece(@RequestBody QuartoGameStateDTO gameStateDTO) {
        try {
            QuartoGameState gameState = QuartoGameStateDTO.getGameState(gameStateDTO);

            if (gameState.getSelectedPiece() == '-') {
                log.error("Cannot place piece: no piece is selected");
                return ResponseEntity.badRequest().build();
            }

            BaseMCTSNode rootNode = BaseMCTSNode.builder()
                    .root(true)
                    .score(0.0)
                    .currentValue(0.0)
                    .depth(0)
                    .board(gameState)
                    .children(new ArrayList<>())
                    .timesVisited(0)
                    .parent(null)
                    .unexplored(new ArrayList<>(quartoMCTSService.possibleNextBoardsAfterPlacement(gameState)))
                    .build();

            // Get the best placement node, then get its best selection child for a complete turn
            BaseMCTSNode selectedNode = quartoMCTSService.monteCarloAndGetGrandchildIfNeeded(rootNode, gameStateDTO.getLevel());
            QuartoGameState resultState = (QuartoGameState) selectedNode.getBoard();
            QuartoGameStateDTO resultDTO = quartoGameService.convertToDTO(resultState, gameStateDTO.getLevel());

            return ResponseEntity.ok(resultDTO);
        } catch (Exception e) {
            log.error("Error in place-piece: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
