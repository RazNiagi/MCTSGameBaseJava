package com.example.mctsbase.controller;

import com.example.mctsbase.model.ConnectFourBoard;
import com.example.mctsbase.service.BoardImportExportService;
import com.example.mctsbase.service.ConnectFourMoveService;
import com.example.mctsbase.service.ConnectFourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private ConnectFourService connectFourService;
    @Autowired
    private BoardImportExportService boardImportExportService;
    @Autowired
    private ConnectFourMoveService connectFourMoveService;


    @RequestMapping(value="/test", method = RequestMethod.GET)
    public String test() {
        ConnectFourBoard board = boardImportExportService.importBoard("--------------r--y---yryr---ryrrry-rryryyy");
        connectFourService.printBoard(board);
        log.info(connectFourService.checkBoardForWins(board).toString());
        try {
            connectFourMoveService.makeMove(board, 5, 'y');
            connectFourService.printBoard(board);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
//        boardImportExportService.saveBoard(board);
        return "";
    }
}
