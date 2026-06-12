package com.example.potatochip.board.service;

import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // 전체 조회
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    // 단건 조회
    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    // 저장
    public Board save(Board board) {
        return boardRepository.save(board);
    }

    // 삭제
    public void delete(Long id) {
        boardRepository.deleteById(id);
    }
}