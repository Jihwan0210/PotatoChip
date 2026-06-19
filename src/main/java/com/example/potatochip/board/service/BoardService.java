package com.example.potatochip.board.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.board.dto.BoardResponse;
import com.example.potatochip.board.dto.CommentResponse;
import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.entity.Comment;
import com.example.potatochip.board.entity.BoardLike;
import com.example.potatochip.board.repository.BoardRepository;
import com.example.potatochip.board.repository.CommentRepository;
import com.example.potatochip.board.repository.BoardLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정하여 성능 최적화
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;

    // 전체 조회
    public List<BoardResponse> findAllResponse() {
        return boardRepository.findAll()
                .stream()
                .map(board -> BoardResponse.from(board, getDisplayNameByEmail(board.getAuthor())))
                .toList();
    }

    // 단건 조회 (엔티티 반환)
    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));
    }

    // 단건 조회 (DTO 반환)
    public BoardResponse findResponseById(Long id) {
        Board board = findById(id);
        return BoardResponse.from(board, getDisplayNameByEmail(board.getAuthor()));
    }

    // 🌟 [인스타 토글] 좋아요 누르기 / 취소 로직
    @Transactional
    public int toggleLike(Long boardId, String userEmail) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."
                ));

        Optional<BoardLike> alreadyLike = boardLikeRepository.findByBoardAndUserEmail(board, userEmail);

        if (board.getLikeCount() == null) {
            board.setLikeCount(0);
        }

        if (alreadyLike.isPresent()) {
            // 1. 이미 눌려있다면: 좋아요 취소 (인스타 하트 불 끄기)
            boardLikeRepository.delete(alreadyLike.get());
            boardLikeRepository.flush();

            board.setLikeCount(Math.max(0, board.getLikeCount() - 1));
        } else {
            // 2. 안 눌려있다면: 좋아요 저장 (인스타 하트 불 켜기)
            BoardLike boardLike = new BoardLike(board, userEmail);
            boardLikeRepository.save(boardLike);

            board.setLikeCount(board.getLikeCount() + 1);
        }

        Board savedBoard = boardRepository.saveAndFlush(board);
        // 🌟 최신 변경 사항을 DB에 안전하게 밀어 넣고 반영된 좋아요 카운트 반환
        System.out.println("====== [좋아요 디버깅] 글ID: " + boardId + " | 현재 누적 좋아요 수: " + savedBoard.getLikeCount() + " ======");
        return savedBoard.getLikeCount();
    }

    // 🌟 현재 로그인한 사용자가 이 게시글에 좋아요를 눌렀는지 확인
    public boolean isLikedByUser(Long boardId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) return false;
        return boardLikeRepository.existsByBoardIdAndUserEmail(boardId, userEmail);
    }

    // 게시글 작성
    @Transactional
    public BoardResponse createBoard(Board board, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        if (isNotice(board.getCategory()) && !isAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "공지사항은 관리자만 작성할 수 있습니다."
            );
        }

        board.setAuthor(user.getEmail());
        if (board.getViewCount() == null) board.setViewCount(0);
        if (board.getCommentCount() == null) board.setCommentCount(0);
        if (board.getLikeCount() == null) board.setLikeCount(0);

        Board savedBoard = boardRepository.save(board);
        return BoardResponse.from(savedBoard, getDisplayName(user));
    }

    // 조회수 증가
    @Transactional
    public void increaseView(Long id) {
        Board board = findById(id);
        if (board.getViewCount() == null) board.setViewCount(0);
        board.setViewCount(board.getViewCount() + 1);
        boardRepository.save(board);
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long id, Authentication authentication) {
        Board board = findById(id);
        User user = getAuthenticatedUser(authentication);

        if (!isOwner(board, user) && !isAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "삭제 권한이 없습니다."
            );
        }

        boardRepository.delete(board);
    }

    // 게시글 수정
    @Transactional
    public BoardResponse updateBoard(Long id, Board updatedBoard, Authentication authentication) {
        Board board = findById(id);
        User user = getAuthenticatedUser(authentication);

        if (!isOwner(board, user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "수정 권한이 없습니다."
            );
        }

        if (isNotice(updatedBoard.getCategory()) && !isAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "공지사항 카테고리로 수정할 권한이 없습니다."
            );
        }

        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());
        board.setCategory(updatedBoard.getCategory());
        board.setImageUrl(updatedBoard.getImageUrl());

        boardRepository.save(board);
        return BoardResponse.from(board, getDisplayNameByEmail(board.getAuthor()));
    }

    // 특정 게시글의 댓글 조회
    public List<CommentResponse> findCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId)
                .stream()
                .map(comment -> CommentResponse.from(comment, getDisplayNameByEmail(comment.getAuthor())))
                .toList();
    }

    // 댓글 저장
    @Transactional
    public CommentResponse saveComment(Long boardId, String content, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."
                ));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setContent(content);
        comment.setAuthor(user.getEmail());

        Comment savedComment = commentRepository.save(comment);

        if (board.getCommentCount() == null) {
            board.setCommentCount(0);
        }
        board.setCommentCount(board.getCommentCount() + 1);
        boardRepository.save(board);

        return CommentResponse.from(savedComment, getDisplayName(user));
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."
                ));

        if (!comment.getAuthor().equals(user.getEmail()) && !isAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다."
            );
        }

        Board board = comment.getBoard();
        if (board != null && board.getCommentCount() != null) {
            board.setCommentCount(Math.max(0, board.getCommentCount() - 1));
            boardRepository.save(board);
        }

        commentRepository.delete(comment);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "사용자 정보를 찾을 수 없습니다."
                ));
    }

    private boolean isOwner(Board board, User user) {
        return board.getAuthor() != null
                && board.getAuthor().equals(user.getEmail());
    }

    private boolean isNotice(String category) {
        return "notice".equalsIgnoreCase(category);
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null
                && "ADMIN".equalsIgnoreCase(user.getRole().name());
    }

    private String getDisplayNameByEmail(String email) {
        if (email == null || email.isBlank()) {
            return "-";
        }

        return userRepository.findByEmail(email)
                .map(this::getDisplayName)
                .orElse(email);
    }

    private String getDisplayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }

        return user.getEmail();
    }
}
