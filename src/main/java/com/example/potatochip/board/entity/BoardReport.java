package com.example.potatochip.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "board_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_report_board_reporter", columnNames = {"board_id", "reporter_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BoardReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "reporter_email", nullable = false)
    private String reporterEmail;

    @Column(name = "reporter_name", length = 100)
    private String reporterName;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "PENDING";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
