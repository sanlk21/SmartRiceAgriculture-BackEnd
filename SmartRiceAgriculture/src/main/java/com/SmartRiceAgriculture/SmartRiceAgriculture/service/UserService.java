package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.Dto.UserDto;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import com.SmartRiceAgriculture.SmartRiceAgriculture.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto createUser(UserDto userDto) {
        User user = convertToEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public UserDto getUserByNic(String nic) {
        User user = userRepository.findById(nic)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(String nic, UserDto userDto) {
        User existingUser = userRepository.findById(nic)
                .orElseThrow(() -> new RuntimeException("User not found"));

        updateUserFields(existingUser, userDto);
        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    public void deleteUser(String nic) {
        if (!userRepository.existsById(nic)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(nic);
    }

    private User convertToEntity(UserDto dto) {
        User user = new User();
        user.setNic(dto.getNic());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // Add this line
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus()); // Add status

        if (dto.getRole() == User.Role.FARMER) {
            user.setBankName(dto.getBankName());
            user.setBankBranch(dto.getBankBranch());
            user.setAccountNumber(dto.getAccountNumber());
            user.setAccountHolderName(dto.getAccountHolderName());
            user.setExpectedCropAmount(dto.getExpectedCropAmount());
        } else if (dto.getRole() == User.Role.BUYER) {
            user.setCompanyName(dto.getCompanyName());
            user.setBusinessRegNumber(dto.getBusinessRegNumber());
            user.setStoreLocation(dto.getStoreLocation());
        }

        return user;
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setNic(user.getNic());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());

        if (user.getRole() == User.Role.FARMER) {
            dto.setBankName(user.getBankName());
            dto.setBankBranch(user.getBankBranch());
            dto.setAccountNumber(user.getAccountNumber());
            dto.setAccountHolderName(user.getAccountHolderName());
            dto.setExpectedCropAmount(user.getExpectedCropAmount());
        } else if (user.getRole() == User.Role.BUYER) {
            dto.setCompanyName(user.getCompanyName());
            dto.setBusinessRegNumber(user.getBusinessRegNumber());
            dto.setStoreLocation(user.getStoreLocation());
        }

        return dto;
    }

    private void updateUserFields(User user, UserDto dto) {
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());

        if (user.getRole() == User.Role.FARMER) {
            user.setBankName(dto.getBankName());
            user.setBankBranch(dto.getBankBranch());
            user.setAccountNumber(dto.getAccountNumber());
            user.setAccountHolderName(dto.getAccountHolderName());
            user.setExpectedCropAmount(dto.getExpectedCropAmount());
        } else if (user.getRole() == User.Role.BUYER) {
            user.setCompanyName(dto.getCompanyName());
            user.setBusinessRegNumber(dto.getBusinessRegNumber());
            user.setStoreLocation(dto.getStoreLocation());
        }
    }
}