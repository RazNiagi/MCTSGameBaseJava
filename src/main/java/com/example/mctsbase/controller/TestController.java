package com.example.mctsbase.controller;

import com.example.mctsbase.model.ConnectFourBoard;
import com.example.mctsbase.service.BoardImportExportService;
import com.example.mctsbase.service.ConnectFourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private ConnectFourService connectFourService;
    @Autowired
    private BoardImportExportService boardImportExportService;
    @RequestMapping(value="/test", method = RequestMethod.GET)
    public String test() {
        ConnectFourBoard board = boardImportExportService.importBoard("--------------r--y---yryr---ryrrry-rryryyy");
        connectFourService.printBoard(board);
        System.out.println(connectFourService.checkBoardForWins(board));
        boardImportExportService.saveBoard(board);
        return "";
    }
}
