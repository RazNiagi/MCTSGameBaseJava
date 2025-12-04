package com.example.mctsbase.controller;

import com.example.mctsbase.dto.QuartoGameStateDTO;
import com.example.mctsbase.model.QuartoGameState;
import com.example.mctsbase.service.QuartoGameMoveService;
import com.example.mctsbase.service.QuartoGameService;
import com.example.mctsbase.service.QuartoMCTSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // Implementation of the testQuarto method
        QuartoGameState board = quartoGameService.initializeGameState(QuartoGameState.builder().build());
        QuartoGameStateDTO boardDTO = quartoGameService.convertToDTO(board, 1);
        return ResponseEntity.status(200).body(boardDTO);
    }
}
