package com.example.potatochip.board.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.board.dto.BoardResponse;
import com.example.potatochip.board.dto.CommentResponse;
import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.entity.Comment;
import com.example.potatochip.board.repository.BoardRepository;
import com.example.potatochip.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 전체 조회
    public List<BoardResponse> findAllResponse() {
        return boardRepository.findAll()
                .stream()
                .map(board -> BoardResponse.from(board, getDisplayNameByEmail(board.getAuthor())))
                .toList();
    }

    // 단건 조회
    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다."
                ));
    }

    public BoardResponse findResponseById(Long id) {
        Board board = findById(id);
        return BoardResponse.from(board, getDisplayNameByEmail(board.getAuthor()));
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

    // 게시글 생성 권한 포함
    public BoardResponse createBoard(Board board, Authentication authentication) {
        User loginUser = getLoginUser(authentication);

        if (isNotice(board.getCategory()) && !isAdmin(loginUser)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "공지사항은 관리자만 작성할 수 있습니다."
            );
        }

        board.setAuthor(loginUser.getEmail());

        Board saved = save(board);
        return BoardResponse.from(saved, getDisplayName(loginUser));
    }

    // 게시글 수정 권한 포함
    public BoardResponse updateBoard(Long id, Board updatedBoard, Authentication authentication) {
        User loginUser = getLoginUser(authentication);
        Board board = findById(id);

        boolean admin = isAdmin(loginUser);
        boolean owner = isOwner(board, loginUser);
        boolean notice = isNotice(board.getCategory());

        // 공지사항 수정은 관리자만 가능
        if (notice) {
            if (!admin) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "공지사항은 관리자만 수정할 수 있습니다."
                );
            }
        }
        // 일반 글 수정은 작성자 본인만 가능
        else {
            if (!owner) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "본인이 작성한 글만 수정할 수 있습니다."
                );
            }
        }

        // 일반 회원이 카테고리를 공지사항으로 바꾸는 것 차단
        if (isNotice(updatedBoard.getCategory()) && !admin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "공지사항으로 변경할 수 없습니다."
            );
        }

        // 관리자가 일반 회원 글을 수정하는 것 차단
        if (!notice && admin && !owner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "관리자는 일반 회원 글을 수정할 수 없습니다."
            );
        }

        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());
        board.setCategory(updatedBoard.getCategory());

        Board saved = save(board);
        return BoardResponse.from(saved, getDisplayNameByEmail(saved.getAuthor()));
    }

    // 게시글 삭제 권한 포함
    public void deleteBoard(Long id, Authentication authentication) {
        User loginUser = getLoginUser(authentication);
        Board board = findById(id);

        boolean admin = isAdmin(loginUser);
        boolean owner = isOwner(board, loginUser);
        boolean notice = isNotice(board.getCategory());

        // 공지사항 삭제는 관리자만
        if (notice && !admin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "공지사항은 관리자만 삭제할 수 있습니다."
            );
        }

        // 일반 글 삭제는 작성자 본인 또는 관리자
        if (!notice && !owner && !admin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "삭제 권한이 없습니다."
            );
        }

        boardRepository.delete(board);
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

    // 댓글 목록 조회
    public List<CommentResponse> findCommentResponsesByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId)
                .stream()
                .map(comment -> CommentResponse.from(comment, getDisplayNameByEmail(comment.getAuthor())))
                .toList();
    }

    // 댓글 저장
    public CommentResponse saveComment(Long boardId, String content, Authentication authentication) {
        User loginUser = getLoginUser(authentication);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 게시글이 존재하지 않습니다. ID: " + boardId
                ));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setContent(content);

        // DB에는 이메일 저장
        comment.setAuthor(loginUser.getEmail());

        Comment saved = commentRepository.save(comment);

        // 댓글 수 증가
        if (board.getCommentCount() == null) {
            board.setCommentCount(0);
        }
        board.setCommentCount(board.getCommentCount() + 1);
        boardRepository.save(board);

        return CommentResponse.from(saved, getDisplayName(loginUser));
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, Authentication authentication) {
        User loginUser = getLoginUser(authentication);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 댓글이 존재하지 않습니다. ID: " + commentId
                ));

        boolean admin = isAdmin(loginUser);
        boolean owner = comment.getAuthor().equals(loginUser.getEmail());

        // 댓글 삭제는 일단 작성자 또는 관리자 허용
        if (!owner && !admin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "댓글 삭제 권한이 없습니다."
            );
        }

        Board board = comment.getBoard();

        commentRepository.delete(comment);

        if (board.getCommentCount() != null && board.getCommentCount() > 0) {
            board.setCommentCount(board.getCommentCount() - 1);
            boardRepository.save(board);
        }
    }

    private User getLoginUser(Authentication authentication) {
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