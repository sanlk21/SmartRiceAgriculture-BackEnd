package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "user")
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String nic;  // Primary key (Unique Identifier)

    @Column(nullable = false)
    private String fullName; // Full Name of the user

    @Column(nullable = false)
    private String username; // Username for the user account

    @Column(nullable = false)
    private String password; // User account password

    @Column(nullable = false)
    private String address; // User's address

    @Column(nullable = false, unique = true)
    private String email; // Email of the user (must be unique)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Role: FARMER, BUYER, or ADMIN

    // Farmer-specific fields
    @ElementCollection
    @CollectionTable(name = "yield_addresses", joinColumns = @JoinColumn(name = "user_nic"))
    private List<String> yieldAddresses; // List of yield addresses for farmers

    @ElementCollection
    @CollectionTable(name = "crop_types", joinColumns = @JoinColumn(name = "user_nic"))
    private List<String> cropTypes; // List of crop types for farmers

    private String landSize; // Land size for farmers

    private String expectedCropAmount; // Expected crop amount for farmers

    // Buyer-specific field
    private String storeLocation; // Store location for buyers

    public enum Role {
        FARMER, BUYER, ADMIN
    }
}
