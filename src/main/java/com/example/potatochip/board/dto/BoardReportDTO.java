package com.example.potatochip.board.dto;

import com.example.potatochip.board.entity.BoardReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardReportDTO {
    private Long id;
    private Long boardId;
    private Long reporterId;
    private String reporterEmail;
    private String reporterName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public static BoardReportDTO fromEntity(BoardReport report) {
        return BoardReportDTO.builder()
                .id(report.getId())
                .boardId(report.getBoard() != null ? report.getBoard().getId() : null)
                .reporterId(report.getReporterId())
                .reporterEmail(report.getReporterEmail())
                .reporterName(report.getReporterName())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
