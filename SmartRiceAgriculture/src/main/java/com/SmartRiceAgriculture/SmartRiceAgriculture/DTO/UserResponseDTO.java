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
public class UserResponseDTO {
    private String nic;
    private String fullName;
    private String username;
    private String address;
    private String email;
    private String phoneNumber;
    private User.Role role;
    private User.Status status;

    // Role-specific fields will be set based on user role
    private String bankName;
    private String bankBranch;
    private String accountNumber;
    private String accountHolderName;
    private Double expectedCropAmount;
    private String storeLocation;
    private String companyName;
    private String businessRegNumber;
}
