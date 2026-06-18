package com.example.potatochip.board.repository;

import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.entity.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    Optional<BoardLike> findByBoardAndUserEmail(Board board, String userEmail);
    boolean existsByBoardIdAndUserEmail(Long boardId, String userEmail);
}