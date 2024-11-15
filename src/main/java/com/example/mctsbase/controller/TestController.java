package com.example.mctsbase.controller;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.ConnectFourGameState;
import com.example.mctsbase.model.ConnectFourMCTSNode;
import com.example.mctsbase.model.OnitamaGameState;
import com.example.mctsbase.model.OnitamaSimpleMovementCard;
import com.example.mctsbase.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private MCTSService mctsService;
    @Autowired
    private OnitamaGameMoveService onitamaGameMoveService;


    @RequestMapping(value="/test", method = RequestMethod.GET)
    public String test() {

        ConnectFourGameState board = connectFourService.initializeGameState(ConnectFourGameState.builder().build());
        connectFourService.printBoard(board);
        while (board.getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            ConnectFourMCTSNode mctsNode = ConnectFourMCTSNode.builder()
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
            board = mctsService.connectFourMCTS(mctsNode, 0, 2000);
            connectFourService.printBoard(board);
        }

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
            ConnectFourMCTSNode mctsNode = ConnectFourMCTSNode.builder()
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
            board = mctsService.parallelMCTS(mctsNode, 0, 2000, threads);
            connectFourService.printBoard(board);
        }

        return "";
    }

    @PostMapping(value="/tryboard/{boardstring}")
    public String tryboard(@PathVariable String boardstring) {

        ConnectFourGameState board = boardImportExportService.importBoard(boardstring);
        connectFourService.printBoard(board);

        ConnectFourMCTSNode mctsNode = ConnectFourMCTSNode.builder()
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
        ConnectFourGameState newboard = mctsService.connectFourMCTS(mctsNode, 0, 2000);
        connectFourService.printBoard(newboard);

        return "";
    }
}
