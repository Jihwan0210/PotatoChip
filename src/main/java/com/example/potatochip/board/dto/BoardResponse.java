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
    private String author;
    private String authorEmail;
    private String category;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount; // 이미 여기에 필드가 선언되어 있습니다!
    private LocalDateTime createdAt;
    private String imageUrl;

    // 🌟 [이 구역을 원래 소스코드로 완전히 원상복구 시켜줍니다]
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
                .likeCount(board.getLikeCount())
                .createdAt(board.getCreatedAt())
                .imageUrl(board.getImageUrl())
                .build();
    }
}