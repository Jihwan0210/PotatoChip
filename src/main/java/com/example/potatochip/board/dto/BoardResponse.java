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
    private Integer likeCount;
    private LocalDateTime createdAt;
    private String imageUrl;
    private Boolean hidden;
    private Boolean active;
    private Boolean reportedByCurrentUser;

    public static BoardResponse from(Board board, String displayName) {
        return from(board, displayName, false);
    }

    public static BoardResponse from(Board board, String displayName, boolean reportedByCurrentUser) {
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
                .hidden(Boolean.TRUE.equals(board.getHidden()))
                .active(!Boolean.FALSE.equals(board.getIsActive()))
                .reportedByCurrentUser(reportedByCurrentUser)
                .build();
    }
}
