package com.example.mctsbase.service;

import com.example.mctsbase.model.QuartoGameState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuartoMCTSService extends BaseMCTSService<QuartoGameState> {
    private final QuartoGameMoveService quartoGameMoveService;
    public QuartoMCTSService(QuartoGameMoveService baseGameMoveService) {
        super(baseGameMoveService);
        quartoGameMoveService = baseGameMoveService;
    }
}
