package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Support;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class SupportDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String subject;
        private String question;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {
        private String answer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String userNic;
        private String adminNic;
        private String subject;
        private String question;
        private String answer;
        private Support.TicketStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Response(Support support) {
            this.id = support.getId();
            this.userNic = support.getUserNic();
            this.adminNic = support.getAdminNic();
            this.subject = support.getSubject();
            this.question = support.getQuestion();
            this.answer = support.getAnswer();
            this.status = support.getStatus();
            this.createdAt = support.getCreatedAt();
            this.updatedAt = support.getUpdatedAt();
        }
    }
}