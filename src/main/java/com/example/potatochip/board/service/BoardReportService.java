package com.example.potatochip.board.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.board.dto.BoardReportDTO;
import com.example.potatochip.board.dto.BoardReportRequest;
import com.example.potatochip.board.entity.Board;
import com.example.potatochip.board.entity.BoardReport;
import com.example.potatochip.board.repository.BoardReportRepository;
import com.example.potatochip.board.repository.BoardRepository;
import com.example.potatochip.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardReportService {

    private final BoardReportRepository boardReportRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public BoardReportDTO reportBoard(Long boardId, BoardReportRequest request, Authentication authentication) {
        User reporter = getAuthenticatedUser(authentication);
        Board board = findBoard(boardId);

        if (Boolean.FALSE.equals(board.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 게시글은 신고할 수 없습니다.");
        }

        if (board.getAuthor() != null && board.getAuthor().equals(reporter.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인이 작성한 글은 신고할 수 없습니다.");
        }

        if (boardReportRepository.existsByBoardIdAndReporterId(boardId, reporter.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신고한 글입니다.");
        }

        String reason = request == null ? null : request.getReason();
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "신고 사유를 입력해주세요.");
        }

        BoardReport report = new BoardReport();
        report.setBoard(board);
        report.setReporterId(reporter.getId());
        report.setReporterEmail(reporter.getEmail());
        report.setReporterName(getDisplayName(reporter));
        report.setReason(reason.trim());

        return BoardReportDTO.fromEntity(boardReportRepository.save(report));
    }

    public Map<String, Object> getMyReportStatus(Long boardId, Authentication authentication) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reported", false);

        if (authentication == null || !authentication.isAuthenticated()) {
            return result;
        }

        User user = getAuthenticatedUser(authentication);
        result.put("reported", boardReportRepository.existsByBoardIdAndReporterId(boardId, user.getId()));
        return result;
    }

    public List<Map<String, Object>> getAdminReportBoards(Authentication authentication) {
        validateAdmin(authentication);

        Map<Long, List<BoardReport>> grouped = boardReportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .collect(Collectors.groupingBy(report -> report.getBoard().getId(), LinkedHashMap::new, Collectors.toList()));

        return grouped.values()
                .stream()
                .map(reports -> {
                    Board board = reports.get(0).getBoard();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("boardId", board.getId());
                    item.put("title", board.getTitle());
                    item.put("authorEmail", board.getAuthor());
                    item.put("authorId", getUserIdByEmail(board.getAuthor()));
                    item.put("authorName", getUserNameByEmail(board.getAuthor()));
                    item.put("category", board.getCategory());
                    item.put("createdAt", board.getCreatedAt());
                    item.put("reportCount", reports.size());
                    item.put("hidden", Boolean.TRUE.equals(board.getHidden()));
                    item.put("active", !Boolean.FALSE.equals(board.getIsActive()));
                    return item;
                })
                .toList();
    }

    public Map<String, Object> getAdminBoardReportDetail(Long boardId, Authentication authentication) {
        validateAdmin(authentication);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        List<BoardReportDTO> reports = boardReportRepository.findByBoardIdOrderByCreatedAtDesc(boardId)
                .stream()
                .map(BoardReportDTO::fromEntity)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("boardId", board.getId());
        result.put("title", board.getTitle());
        result.put("content", board.getContent());
        result.put("authorEmail", board.getAuthor());
        result.put("authorId", getUserIdByEmail(board.getAuthor()));
        result.put("authorName", getUserNameByEmail(board.getAuthor()));
        result.put("category", board.getCategory());
        result.put("createdAt", board.getCreatedAt());
        result.put("reportCount", reports.size());
        result.put("hidden", Boolean.TRUE.equals(board.getHidden()));
        result.put("active", !Boolean.FALSE.equals(board.getIsActive()));
        result.put("reports", reports);
        return result;
    }

    @Transactional
    public Map<String, Object> hideBoard(Long boardId, Authentication authentication) {
        validateAdmin(authentication);
        Board board = findBoard(boardId);
        board.hide();
        notifyBoardOwner(board, true);
        return Map.of("message", "게시글이 숨김 처리되었습니다.", "boardId", boardId);
    }

    @Transactional
    public Map<String, Object> restoreBoard(Long boardId, Authentication authentication) {
        validateAdmin(authentication);
        Board board = findBoard(boardId);

        if (Boolean.FALSE.equals(board.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 게시글은 복구할 수 없습니다.");
        }

        board.setHidden(false);
        return Map.of("message", "게시글이 복구되었습니다.", "boardId", boardId);
    }

    @Transactional
    public Map<String, Object> deleteBoard(Long boardId, Authentication authentication) {
        validateAdmin(authentication);
        Board board = findBoard(boardId);
        board.deactivate();
        notifyBoardOwner(board, false);
        return Map.of("message", "게시글이 삭제 처리되었습니다.", "boardId", boardId);
    }

    private void notifyBoardOwner(Board board, boolean hidden) {
        Long userId = getUserIdByEmail(board.getAuthor());
        if (userId == null) return;

        if (hidden) {
            notificationService.createBoardHiddenNotification(userId, board.getId(), board.getTitle());
        } else {
            notificationService.createBoardDeletedNotification(userId, board.getId(), board.getTitle());
        }
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));
    }

    private void validateAdmin(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }
    }

    private Long getUserIdByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }

    private String getUserNameByEmail(String email) {
        if (email == null || email.isBlank()) return "-";
        return userRepository.findByEmail(email).map(this::getDisplayName).orElse(email);
    }

    private String getDisplayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) return user.getNickname();
        if (user.getName() != null && !user.getName().isBlank()) return user.getName();
        return user.getEmail();
    }
}
