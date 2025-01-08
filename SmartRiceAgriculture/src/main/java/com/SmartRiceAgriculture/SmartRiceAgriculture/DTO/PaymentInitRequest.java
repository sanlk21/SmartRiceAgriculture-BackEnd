package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;


import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Data
public class PaymentInitRequest {
    private Long orderId;
    private Payment.PaymentMethod paymentMethod;
}

