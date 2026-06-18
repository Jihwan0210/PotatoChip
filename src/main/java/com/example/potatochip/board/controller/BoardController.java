package com.example.potatochip.board.controller;

import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 페이지 이동
    @GetMapping("/board")
    public String boardPage() {
        return "board/board";
    }

    // 전체 조회
    @GetMapping("/api/board")
    @ResponseBody
    public List<Board> getBoards() {
        return boardService.findAll();
    }

    // 단건 조회
    @GetMapping("/api/board/{id}")
    @ResponseBody
    public Board getBoard(@PathVariable Long id) {
        return boardService.findById(id);
    }

    // 1. 게시글 생성 (원래 잘 작동하던 코드)
    @PostMapping(value = "/api/board", consumes = {"multipart/form-data"})
    @ResponseBody
    public Board createBoard(
            @RequestPart("board") Board board,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            Authentication authentication
    ) {
        String email = authentication.getName();
        board.setAuthor(email);

        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "C:/minsung/uploads/";
                java.io.File folder = new java.io.File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String originalFileName = image.getOriginalFilename();
                String savedFileName = java.util.UUID.randomUUID().toString() + "_" + originalFileName;

                java.io.File destinationFile = new java.io.File(uploadDir + savedFileName);
                image.transferTo(destinationFile);

                board.setImageUrl("/uploads/" + savedFileName);
                System.out.println("📷 이미지 업로드 및 엔티티 매핑 성공: /uploads/" + savedFileName);

            } catch (java.io.IOException e) {
                System.out.println("❌ 이미지 저장 중 에러 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return boardService.save(board);
    }

    // 2. 게시글 수정 (톰캣 버그 방지를 위해 POST + /update 경로로 안전하게 분리)
    @PostMapping(value = "/api/board/{id}/update", consumes = {"multipart/form-data"})
    @ResponseBody
    public Board updateBoard(
            @PathVariable Long id,
            @RequestPart("board") Board updatedBoard,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            Authentication authentication
    ) {
        // 기존 게시글 조회
        Board board = boardService.findById(id);

        // 권한 체크
        if (authentication == null || !board.getAuthor().equals(authentication.getName())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 기본 정보 갱신 (오타 수정 반영)
        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());
        board.setCategory(updatedBoard.getCategory());

        // 수정할 새 이미지가 들어온 경우에만 가로채서 업로드 처리
        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "C:/minsung/uploads/";
                java.io.File folder = new java.io.File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String originalFileName = image.getOriginalFilename();
                String savedFileName = java.util.UUID.randomUUID().toString() + "_" + originalFileName;

                java.io.File destinationFile = new java.io.File(uploadDir + savedFileName);
                image.transferTo(destinationFile);

                board.setImageUrl("/uploads/" + savedFileName);
                System.out.println("🔄 [수정] 이미지 업데이트 완료: /uploads/" + savedFileName);

            } catch (java.io.IOException e) {
                System.out.println("❌ [수정] 이미지 저장 중 에러 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // 이미지를 첨부 안 했으면 기존 board.getImageUrl()이 그대로 유지됩니다.

        return boardService.save(board);
    }

    // 조회수 증가
    @PostMapping("/api/board/{id}/view")
    @ResponseBody
    public void increasedView(@PathVariable Long id) {
        boardService.increaseView(id);
    }

    //좋아요 수 증가 API
@PostMapping("/api/board/{id}/like")
@ResponseBody
public void addLike(@PathVariable("id") Long id) {
        System.out.println("\uD83D\uDCEC [서버] 좋아요 등록 요청 진입! 게시글 ID:" + id);
 boardService.toggleLike(id,false);
}
        @PostMapping("/api/board/{id}/unlike")
        @ResponseBody
        public void removeLike(@PathVariable("id") Long id) {
System.out.println("📬 [서버] 좋아요 취소 요청 진입! 게시글 ID: " + id);
        boardService.toggleLike(id,true);
        }


    // 삭제
    @DeleteMapping("/api/board/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBoard(
            @PathVariable Long id,
            Authentication authentication
    ) {
        System.out.println("=== 삭제 요청 진입! 게시글 ID: " + id);

        if (authentication == null) {
            System.out.println("❌ 인증 객체가 null입니다. 토큰이 안 넘어왔을 수 있습니다.");
            return ResponseEntity.status(401).body(java.util.Map.of("error", "로그인이 필요합니다."));
        }

        Board board = boardService.findById(id);

        System.out.println("DB에 저장된 작성자: " + board.getAuthor());
        System.out.println("현재 로그인한 유저: " + authentication.getName());

        if (!board.getAuthor().equals(authentication.getName())) {
            System.out.println("❌ 작성자가 일치하지 않아 삭제가 거부되었습니다.");
            return ResponseEntity.status(403).body(java.util.Map.of("error", "삭제 권한이 없습니다."));
        }

        boardService.delete(id);
        System.out.println("✅ 삭제 성공!");

        return ResponseEntity.ok().body(java.util.Map.of("message", "삭제 완료"));
    }
}