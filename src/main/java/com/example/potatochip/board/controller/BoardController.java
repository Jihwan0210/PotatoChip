package com.example.potatochip.board.controller;

import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 페이지 이동
    @GetMapping("/board")
    public String boardPage() {
        return "board/board";
    }

    // 전체 조회
    @GetMapping("/api/board")
    @ResponseBody
    public List<Board> getBoards() {
        return boardService.findAll();
    }

    // 단건 조회
    @GetMapping("/api/board/{id}")
    @ResponseBody
    public Board getBoard(@PathVariable Long id) {
        return boardService.findById(id);
    }

    // 생성


    @PostMapping("/api/board")
    @ResponseBody
    public Board createBoard(
            @RequestBody Board board,
            Authentication authentication
    ) {

        String email = authentication.getName();

        board.setAuthor(email);

        return boardService.save(board);
    }

    // 삭제
    @DeleteMapping("/api/board/{id}")
    @ResponseBody
    public void deleteBoard(@PathVariable Long id) {
        boardService.delete(id);
    }
}