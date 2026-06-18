package com.example.potatochip.board.service;

import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.entity.Comment;
import com.example.potatochip.board.repository.BoardRepository;
import com.example.potatochip.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    @Autowired
    private CommentRepository commentRepository;

    // 전체 조회
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    // 단건 조회
    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    // 저장
    public Board save(Board board) {
        if (board.getViewCount() == null) {
            board.setViewCount(0);
        }
        if (board.getCommentCount() == null) {
            board.setCommentCount(0);
        }
        if (board.getCreatedAt() == null) {
            board.setCreatedAt(java.time.LocalDateTime.now());
        }
        return boardRepository.save(board);
    }

    // 조회수 증가
    public void increaseView(Long id) {
        Board board = findById(id);
        if (board.getViewCount() == null) {
            board.setViewCount(0);
        }
        board.setViewCount(board.getViewCount() + 1);
        boardRepository.save(board);
    }
    //좋아요 로직
    public void toggleLike(Long id, boolean isCancel) {
        Board board = findById(id);

if (board == null) {
    throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id =" + id);
}

        if (board.getLikeCount() == null) {
            board.setLikeCount(0);
        }

        if (isCancel) {

            if (board.getLikeCount() > 0) {
                board.setLikeCount(board.getLikeCount() - 1);
            }
        } else {

            board.setLikeCount(board.getLikeCount() + 1);
        }
        boardRepository.saveAndFlush(board);
    }

    // 삭제
    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    /**
     * 1. 특정 게시글의 댓글 목록 조회
     * 순환 참조(500 에러)를 차단하기 위해 CommentRepository에서 직접 깔끔하게 배열로 가져옵니다.
     */
    public List<Comment> findCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);
    }

    /**
     * 2. 댓글 저장
     */
    public Comment saveComment(Long boardId, String content, String email) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + boardId));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setContent(content);
        comment.setAuthor(email);

        return commentRepository.save(comment);
    }

    /**
     * 3. 댓글 삭제
     */
    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. ID: " + commentId));

        // 작성자 검증
        if (!comment.getAuthor().equals(email)) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}