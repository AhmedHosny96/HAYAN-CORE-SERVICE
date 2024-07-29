package com.hayaan.auth.controller;


import com.hayaan.auth.object.dto.ChangePasswordDto;
import com.hayaan.auth.object.dto.CreateRoleDto;
import com.hayaan.auth.object.dto.CreateUserDto;
import com.hayaan.auth.object.dto.UserDetailsResp;
import com.hayaan.auth.object.entity.Role;
import com.hayaan.auth.service.UserService;
import com.hayaan.dto.CustomResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class UserController {

    private final UserService userService;

    // TODO: 2/14/2024 USER ROUTES

    @PostMapping("/users")
    public ResponseEntity<CustomResponse> createUser(@RequestBody CreateUserDto userDto) throws MessagingException {
        CustomResponse response = userService.createUser(userDto);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable Long userId) {
        UserDetailsResp userDetails = userService.getUserDetails(userId);
        return ResponseEntity.status(userDetails.getStatus()).body(userDetails);
    }

    // change password
    @PutMapping("/user/change-password/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId, @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        CustomResponse customResponse = userService.changePassword(userId, changePasswordDto);
        return ResponseEntity.status(customResponse.status()).body(customResponse);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = userService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @GetMapping("roles/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable int id) {
        Role role = userService.getRoleById(id);
        if (role != null) {
            return new ResponseEntity<>(role, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/roles")
    public ResponseEntity<CustomResponse> createRole(@RequestBody CreateRoleDto roleDto) {
        CustomResponse response = userService.createRole(roleDto);
        HttpStatus status = response.status() == 200 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // TODO: 2/14/2024 USER ROUTES
}
