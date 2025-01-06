package com.SmartRiceAgriculture.SmartRiceAgriculture.Dto;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String nic;
    private String fullName;
    private String username;
    private String password;
    private String address;
    private String email;
    private String phoneNumber;
    private User.Role role;
    private User.Status status;

    // Farmer fields
    private String bankName;
    private String bankBranch;
    private String accountNumber;
    private String accountHolderName;
    private Double expectedCropAmount;
    private List<LandDto> lands;
    private List<RiceVarietyDto> riceVarieties;
    private FertilizerQuotaDto fertilizerQuota;

    // Buyer fields
    private String storeLocation;
    private String companyName;
    private String businessRegNumber;
    private List<TransactionDto> purchaseHistory;
}