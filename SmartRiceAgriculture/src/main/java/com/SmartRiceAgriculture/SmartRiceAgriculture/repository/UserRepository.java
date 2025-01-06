package com.SmartRiceAgriculture.SmartRiceAgriculture.repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRole(User.Role role);

    // Find users by phone number
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Find farmers by district (assuming address contains district)
    List<User> findByAddressContainingAndRole(String district, User.Role role);

    // Find active users
    List<User> findByStatus(User.Status status);

    // Find users by company name (for buyers)
    List<User> findByCompanyNameContaining(String companyName);

    // Find by NIC
    Optional<User> findByNic(String nic);
}