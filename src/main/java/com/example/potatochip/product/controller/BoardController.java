package com.example.potatochip.product.controller;


import com.example.potatochip.product.dto.request.BoardRequestDTO;
import com.example.potatochip.product.dto.response.BoardResponseDTO;
import com.example.potatochip.product.entity.Board;
import com.example.potatochip.product.repository.BoardRepository;
import com.example.potatochip.product.service.BoardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }
    @GetMapping
    public List<BoardResponseDTO> getBoard() {
        return boardService.getBoards();
    }

    @GetMapping("/{id}")
    public BoardResponseDTO getBoard(@PathVariable Long id) {
        return boardService.getBoard(id);
    }

    @PostMapping
    public BoardResponseDTO createBoard(@RequestBody BoardRequestDTO request){
        return boardService.createBoard(request);
    }

    @PostMapping("/{id}")
    public BoardResponseDTO updateBoard(
            @PathVariable Long id,
            @RequestBody BoardRequestDTO request) {

        return boardService.updateBoard(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
    }

}
