package com.example.potatochip.board.dto;

import com.example.potatochip.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private String content;

    // 화면 표시용
    private String author;

    // 권한 비교용
    private String authorEmail;

    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment, String displayName) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(displayName)
                .authorEmail(comment.getAuthor())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}