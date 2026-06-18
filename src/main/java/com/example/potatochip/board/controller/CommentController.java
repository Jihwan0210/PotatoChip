package com.example.potatochip.board.controller;

import com.example.potatochip.board.dto.CommentResponse;
import com.example.potatochip.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final BoardService boardService;

    // 댓글 목록 조회
    @GetMapping
    public List<CommentResponse> getComments(@PathVariable Long boardId) {
        // 🌟 BoardService의 메서드명과 정확히 매칭 (findCommentsByBoardId)
        return boardService.findCommentsByBoardId(boardId);
    }

    // 댓글 등록
    @PostMapping
    public CommentResponse createComment(
            @PathVariable Long boardId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication
    ) {
        String content = requestBody.get("content");

        // 🌟 빈 댓글이 입력되는 것을 방지하는 검증 로직
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요."
            );
        }
        return boardService.saveComment(boardId, content, authentication);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        boardService.deleteComment(commentId, authentication);
        return ResponseEntity.ok(Map.of("message", "댓글 삭제 완료"));
    }
}