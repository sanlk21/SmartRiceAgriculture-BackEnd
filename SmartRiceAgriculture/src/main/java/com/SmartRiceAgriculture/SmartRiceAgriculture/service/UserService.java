package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LoginDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.UserDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.UserResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.UserRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    // Add getUsersByRole method
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO registerUser(UserDTO userDTO) {
        try {
            validateUniqueFields(userDTO);

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

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getPaginatedUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByNic(String nic) {
        User user = userRepository.findById(nic)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return convertToResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(String nic, UserDTO userDTO) {
        User user = userRepository.findById(nic)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());

        if (userDTO.getRole() == User.Role.FARMER) {
            setFarmerFields(user, userDTO);
        } else if (userDTO.getRole() == User.Role.BUYER) {
            setBuyerFields(user, userDTO);
        }

        User updatedUser = userRepository.save(user);
        return convertToResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(String nic) {
        if (!userRepository.existsById(nic)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(nic);
    }

    @Transactional
    public UserResponseDTO updateUserStatus(String nic, User.Status status) {
        User user = userRepository.findById(nic)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setStatus(status);
        return convertToResponseDTO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(String searchTerm) {
        return userRepository.findByFullNameContainingOrEmailContainingOrNicContaining(
                        searchTerm, searchTerm, searchTerm)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private void validateUniqueFields(UserDTO userDTO) {
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
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
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