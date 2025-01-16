package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LoginDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.UserDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.UserResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.UserRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO registerUser(UserDTO userDTO) {
        try {
            // Validate unique fields
            validateUniqueFields(userDTO);

            // Create user entity
            User user = User.builder()
                    .nic(userDTO.getNic())
                    .fullName(userDTO.getFullName())
                    .username(userDTO.getUsername())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .address(userDTO.getAddress())
                    .email(userDTO.getEmail())
                    .phoneNumber(userDTO.getPhoneNumber())
                    .role(userDTO.getRole())
                    .status(User.Status.ACTIVE)
                    .build();

            // Set role-specific fields
            if (userDTO.getRole() == User.Role.FARMER) {
                setFarmerFields(user, userDTO);
            } else if (userDTO.getRole() == User.Role.BUYER) {
                setBuyerFields(user, userDTO);
            }

            User savedUser = userRepository.save(user);
            return convertToResponseDTO(savedUser);
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public UserResponseDTO login(LoginDTO loginDTO) {
        try {
            User user = userRepository.findByUsername(loginDTO.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                if (user.getStatus() != User.Status.ACTIVE) {
                    throw new RuntimeException("Account is not active");
                }
                return convertToResponseDTO(user);
            }
            throw new RuntimeException("Invalid password");
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    private void validateUniqueFields(UserDTO userDTO) {
        if (userDTO.getNic() == null || userDTO.getUsername() == null || userDTO.getEmail() == null) {
            throw new RuntimeException("NIC, username and email are required");
        }

        if (userRepository.existsById(userDTO.getNic())) {
            throw new RuntimeException("NIC already registered");
        }
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
    }

    private void setFarmerFields(User user, UserDTO userDTO) {
        user.setBankName(userDTO.getBankName());
        user.setBankBranch(userDTO.getBankBranch());
        user.setAccountNumber(userDTO.getAccountNumber());
        user.setAccountHolderName(userDTO.getAccountHolderName());
        user.setExpectedCropAmount(userDTO.getExpectedCropAmount());
    }

    private void setBuyerFields(User user, UserDTO userDTO) {
        user.setStoreLocation(userDTO.getStoreLocation());
        user.setCompanyName(userDTO.getCompanyName());
        user.setBusinessRegNumber(userDTO.getBusinessRegNumber());
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .nic(user.getNic())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .address(user.getAddress())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .bankName(user.getBankName())
                .bankBranch(user.getBankBranch())
                .accountNumber(user.getAccountNumber())
                .accountHolderName(user.getAccountHolderName())
                .expectedCropAmount(user.getExpectedCropAmount())
                .storeLocation(user.getStoreLocation())
                .companyName(user.getCompanyName())
                .businessRegNumber(user.getBusinessRegNumber())
                .build();
    }
}