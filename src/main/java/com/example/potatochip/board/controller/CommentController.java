package com.example.potatochip.board.controller;

import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.board.dto.CommentResponse;
import com.example.potatochip.board.service.BoardService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final BoardService boardService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<CommentResponse> getComments(@PathVariable Long boardId) {
        return boardService.findCommentsByBoardId(boardId);
    }

    @PostMapping
    public CommentResponse createComment(
            @PathVariable Long boardId,
            @RequestBody Map<String, String> body,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        return boardService.saveComment(boardId, body.get("content"), resolvedAuthentication);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        boardService.deleteComment(commentId, resolvedAuthentication);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }

    private Authentication resolveAuthentication(Authentication authentication, HttpServletRequest request) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication;
        }

        String token = extractToken(request);

        if (token == null || !jwtUtil.validateToken(token)) {
            return authentication;
        }

        String email = jwtUtil.getEmail(token);
        String role = jwtUtil.getRole(token);

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (!token.isBlank() && !"null".equalsIgnoreCase(token)) {
                return token;
            }
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}