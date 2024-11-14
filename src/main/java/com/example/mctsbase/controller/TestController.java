package com.example.mctsbase.controller;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.ConnectFourGameState;
import com.example.mctsbase.model.MCTSNode;
import com.example.mctsbase.service.BoardImportExportService;
import com.example.mctsbase.service.ConnectFourMoveService;
import com.example.mctsbase.service.ConnectFourService;
import com.example.mctsbase.service.MCTSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private ConnectFourService connectFourService;
    @Autowired
    private BoardImportExportService boardImportExportService;
    @Autowired
    private ConnectFourMoveService connectFourMoveService;
    @Autowired
    private MCTSService mctsService;


    @RequestMapping(value="/test", method = RequestMethod.GET)
    public String test() {

        ConnectFourGameState board = connectFourService.initializeBoard(ConnectFourGameState.builder().build());
        connectFourService.printBoard(board);
        while (board.getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            MCTSNode mctsNode = MCTSNode.builder()
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

    @PostMapping(value="/testParallel/{threads}")
    public String testParallel(@PathVariable Integer threads) {

        ConnectFourGameState board = connectFourService.initializeBoard(ConnectFourGameState.builder().build());
        connectFourService.printBoard(board);

        while (board.getBoardGameScore() == BoardGameScore.UNDETERMINED) {
            MCTSNode mctsNode = MCTSNode.builder()
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

        MCTSNode mctsNode = MCTSNode.builder()
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
