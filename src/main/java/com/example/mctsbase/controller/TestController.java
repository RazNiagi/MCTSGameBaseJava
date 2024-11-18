package com.example.mctsbase.controller;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.*;
import com.example.mctsbase.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private ConnectFourService connectFourService;
    @Autowired
    private OnitamaGameService onitamaGameService;
    @Autowired
    private BoardImportExportService boardImportExportService;
    @Autowired
    private ConnectFourMoveService connectFourMoveService;
    @Autowired
    private ConnectFourMCTSService c4mctsService;
    @Autowired
    private OnitamaGameMoveService onitamaGameMoveService;
    @Autowired
    private OnitamaGameMCTSService onitamaGameMCTSService;


    @RequestMapping(value="/test", method = RequestMethod.GET)
    public String test() {

        ConnectFourGameState board = connectFourService.initializeGameState(ConnectFourGameState.builder().build());
        connectFourService.printBoard(board);
        BaseMCTSNode mctsNode = BaseMCTSNode.builder()
                .depth(0)
                .root(true)
                .unexplored(new ArrayList<>(connectFourMoveService.possibleNextBoards(board)))
                .children(new ArrayList<>())
                .parent(null)
                .timesVisited(0)
                .score(0.0)
                .currentValue(0.0)
                .board(board)
                .build();
        while (mctsNode.getBoard().getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            mctsNode = c4mctsService.monteCarloTreeSearch(mctsNode, 0, 2000);
            mctsNode.setParent(null);
            mctsNode.setRoot(true);
            connectFourService.printBoard((ConnectFourGameState) mctsNode.getBoard());
        }

        return "";
    }

    @RequestMapping(value="/testOnitama", method = RequestMethod.GET)
    public String testOnitama() {

        OnitamaGameState board = onitamaGameService.initializeGameState(OnitamaGameState.builder().build());
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
        mctsNode = onitamaGameMCTSService.monteCarloTreeSearch(mctsNode, 10, 5000);
        mctsNode.setParent(null);
        mctsNode.setRoot(true);
        onitamaGameService.printBoard((OnitamaGameState) mctsNode.getBoard());

        return "";
    }

    @RequestMapping(value="/testOnitamaParallel", method = RequestMethod.GET)
    public String testOnitamaParallel() {

        OnitamaGameState board = onitamaGameService.initializeGameState(OnitamaGameState.builder().build());
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
        while (mctsNode.getBoard().getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            mctsNode = onitamaGameMCTSService.parallelMCTS(mctsNode, 10, 2000, 4);
            onitamaGameService.printBoard((OnitamaGameState) mctsNode.getBoard());
        }

        return "";
    }

    @PostMapping(value="/testOnitamaParallel", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @RequestMapping(value="/testOnitamaInit", method = RequestMethod.GET)
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

    @PostMapping(value="/testParallel/{threads}")
    public String testParallel(@PathVariable Integer threads) {

        ConnectFourGameState board = connectFourService.initializeGameState(ConnectFourGameState.builder().build());
        connectFourService.printBoard(board);

        while (board.getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            BaseMCTSNode mctsNode = BaseMCTSNode.builder()
                    .depth(0)
                    .root(true)
                    .unexplored(new ArrayList<>(connectFourMoveService.possibleNextBoards(board)))
                    .children(new ArrayList<>())
                    .parent(null)
                    .timesVisited(0)
                    .score(0.0)
                    .currentValue(0.0)
                    .board(board)
                    .build();
            board = c4mctsService.parallelMCTS(mctsNode, 0, 2000, threads);
            connectFourService.printBoard(board);
        }

        return "";
    }

    @PostMapping(value="/tryboard/{boardstring}")
    public String tryboard(@PathVariable String boardstring) {

        ConnectFourGameState board = boardImportExportService.importBoard(boardstring);
        connectFourService.printBoard(board);

        BaseMCTSNode mctsNode = BaseMCTSNode.builder()
                .depth(0)
                .root(true)
                .unexplored(new ArrayList<>(connectFourMoveService.possibleNextBoards(board)))
                .children(new ArrayList<>())
                .parent(null)
                .timesVisited(0)
                .score(0.0)
                .currentValue(0.0)
                .board(board)
                .build();
        ConnectFourGameState newboard = (ConnectFourGameState) c4mctsService.monteCarloTreeSearch(mctsNode, 0, 2000).getBoard();
        connectFourService.printBoard(newboard);

        return "";
    }
}
