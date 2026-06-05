package com.example.potatochip.product.service;


import com.example.potatochip.product.dto.request.BoardRequestDTO;
import com.example.potatochip.product.dto.response.BoardResponseDTO;
import com.example.potatochip.product.entity.Board;
import com.example.potatochip.product.repository.BoardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public List<BoardResponseDTO> getBoards() {
        return boardRepository.findAll()
                .stream()
                .map(BoardResponseDTO::from)
                .toList();
    }

    public BoardResponseDTO getBoard(Long id) {
        return boardRepository.findById(id)
                .map(BoardResponseDTO::from)
                .orElseThrow(()-> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    public BoardResponseDTO createBoard(BoardRequestDTO request) {

        Board board = new Board();

        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        Board saveBoard = boardRepository.save(board);

        return BoardResponseDTO.from(saveBoard);
    }

    public BoardResponseDTO updateBoard(Long id,BoardRequestDTO request) {

        Board board = boardRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("게시글을 찾을 수 없습니다"));

        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        Board updateBoard = boardRepository.save(board);

        return BoardResponseDTO.from(updateBoard);
    }

    public void deleteBoard(Long id) {

        Board board = boardRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("게시글을 찾을 수 없습니다"));

        boardRepository.delete(board);
    }
}
