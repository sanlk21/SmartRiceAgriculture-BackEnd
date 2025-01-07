package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
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

    // Buyer fields
    private String storeLocation;
    private String companyName;
    private String businessRegNumber;
}

