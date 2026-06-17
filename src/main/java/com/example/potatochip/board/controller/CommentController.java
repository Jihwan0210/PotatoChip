package com.example.potatochip.board.controller;

import com.example.potatochip.board.entity.Comment;
import com.example.potatochip.board.service.BoardService; // 또는 본인의 CommentService가 있다면 주입
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
    @ResponseBody
    public List<Comment> getComments(@PathVariable Long boardId) {

        return boardService.findCommentsByBoardId(boardId);
    }

    // 2. 댓글 등록 (POST)
    @PostMapping
    public Comment createComment(
            @PathVariable Long boardId,
            @RequestBody Map<String, String> requestBody, // JS에서 보낸 { content: content } 구조를 가볍게 수신
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        String content = requestBody.get("content");
        String email = authentication.getName(); // 로그인한 유저 이메일


        return boardService.saveComment(boardId, content, email);
    }

    // 3. 댓글 삭제 (DELETE)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }


        boardService.deleteComment(commentId, authentication.getName());

        return ResponseEntity.ok().body(Map.of("message", "댓글 삭제 완료"));
    }
}