package com.example.mctsbase.service;

import com.example.mctsbase.model.ConnectFourGameState;
import com.example.mctsbase.model.BaseMCTSNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

@Slf4j
@Service
public class BoardImportExportService {
    @Autowired
    private ConnectFourMoveService connectFourMoveService;

    private final ObjectMapper mapper = new ObjectMapper();
    public ConnectFourGameState importBoard(String fileName) {
        // read from file
        ConnectFourGameState newBoard = convertFileNameToBoard(fileName);
        int moveCount = 0;
        for (char[] row : newBoard.getBoard()) {
            for (char c : row) {
                if (c != '-') {
                    moveCount++;
                }
            }
        }
        newBoard.setCurrentTurn(moveCount % 2 == 0 ? 'r' : 'y');
        newBoard.setBoardGameScore(connectFourMoveService.checkBoardForWins(newBoard));
        return newBoard;
    }

    public BaseMCTSNode importNode(String boardFileName) {
        String rootPath = System.getProperty("user.dir");
        File file = new File(StringUtils.join(rootPath, "/MCTSConnectFour/", boardFileName, ".txt"));
        try {
            if (!file.exists()) {
                return null;
            }
            return mapper.readValue(file, BaseMCTSNode.class);
        } catch (IOException e) {
            log.error("Node file import failed", e);
            return null;
        }
    }

    public boolean saveNode(BaseMCTSNode mctsNode) {
        String boardAsString = convertBoardToFileName((ConnectFourGameState) mctsNode.getBoard());
        String rootPath = System.getProperty("user.dir");
        File file = new File(StringUtils.join(rootPath, "/MCTSConnectFour/", boardAsString, ".txt"));
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            if (file.createNewFile()) {
                log.info("Node file created");
            } else {
                log.warn("Node file already exists");
                file.delete();
                file.createNewFile();
            }
            mapper.writeValue(file, mctsNode);
        } catch (IOException e) {
            log.error("Node file creation failed", e);
            return false;
        }

        return true;
    }

    public boolean saveBoard(ConnectFourGameState board) {
        //convert to single string then save file with file name being board state
        String boardAsString = convertBoardToFileName(board);
        String rootPath = System.getProperty("user.dir");
        File file = new File(StringUtils.join(rootPath, "/MCTSConnectFour/", boardAsString, ".txt"));
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            if (file.createNewFile()) {
                log.info("Board file created");
            } else {
                log.warn("Board file already exists");
                file.delete();
                file.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(boardAsString);
            } catch (FileNotFoundException e) {
                log.error("Board file not found");
                return false;
            }
        } catch (IOException e) {
            log.error("Board file creation failed", e);
            return false;
        }

        return true;
    }

    public String convertBoardToFileName(ConnectFourGameState board) {
        char[][] currentBoard = board.getBoard();
        StringBuilder flattenedBoard = new StringBuilder();
        for (char[] chars : currentBoard) {
            flattenedBoard.append(new String(chars));
        }
        return flattenedBoard.toString();
    }

    public ConnectFourGameState convertFileNameToBoard(String fileName) {
        char[][] newBoard = new char[6][7];
        if (fileName.length() != 42) {
            log.error("Board file name is not correct length");
            return null;
        }
        for (int i = 0; i < 6; i++) {
            char[] chars = fileName.substring(7*i, 7+7*i).toCharArray();
            char[] tempArray = new char[7];
            System.arraycopy(chars, 0, tempArray, 0, 7);
            newBoard[5 - i] = tempArray;
        }
        return ConnectFourGameState.builder().board(newBoard).build();
    }
}
