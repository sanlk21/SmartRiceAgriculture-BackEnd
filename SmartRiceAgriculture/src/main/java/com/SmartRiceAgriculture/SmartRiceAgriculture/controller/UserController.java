package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LoginDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.UserDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.UserResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserDTO userDTO) {
        UserResponseDTO response = userService.registerUser(userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        UserResponseDTO response = userService.login(loginDTO);
        return ResponseEntity.ok(response);
    }
}
