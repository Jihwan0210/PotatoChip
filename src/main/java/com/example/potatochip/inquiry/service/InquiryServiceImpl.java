package com.example.potatochip.inquiry.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.inquiry.dto.InquiryAnswerRequest;
import com.example.potatochip.inquiry.dto.InquiryDTO;
import com.example.potatochip.inquiry.entity.Inquiry;
import com.example.potatochip.inquiry.repository.InquiryRepository;
import com.example.potatochip.notification.service.NotificationService;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    private static final Long DEFAULT_ADMIN_ID = 1L;

    private final InquiryRepository inquiryRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public InquiryDTO createInquiry(InquiryDTO inquiryDTO) {
        validateUserId(inquiryDTO.getUserId());
        validateCategory(inquiryDTO.getCategory());
        validateTitle(inquiryDTO.getTitle());
        validateContent(inquiryDTO.getContent());
        normalizeInquiryTargetByRole(inquiryDTO);

        Inquiry inquiry = Inquiry.builder()
                .userId(inquiryDTO.getUserId())
                .productId(inquiryDTO.getProductId())
                .orderId(inquiryDTO.getOrderId())
                .orderItemId(inquiryDTO.getOrderItemId())
                .category(inquiryDTO.getCategory())
                .title(inquiryDTO.getTitle())
                .content(inquiryDTO.getContent())
                .imageUrl(normalizeImageUrl(inquiryDTO.getImageUrl()))
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return InquiryDTO.fromEntity(savedInquiry);
    }

    @Override
    public List<InquiryDTO> getMyInquiries(Long userId) {
        validateUserId(userId);
        return inquiryRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(InquiryDTO::fromEntity)
                .toList();
    }

    @Override
    public InquiryDTO getMyInquiryDetail(Long inquiryId, Long userId) {
        validateUserId(userId);
        Inquiry inquiry = findActiveInquiry(inquiryId);

        if (!inquiry.isWrittenBy(userId)) {
            throw new IllegalArgumentException("본인이 작성한 문의만 조회할 수 있습니다.");
        }

        return InquiryDTO.fromEntity(inquiry);
    }

    @Override
    @Transactional
    public InquiryDTO updateInquiry(Long inquiryId, InquiryDTO inquiryDTO) {
        validateUserId(inquiryDTO.getUserId());
        validateCategory(inquiryDTO.getCategory());
        validateTitle(inquiryDTO.getTitle());
        validateContent(inquiryDTO.getContent());
        normalizeInquiryTargetByRole(inquiryDTO);

        Inquiry inquiry = findActiveInquiry(inquiryId);

        if (!inquiry.isWrittenBy(inquiryDTO.getUserId())) {
            throw new IllegalArgumentException("본인이 작성한 문의만 수정할 수 있습니다.");
        }

        if ("answered".equals(inquiry.getStatus())) {
            throw new IllegalArgumentException("답변 완료된 문의는 수정할 수 없습니다.");
        }

        inquiry.updateInquiry(
                inquiryDTO.getCategory(),
                inquiryDTO.getTitle(),
                inquiryDTO.getContent(),
                inquiryDTO.getProductId(),
                inquiryDTO.getOrderId(),
                inquiryDTO.getOrderItemId(),
                normalizeImageUrl(inquiryDTO.getImageUrl())
        );

        return InquiryDTO.fromEntity(inquiry);
    }

    @Override
    @Transactional
    public void deleteInquiry(Long inquiryId, Long userId) {
        validateUserId(userId);
        Inquiry inquiry = findActiveInquiry(inquiryId);

        if (!inquiry.isWrittenBy(userId)) {
            throw new IllegalArgumentException("본인이 작성한 문의만 삭제할 수 있습니다.");
        }

        inquiry.deactivate();
    }

    @Override
    public List<InquiryDTO> getAdminInquiries(String status) {
        if (status != null && !status.isBlank() && !"all".equals(status)) {
            return inquiryRepository.findByStatusAndIsActiveTrueOrderByCreatedAtDesc(status)
                    .stream()
                    .map(this::toInquiryDTOWithUser)
                    .toList();
        }

        return inquiryRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toInquiryDTOWithUser)
                .toList();
    }

    @Override
    public InquiryDTO getAdminInquiryDetail(Long inquiryId) {
        Inquiry inquiry = findActiveInquiry(inquiryId);
        return toInquiryDTOWithUser(inquiry);
    }

    @Override
    @Transactional
    public InquiryDTO answerInquiry(Long inquiryId, InquiryAnswerRequest request) {
        if (request == null || request.getAnswer() == null || request.getAnswer().isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        Inquiry inquiry = findActiveInquiry(inquiryId);
        Long adminId = request.getAdminId() == null ? DEFAULT_ADMIN_ID : request.getAdminId();

        inquiry.answer(request.getAnswer(), adminId);

        notificationService.createInquiryAnsweredNotification(
                inquiry.getUserId(),
                inquiry.getId(),
                inquiry.getTitle()
        );

        return InquiryDTO.fromEntity(inquiry);
    }

    private InquiryDTO toInquiryDTOWithUser(Inquiry inquiry) {
        InquiryDTO dto = InquiryDTO.fromEntity(inquiry);

        userRepository.findById(inquiry.getUserId()).ifPresent(user -> {
            dto.setUserName(getUserDisplayName(user));
            if (user.getRole() != null) {
                String role = user.getRole().name();
                dto.setUserRole(role);
                dto.setUserRoleText(getUserRoleText(role));
            }
        });

        return dto;
    }

    private String getUserDisplayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }

        return user.getEmail();
    }

    private String getUserRoleText(String role) {
        if (role == null) {
            return "-";
        }

        return switch (role.toUpperCase()) {
            case "BUYER" -> "구매자";
            case "SELLER" -> "판매자";
            case "ADMIN" -> "관리자";
            default -> role;
        };
    }

    private void normalizeInquiryTargetByRole(InquiryDTO inquiryDTO) {
        User user = userRepository.findById(inquiryDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String role = user.getRole() == null ? "" : user.getRole().name().toUpperCase();
        String category = inquiryDTO.getCategory() == null ? "" : inquiryDTO.getCategory().toLowerCase();

        if ("SELLER".equals(role)) {
            inquiryDTO.setCategory("etc");
            inquiryDTO.setOrderId(null);
            inquiryDTO.setProductId(null);
            inquiryDTO.setOrderItemId(null);
            return;
        }

        if (!"BUYER".equals(role)) {
            inquiryDTO.setOrderId(null);
            inquiryDTO.setProductId(null);
            inquiryDTO.setOrderItemId(null);
            return;
        }

        boolean targetAllowed = "delivery".equals(category)
                || "refund".equals(category)
                || "order".equals(category)
                || "product".equals(category);

        if (!targetAllowed) {
            inquiryDTO.setOrderId(null);
            inquiryDTO.setProductId(null);
            inquiryDTO.setOrderItemId(null);
            return;
        }

        // 구매자 주문/상품 선택은 선택사항입니다.
        // 선택하지 않은 경우 일반 문의로 저장합니다.
        if (inquiryDTO.getOrderId() == null) {
            inquiryDTO.setProductId(null);
            inquiryDTO.setOrderItemId(null);
            return;
        }

        Order order = orderRepository.findById(inquiryDTO.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!Objects.equals(order.getBuyerId(), inquiryDTO.getUserId())) {
            throw new IllegalArgumentException("본인의 주문만 문의할 수 있습니다.");
        }

        if (inquiryDTO.getProductId() == null) {
            inquiryDTO.setOrderItemId(null);
            return;
        }

        OrderItem matchedItem = order.getOrderItems()
                .stream()
                .filter(item -> Objects.equals(item.getProductId(), inquiryDTO.getProductId()))
                .filter(item -> inquiryDTO.getOrderItemId() == null || Objects.equals(item.getId(), inquiryDTO.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("선택한 주문에 포함된 상품만 문의할 수 있습니다."));

        inquiryDTO.setOrderItemId(matchedItem.getId());
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return imageUrl.trim();
    }

    private Inquiry findActiveInquiry(Long inquiryId) {
        if (inquiryId == null) {
            throw new IllegalArgumentException("문의 ID가 필요합니다.");
        }

        return inquiryRepository.findByIdAndIsActiveTrue(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    }

    private void validateUserId(Long userId) {
        if (userId == null) throw new IllegalArgumentException("사용자 ID가 필요합니다.");
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) throw new IllegalArgumentException("문의 유형을 선택해주세요.");
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("문의 제목을 입력해주세요.");
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("문의 내용을 입력해주세요.");
    }
}
