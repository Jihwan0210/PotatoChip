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

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

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
            Authentication authentication
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

        return boardService.createBoard(board, authentication);
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
            Authentication authentication
    ) {
        boardService.deleteBoard(id, authentication);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }

    @PutMapping("/api/board/{id}")
    @ResponseBody
    public BoardResponse updateBoard(
            @PathVariable Long id,
            @RequestBody Board updatedBoard,
            Authentication authentication
    ) {
        return boardService.updateBoard(id, updatedBoard, authentication);
    }
}