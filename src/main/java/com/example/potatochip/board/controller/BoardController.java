package com.example.potatochip.board.controller;

import com.example.potatochip.board.dto.BoardResponse;
import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import com.example.potatochip.auth.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
    public List<BoardResponse> getBoards() {
        return boardService.findAllResponse();
    }

    @GetMapping("/api/board/{id}")
    @ResponseBody
    public BoardResponse getBoard(@PathVariable Long id) {
        return boardService.findResponseById(id);
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

                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String originalFileName = image.getOriginalFilename();
                String savedFileName = java.util.UUID.randomUUID() + "_" + originalFileName;

                java.io.File destinationFile = new java.io.File(uploadDir + savedFileName);
                image.transferTo(destinationFile);

                board.setImageUrl("/uploads/" + savedFileName);
                System.out.println("📷 이미지 업로드 성공: /uploads/" + savedFileName);

            } catch (java.io.IOException e) {
                System.out.println("❌ 이미지 저장 중 에러 발생: " + e.getMessage());
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
    public ResponseEntity<?> deleteBoard(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
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

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String originalFileName = image.getOriginalFilename();
            String savedFileName = java.util.UUID.randomUUID() + "_" + originalFileName;

            java.io.File destinationFile =
                    new java.io.File(uploadDir + savedFileName);

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
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
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
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Authentication resolvedAuthentication = resolveAuthentication(authentication, request);
        String userEmail = (resolvedAuthentication != null && resolvedAuthentication.isAuthenticated())
                ? resolvedAuthentication.getName()
                : null;
        // 1. DB에서 실시간으로 해당 게시글 정보를 안전하게 가져옵니다.
        com.example.potatochip.board.entity.Board board = boardService.findById(id);

        // 2. 만약 DB에 좋아요 수가 null로 박혀있다면 안전하게 0으로 처리해 줍니다.
        int currentLikeCount = (board.getLikeCount() == null) ? 0 : board.getLikeCount();

        // 3. 현재 로그인한 유저가 하트를 눌렀는지 여부 판단
        boolean isLiked = boardService.isLikedByUser(id, userEmail);

        // 4. 🚨 Map.of 대신 null 안전성이 보장되는 HashMap을 사용하여 500 에러를 원천 차단합니다.
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