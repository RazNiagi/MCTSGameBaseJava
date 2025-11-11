package com.example.mctsbase.controller;

import com.example.mctsbase.dto.OnitamaGameStateDTO;
import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.*;
import com.example.mctsbase.service.OnitamaGameMCTSService;
import com.example.mctsbase.service.OnitamaGameMoveService;
import com.example.mctsbase.service.OnitamaGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/onitama")
public class OnitamaController {
    @Autowired
    private OnitamaGameService onitamaGameService;
    @Autowired
    private OnitamaGameMoveService onitamaGameMoveService;
    @Autowired
    private OnitamaGameMCTSService onitamaGameMCTSService;

    @GetMapping(value="/test-onitama")
    public String testOnitama() {

        OnitamaGameState board = onitamaGameService.initializeGameState(OnitamaGameState.builder().build());
        char startingTurn = board.getCurrentTurn();
        onitamaGameService.printBoardFromCertainSide(board, startingTurn);
        BaseMCTSNode mctsNode = BaseMCTSNode.builder()
                .depth(0)
                .root(true)
                .unexplored(new ArrayList<>(onitamaGameMoveService.possibleNextBoards(board)))
                .children(new ArrayList<>())
                .parent(null)
                .timesVisited(0)
                .score(0.0)
                .currentValue(0.0)
                .board(board)
                .build();
        while (mctsNode.getBoard().getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            mctsNode = onitamaGameMCTSService.monteCarloTreeSearch(mctsNode, 10, 2000);
            mctsNode.setParent(null);
            mctsNode.setRoot(true);
            onitamaGameService.printBoardFromCertainSide((OnitamaGameState) mctsNode.getBoard(), startingTurn);
        }
        return "";
    }

    @PostMapping(value="/test-onitama-parallel", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String testOnitamaParallel_fromBoard(@RequestBody OnitamaSimplifiedGameState simpleBoard) {
        OnitamaGameState board = onitamaGameService.initializeGameStateFromSimplified(simpleBoard);
        onitamaGameService.printBoard(board);
        BaseMCTSNode mctsNode = BaseMCTSNode.builder()
                .depth(0)
                .root(true)
                .unexplored(new ArrayList<>(onitamaGameMoveService.possibleNextBoards(board)))
                .children(new ArrayList<>())
                .parent(null)
                .timesVisited(0)
                .score(0.0)
                .currentValue(0.0)
                .board(board)
                .build();
        mctsNode = onitamaGameMCTSService.parallelMCTS(mctsNode, 10, 5000, 4);
        onitamaGameService.printBoard((OnitamaGameState) mctsNode.getBoard());

        return "";
    }

    @RequestMapping(value="/test-onitama-parallel", method = RequestMethod.GET)
    public String testOnitamaParallel() {

        OnitamaGameState board = onitamaGameService.initializeGameState(OnitamaGameState.builder().build());
        char startingTurn = board.getCurrentTurn();
        onitamaGameService.printBoardFromCertainSide(board, startingTurn);
        BaseMCTSNode mctsNode = BaseMCTSNode.builder()
                .depth(0)
                .root(true)
                .unexplored(new ArrayList<>(onitamaGameMoveService.possibleNextBoards(board)))
                .children(new ArrayList<>())
                .parent(null)
                .timesVisited(0)
                .score(0.0)
                .currentValue(0.0)
                .board(board)
                .build();
        while (mctsNode.getBoard().getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            mctsNode = onitamaGameMCTSService.parallelMCTS(mctsNode, 10, 2000, 4);
            mctsNode.setRoot(true);
            mctsNode.setParent(null);
            onitamaGameService.printBoardFromCertainSide((OnitamaGameState) mctsNode.getBoard(), startingTurn);
        }

        return "";
    }

    @GetMapping(value="/test-onitama-init")
    public String testOnitamaInit() {

        OnitamaGameState board = onitamaGameService.initializeGameState(OnitamaGameState.builder().build());
        onitamaGameService.printBoard(board);

        List<OnitamaGameState> possibleNextBoards = onitamaGameMoveService.possibleNextBoards(board);
        log.info("Possible next board size: {}, Movement cards for Red: {}, Movement cards for Blue: {}", possibleNextBoards.size(),
                board.getRedPlayerMovementCards().stream().map(OnitamaSimpleMovementCard::getName).toList(),
                board.getBluePlayerMovementCards().stream().map(OnitamaSimpleMovementCard::getName).toList());
        for (OnitamaGameState possibleNextBoard : possibleNextBoards) {
            onitamaGameService.printBoard(possibleNextBoard);
        }

        return "";
    }

    @PostMapping(value="/make-move", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity makeMove(@RequestBody OnitamaGameStateDTO gameState) {
        log.info("Received request to make move on board");
        if (gameState.getLevel() < 1 || gameState.getLevel() > 10) {
            return ResponseEntity.
                    status(HttpStatus.BAD_REQUEST)
                    .body("Level must be between 1 and 10");
        }

        OnitamaGameState board = OnitamaGameStateDTO.getGameState(gameState);
        BaseMCTSNode mctsNode = BaseMCTSNode.builder()
                .depth(0)
                .root(true)
                .unexplored(new ArrayList<>(onitamaGameMoveService.possibleNextBoards(board)))
                .children(new ArrayList<>())
                .parent(null)
                .timesVisited(0)
                .score(0.0)
                .currentValue(0.0)
                .board(board)
                .build();
        OnitamaGameState newBoard = (OnitamaGameState) onitamaGameMCTSService.monteCarloTreeSearchWithLevel(mctsNode, gameState.getLevel()).getBoard();
        OnitamaGameStateDTO newBoardDTO = onitamaGameService.getDTOFromGameState(newBoard, gameState.getLevel());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(newBoardDTO);
    }
}
