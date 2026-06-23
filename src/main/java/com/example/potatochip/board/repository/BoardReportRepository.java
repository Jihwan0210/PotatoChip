package com.example.potatochip.board.repository;

import com.example.potatochip.board.entity.BoardReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardReportRepository extends JpaRepository<BoardReport, Long> {
    boolean existsByBoardIdAndReporterId(Long boardId, Long reporterId);
    Optional<BoardReport> findByBoardIdAndReporterId(Long boardId, Long reporterId);
    List<BoardReport> findByBoardIdOrderByCreatedAtDesc(Long boardId);
    List<BoardReport> findAllByOrderByCreatedAtDesc();
    long countByBoardId(Long boardId);
}
