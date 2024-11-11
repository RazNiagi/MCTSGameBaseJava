package com.example.mctsbase.controller;

import com.example.mctsbase.enums.ConnectFourScore;
import com.example.mctsbase.model.ConnectFourBoard;
import com.example.mctsbase.model.MCTSNode;
import com.example.mctsbase.service.BoardImportExportService;
import com.example.mctsbase.service.ConnectFourMoveService;
import com.example.mctsbase.service.ConnectFourService;
import com.example.mctsbase.service.MCTSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public String test() throws Exception {

        ConnectFourBoard board = ConnectFourBoard.builder().build();
        connectFourService.initializeBoard(board);
        board.setCurrentTurn('r');
        board.setConnectFourScore(ConnectFourScore.UNDETERMINED);
        ConnectFourBoard correctBoard =  connectFourMoveService.makeMove(board, 3, board.getCurrentTurn());
        int correctcount = 0;
        for (int i = 0; i < 100; i++) {
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
            ConnectFourBoard newboard = mctsService.connectFourMCTS(mctsNode, 0, 500);
            connectFourService.printBoard(newboard);
            if (newboard.equals(correctBoard)) {
                correctcount++;
                log.info("correct " + correctcount + "/" + (i + 1));
            }
        }

        return "";
    }
}
