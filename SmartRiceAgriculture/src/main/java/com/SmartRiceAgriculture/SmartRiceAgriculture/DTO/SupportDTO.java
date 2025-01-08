package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Support;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class SupportDTO {

    // Request DTO for creating a new ticket
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String subject;
        private String question;
    }

    // Request DTO for answering a ticket
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {
        private String answer;
    }

    // Response DTO for support tickets
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
        private LocalDateTime createdAt;
        private LocalDateTime answeredAt;
        private Support.TicketStatus status;

        // Constructor to convert from entity to DTO
        public Response(Support support) {
            this.id = support.getId();
            this.userNic = support.getUserNic();
            this.adminNic = support.getAdminNic();
            this.subject = support.getSubject();
            this.question = support.getQuestion();
            this.answer = support.getAnswer();
            this.createdAt = support.getCreatedAt();
            this.answeredAt = support.getAnsweredAt();
            this.status = support.getStatus();
        }
    }
}