package com.example.potatochip.board.controller;

import com.example.potatochip.board.dto.CommentResponse;
import com.example.potatochip.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board/{boardId}/comments")
public class CommentController {

    private final BoardService boardService;

    @GetMapping
    public List<CommentResponse> getComments(@PathVariable Long boardId) {
        return boardService.findCommentResponsesByBoardId(boardId);
    }

    @PostMapping
    public CommentResponse createComment(
            @PathVariable Long boardId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication
    ) {
        String content = requestBody.get("content");
        return boardService.saveComment(boardId, content, authentication);
    }

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