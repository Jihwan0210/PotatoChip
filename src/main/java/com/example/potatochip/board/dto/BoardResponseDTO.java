package com.example.potatochip.board.dto;

import com.example.potatochip.board.entity.Board;

public class BoardResponseDTO {

    private Long id;
    private String title;
    private String content;

    public static BoardResponseDTO from(Board board) {

        BoardResponseDTO dto = new BoardResponseDTO();

        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
