package com.example.potatochip.board.controller;

import com.example.potatochip.board.dto.BoardRequestDTO;
import com.example.potatochip.board.dto.BoardRequestDTO;
import com.example.potatochip.board.dto.BoardResponseDTO;
import com.example.potatochip.board.service.BoardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 전체 게시글 조회
    @GetMapping
    public List<BoardResponseDTO> getBoards() {
        return boardService.getBoards();
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public BoardResponseDTO getBoard(@PathVariable Long id) {
        return boardService.getBoard(id);
    }

    // 게시글 등록
    @PostMapping
    public BoardResponseDTO createBoard(@RequestBody BoardRequestDTO request) {
        return boardService.createBoard(request);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public BoardResponseDTO updateBoard(
            @PathVariable Long id,
            @RequestBody BoardRequestDTO request) {

        return boardService.updateBoard(id, request);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
    }
}