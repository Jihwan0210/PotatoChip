package com.example.potatochip.board.repository;

import com.example.potatochip.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // board 엔티티 내부의 id를 기준으로 찾는 JPA 규칙 메서드입니다.
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);
}
