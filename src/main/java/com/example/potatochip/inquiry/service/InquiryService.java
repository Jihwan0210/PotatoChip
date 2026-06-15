package com.example.potatochip.inquiry.service;

import com.example.potatochip.inquiry.dto.InquiryAnswerRequest;
import com.example.potatochip.inquiry.dto.InquiryDTO;

import java.util.List;

public interface InquiryService {

    InquiryDTO createInquiry(InquiryDTO inquiryDTO);

    List<InquiryDTO> getMyInquiries(Long userId);

    InquiryDTO getMyInquiryDetail(Long inquiryId, Long userId);

    InquiryDTO updateInquiry(Long inquiryId, InquiryDTO inquiryDTO);

    void deleteInquiry(Long inquiryId, Long userId);

    List<InquiryDTO> getAdminInquiries(String status);

    InquiryDTO getAdminInquiryDetail(Long inquiryId);

    InquiryDTO answerInquiry(Long inquiryId, InquiryAnswerRequest request);
}