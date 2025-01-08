package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class BankTransferRequest {
    private String senderBankName;
    private String senderAccountNumber;
    private String senderAccountName;
    private LocalDateTime transferDate;
    private MultipartFile transferProof;
}
