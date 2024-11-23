package com.example.mctsbase.controller;

import com.example.mctsbase.enums.BoardGameScore;
import com.example.mctsbase.model.BaseMCTSNode;
import com.example.mctsbase.model.ConnectFourGameState;
import com.example.mctsbase.service.BoardImportExportService;
import com.example.mctsbase.service.ConnectFourMCTSService;
import com.example.mctsbase.service.ConnectFourMoveService;
import com.example.mctsbase.service.ConnectFourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/connect-four")
public class ConnectFourController {

    @Autowired
    private ConnectFourService connectFourService;
    @Autowired
    private BoardImportExportService boardImportExportService;
    @Autowired
    private ConnectFourMoveService connectFourMoveService;
    @Autowired
    private ConnectFourMCTSService c4mctsService;

    @GetMapping(value="/test")
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

    @PostMapping(value="/test-parallel/{threads}")
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

    @PostMapping(value="/try-board/{boardstring}")
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
