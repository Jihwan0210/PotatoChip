package com.example.potatochip.board.dto;

import com.example.potatochip.board.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardResponse {

    private Long id;
    private String title;
    private String content;

    // 화면 표시용: 닉네임 -> 이름 -> 이메일
    private String author;

    // 권한 비교용: 이메일
    private String authorEmail;

    private String category;
    private Integer viewCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private String imageUrl;

    public static BoardResponse from(Board board, String displayName) {
        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(displayName)
                .authorEmail(board.getAuthor())
                .category(board.getCategory())
                .viewCount(board.getViewCount())
                .commentCount(board.getCommentCount())
                .createdAt(board.getCreatedAt())
                .imageUrl(board.getImageUrl())
                .build();
    }
}