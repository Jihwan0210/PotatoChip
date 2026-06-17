package com.example.potatochip.board.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "comments")
@SuppressWarnings("PersistenceUnitPresent")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글 내용 (긴 텍스트를 저장할 수 있도록 TEXT 타입 지정)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 댓글 작성자 (유저 이메일 등)
    @Column(nullable = false)
    private String author;

    // 댓글 작성 시간 (기본값으로 현재 시간 지정)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 게시글 엔티티와의 연관관계 매핑 (지연 로딩 설정)
    // 💡 @JsonIgnore: 보드 서비스에서 댓글을 불러올 때 무한 루프(500 에러)가 터지는 것을 완벽히 방지해 줍니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnore
    private Board board;
}