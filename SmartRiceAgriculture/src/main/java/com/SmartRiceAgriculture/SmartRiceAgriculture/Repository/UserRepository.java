package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);  // Change from User to Optional<User>
}