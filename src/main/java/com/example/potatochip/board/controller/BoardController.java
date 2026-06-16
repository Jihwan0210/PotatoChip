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


    @PostMapping("/api/board")
    @ResponseBody
    public Board createBoard(
            @RequestBody Board board,
            Authentication authentication
    ) {

        String email = authentication.getName();

        board.setAuthor(email);

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