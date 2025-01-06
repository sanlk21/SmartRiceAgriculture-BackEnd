package com.SmartRiceAgriculture.SmartRiceAgriculture;

import com.SmartRiceAgriculture.SmartRiceAgriculture.Dto.UserDto;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @GetMapping("/{nic}")
    public ResponseEntity<UserDto> getUser(@PathVariable String nic) {
        return ResponseEntity.ok(userService.getUserByNic(nic));
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{nic}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String nic, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(nic, userDto));
    }

    @DeleteMapping("/{nic}")
    public ResponseEntity<Void> deleteUser(@PathVariable String nic) {
        userService.deleteUser(nic);
        return ResponseEntity.ok().build();
    }
}