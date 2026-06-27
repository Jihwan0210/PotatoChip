package com.example.potatochip.board.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "board_like")
public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌟 Board 엔티티의 likes 필드와 연결되는 다대일(N:1) 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 좋아요를 누른 유저의 식별 정보 (Spring Security의 이메일 혹은 ID)
    @Column(name = "user_email")
    private String userEmail;

    // JPA 필수 기본 생성자
    public BoardLike() {}

    public BoardLike(Board board, String userEmail) {
        this.board = board;
        this.userEmail = userEmail;
    }

    // 수동 Getter / Setter
    public Long getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}