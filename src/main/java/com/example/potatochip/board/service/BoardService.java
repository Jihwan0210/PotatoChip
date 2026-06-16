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

        if (board.getViewCount() == null){
            board.setViewCount(0);
        }

        if(board.getCommentCount() == null) {
            board.setCommentCount(0);
        }

        if(board.getCreatedAt() == null) {
            board.setCreatedAt(java.time.LocalDateTime.now());
        }
        return boardRepository.save(board);
    }

    //조회수 증가
    public void increaseView(Long id) {

        Board board = findById(id);

        if(board.getViewCount() == null) {
            board.setViewCount(0);
        }

        board.setViewCount(board.getViewCount() +1);

        boardRepository.save(board);
    }

    // 삭제
    public void delete(Long id) {
        boardRepository.deleteById(id);
    }
}