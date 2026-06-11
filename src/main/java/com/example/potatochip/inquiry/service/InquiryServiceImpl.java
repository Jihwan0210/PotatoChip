package com.example.potatochip.inquiry.service;

import com.example.potatochip.inquiry.dto.InquiryAnswerRequest;
import com.example.potatochip.inquiry.dto.InquiryDTO;
import com.example.potatochip.inquiry.entity.Inquiry;
import com.example.potatochip.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    private static final Long DEFAULT_ADMIN_ID = 1L;

    private final InquiryRepository inquiryRepository;

    @Override
    @Transactional
    public InquiryDTO createInquiry(InquiryDTO inquiryDTO) {
        validateUserId(inquiryDTO.getUserId());
        validateCategory(inquiryDTO.getCategory());
        validateTitle(inquiryDTO.getTitle());
        validateContent(inquiryDTO.getContent());

        Inquiry inquiry = Inquiry.builder()
                .userId(inquiryDTO.getUserId())
                .productId(inquiryDTO.getProductId())
                .orderId(inquiryDTO.getOrderId())
                .category(inquiryDTO.getCategory())
                .title(inquiryDTO.getTitle())
                .content(inquiryDTO.getContent())
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
                inquiryDTO.getOrderId()
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
                    .map(InquiryDTO::fromEntity)
                    .toList();
        }

        return inquiryRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(InquiryDTO::fromEntity)
                .toList();
    }

    @Override
    public InquiryDTO getAdminInquiryDetail(Long inquiryId) {
        Inquiry inquiry = findActiveInquiry(inquiryId);

        return InquiryDTO.fromEntity(inquiry);
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

        return InquiryDTO.fromEntity(inquiry);
    }

    private Inquiry findActiveInquiry(Long inquiryId) {
        if (inquiryId == null) {
            throw new IllegalArgumentException("문의 ID가 필요합니다.");
        }

        return inquiryRepository.findByIdAndIsActiveTrue(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("문의 유형을 선택해주세요.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("문의 제목을 입력해주세요.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해주세요.");
        }
    }
}