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

    // 생성
    @PostMapping(value = "/api/board", consumes = {"multipart/form-data"})
    @ResponseBody
    public Board createBoard(
            @RequestPart("board") Board board,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            Authentication authentication
    ) {
        String email = authentication.getName();
        board.setAuthor(email);

        // 💡 [수정] 수신된 이미지 파일을 실제로 서버 하드디스크에 저장하는 로직 구현
        if (image != null && !image.isEmpty()) {
            try {
                // 1. 파일을 보관할 실제 물리적인 폴더 경로 지정
                String uploadDir = "C:/minsung/uploads/";
                java.io.File folder = new java.io.File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs(); // 폴더가 없다면 자동으로 생성해 줍니다.
                }

                // 2. 파일 이름 중복 방지를 위해 고유한 랜덤 ID(UUID)를 파일명 앞에 결합
                String originalFileName = image.getOriginalFilename();
                String savedFileName = java.util.UUID.randomUUID().toString() + "_" + originalFileName;

                // 3. 해당 경로에 파일 물리적 저장(이동)
                java.io.File destinationFile = new java.io.File(uploadDir + savedFileName);
                image.transferTo(destinationFile);

                // 4. 프론트엔드 브라우저가 접근할 수 있는 웹상의 가상 URL 주소를 Board 객체에 매핑
                // (예: /uploads/랜덤ID_사진명.jpg)
                board.setImageUrl("/uploads/" + savedFileName);
                System.out.println("📷 이미지 업로드 및 엔티티 매핑 성공: /uploads/" + savedFileName);

            } catch (java.io.IOException e) {
                System.out.println("❌ 이미지 저장 중 에러 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return boardService.save(board);
    }



    //조회수 증가
    @PostMapping("/api/board/{id}/view")
    @ResponseBody
    public void increasedView(@PathVariable Long id) {
        boardService.increaseView(id);

    }

    // 삭제
    @DeleteMapping("/api/board/{id}")
    @ResponseBody
    public ResponseEntity<?>deleteBoard(
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

    @PutMapping("/api/board/{id}")
    @ResponseBody
    public Board updateBoard(
            @PathVariable Long id,
            @RequestBody Board updatedBoard,
            Authentication authentication
    ) {

        Board board = boardService.findById(id);

        if (!board.getAuthor().equals(authentication.getName())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());
        board.setCategory(updatedBoard.getCategory());
        return boardService.save(board);
    }
}