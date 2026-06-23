package com.example.potatochip.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, length = 50)
    private String category;

    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "hidden", nullable = false)
    private Boolean hidden = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (viewCount == null) viewCount = 0;
        if (commentCount == null) commentCount = 0;
        if (likeCount == null) likeCount = 0;
        if (hidden == null) hidden = false;
        if (isActive == null) isActive = true;
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isVisible() {
        return !Boolean.TRUE.equals(hidden) && !Boolean.FALSE.equals(isActive);
    }

    public void hide() {
        this.hidden = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void restore() {
        this.hidden = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
