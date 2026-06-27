package com.example.potatochip.board.controller;

import com.example.potatochip.auth.util.JwtUtil;
import com.example.potatochip.board.dto.BoardResponse;
import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.service.BoardService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final JwtUtil jwtUtil;

    @GetMapping("/board")
    public String boardPage() {
        return "board/board";
    }

    @GetMapping("/api/board")
    @ResponseBody
    public List<BoardResponse> getBoards(Authentication authentication, HttpServletRequest request) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        String userEmail = resolvedAuthentication != null && resolvedAuthentication.isAuthenticated()
                ? resolvedAuthentication.getName()
                : null;
        return boardService.findAllResponse(userEmail);
    }

    @GetMapping("/api/board/{id}")
    @ResponseBody
    public BoardResponse getBoard(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        String userEmail = resolvedAuthentication != null && resolvedAuthentication.isAuthenticated()
                ? resolvedAuthentication.getName()
                : null;
        return boardService.findResponseById(id, userEmail);
    }

    @PostMapping(value = "/api/board", consumes = {"multipart/form-data"})
    @ResponseBody
    public BoardResponse createBoard(
            @RequestPart("board") Board board,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication,
            HttpServletRequest request
    ) {
        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "C:/minsung/uploads/";
                java.io.File folder = new java.io.File(uploadDir);
                if (!folder.exists()) folder.mkdirs();
                String savedFileName = java.util.UUID.randomUUID() + "_" + image.getOriginalFilename();
                java.io.File destinationFile = new java.io.File(uploadDir + savedFileName);
                image.transferTo(destinationFile);
                board.setImageUrl("/uploads/" + savedFileName);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        return boardService.createBoard(board, resolvedAuthentication);
    }

    @PostMapping("/api/board/{id}/view")
    @ResponseBody
    public void increasedView(@PathVariable Long id) {
        boardService.increaseView(id);
    }

    @DeleteMapping("/api/board/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBoard(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        boardService.deleteBoard(id, resolvedAuthentication);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }

    @PutMapping(value = "/api/board/{id}", consumes = {"multipart/form-data"})
    @ResponseBody
    public BoardResponse updateBoard(
            @PathVariable Long id,
            @RequestPart("board") Board updatedBoard,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication,
            HttpServletRequest request
    ) {
        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "C:/minsung/uploads/";
                java.io.File folder = new java.io.File(uploadDir);
                if (!folder.exists()) folder.mkdirs();
                String savedFileName = java.util.UUID.randomUUID() + "_" + image.getOriginalFilename();
                java.io.File destinationFile = new java.io.File(uploadDir + savedFileName);
                image.transferTo(destinationFile);
                updatedBoard.setImageUrl("/uploads/" + savedFileName);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        return boardService.updateBoard(id, updatedBoard, resolvedAuthentication);
    }

    @PostMapping("/api/board/{id}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        if (resolvedAuthentication == null || !resolvedAuthentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        String userEmail = resolvedAuthentication.getName();
        int updatedLikeCount = boardService.toggleLike(id, userEmail);

        return ResponseEntity.ok(Map.of(
                "likeCount", updatedLikeCount,
                "isLiked", boardService.isLikedByUser(id, userEmail)
        ));
    }

    @GetMapping("/api/board/{id}/like/status")
    @ResponseBody
    public ResponseEntity<?> getLikeStatus(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        String userEmail = (resolvedAuthentication != null && resolvedAuthentication.isAuthenticated())
                ? resolvedAuthentication.getName()
                : null;

        Board board = boardService.findById(id);
        int currentLikeCount = board.getLikeCount() == null ? 0 : board.getLikeCount();
        boolean isLiked = boardService.isLikedByUser(id, userEmail);

        java.util.Map<String, Object> responseMap = new java.util.HashMap<>();
        responseMap.put("likeCount", currentLikeCount);
        responseMap.put("isLiked", isLiked);
        return ResponseEntity.ok(responseMap);
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
            if (!token.isBlank() && !"null".equalsIgnoreCase(token)) return token;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) return cookie.getValue();
            }
        }

        return null;
    }
}
