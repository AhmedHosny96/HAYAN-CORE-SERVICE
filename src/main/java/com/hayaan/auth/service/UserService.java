package com.hayaan.auth.service;

import com.hayaan.auth.object.dto.*;
import com.hayaan.auth.object.entity.Role;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.RoleRepo;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.config.UtilService;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.repo.AgentRepo;
import com.hayaan.flight.repo.CommissionRepo;
import com.hayaan.notification.NotificationService;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final RoleRepo roleRepo;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UtilService utilService;

    private final AgentRepo agentRepo;

    private final CommissionRepo commissionRepo;

    private final NotificationService notificationService;

    // TODO: 2/14/2024 USER AND USER ROLE ALL RELATED FEATURES

    // ROLES

    public RolesResponse getAllRoles() {

        String loggedInUser = getLoggedInUserRole();

        log.info("logged in user : {}", loggedInUser);


        return RolesResponse.builder()
                .status(200)
                .message("success")
                .roles(roleRepo.findAll())
                .build();
    }

    // TODO: 11/26/2024 all users


    public AllUserResp getAllUsers() {

        List<User> all = userRepository.findAll();

        return AllUserResp.builder()
                .status(200)
                .message("success")
                .users(all)
                .build();
    }


    public Role getRoleById(int id) {
        return roleRepo.findById(id).orElse(null);
    }


    public String getLoggedInUserCurrency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return null; // Or throw an exception or return a default currency
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return null; // Or handle the case where the user is not found
        }

        User user = userOptional.get();

        return commissionRepo.findByUser(user)
                .filter(commissionList -> !commissionList.isEmpty())
                .map(commissionList -> commissionList.get(0).getCurrency())
                .orElse(null); // Or handle the case where no commissions are found
    }

    public CustomResponse createRole(CreateRoleDto roleDto) {
        Optional<Role> existingRole = roleRepo.findByName(roleDto.name());

        if (existingRole.isPresent()) {
            return new CustomResponse(400, "Role with the same name already exists", null);
        }

        var role = Role.builder()
                .name(roleDto.name())
                .description(roleDto.description())
                .status(1)
                .build();
        roleRepo.save(role);

        return new CustomResponse(200, "Role created successfully", null);
    }

    public String getLoggedInUserRole() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userType = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            Optional<User> byUsername = userRepository.findByUsername(username);


            User user = byUsername.get();

            userType = user.getRole().getName();

            return userType;
        } else {

            return userType;
        }

    }

    // TODO USER BY ID FOR PROFILE

    public UserDetailsResp getUserDetails(Long userId) {

        Optional<User> byId = userRepository.findById(userId);

        if (!byId.isPresent()) {

            return UserDetailsResp.builder()
                    .status(400)
                    .message("Invalid User ID")
                    .build();
        }


        User user = byId.get();

        return UserDetailsResp.builder()
                .status(200)
                .message("Success")
                .user(user)
                .build();
    }

    // CHANGE PASSWORD

    public CustomResponse changePassword(Long userId, ChangePasswordDto changePasswordDto) {


        Optional<User> byId = userRepository.findById(userId);


        if (!byId.isPresent()) {
            return new CustomResponse(400, "Invalid User ID", null);
        }

        User existingUser = byId.get();

        boolean matches = passwordEncoder.matches(changePasswordDto.oldPassword(), existingUser.getPassword());

        if (!matches) {
            return new CustomResponse(400, "Wrong old password", null);
        }

        existingUser.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
        existingUser.setStatus(1); // active
        existingUser.setPasswordChanged(true);

        userRepository.save(existingUser);

        return new CustomResponse(200, "Password changed successfully", null);

    }


    // CREATE NEW USER

    @SneakyThrows
    @Transactional
    public CustomResponse createUser(CreateUserDto userDto) {


        Optional<User> byUsername = userRepository.findByUsername(userDto.username());

        if (byUsername.isPresent()) {
            return new CustomResponse(400, "Username is taken", null);

        }

        Optional<User> byEmail = userRepository.findByEmail(userDto.email());

        if (byEmail.isPresent()) {
            return new CustomResponse(400, "Email ID is taken", null);

        }

        Optional<User> byPhoneNumber = userRepository.findByPhoneNumber(userDto.email());

        if (byPhoneNumber.isPresent()) {
            return new CustomResponse(400, "Phone number is taken", null);
        }


        String generatedPassword = utilService.generatePassword();

        log.info("USER : {} GENERATED PASSWORD", userDto.email());
        String hashedPassword = passwordEncoder.encode(generatedPassword);

        var role = roleRepo.findById(userDto.roleId());


        if (!role.isPresent()) {
            return new CustomResponse(400, "RoleId not found", null);

        }

        User user = User.builder()
                .username(userDto.username())
                .email(userDto.email())
                .phoneNumber(userDto.phoneNumber())
                .fullName(userDto.fullName())
                .agent(null)
                .role(role.get())
                .status(0) // 0 : created , 1: active , 2 :inactive
                .isPasswordChanged(false)
                .createdDate(LocalDateTime.now())
                .password(hashedPassword)
                .build();

        // Save the user

        userRepository.save(user);

        // send email

        Context context = new Context();
        context.setVariable("username", userDto.username());
        context.setVariable("otp", generatedPassword);
        context.setVariable("currentYear", LocalDate.now().getYear());

        notificationService.sendMail(userDto.email(), "Onetime password", "user-credentials", context, Optional.empty());
        // send sms
        String smsBody = "Dear " + userDto.username() + ", your one-time password is: " + generatedPassword + " Remember to change it after login.";

        notificationService.sendSms(user.getPhoneNumber(), smsBody);


        return new CustomResponse(200, "User created successfully", null);

    }

    public CustomResponse updateUser(Long userId, UpdateUserDto updateUserDto) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return new CustomResponse(404, "User not found", null);
            }

            User user = userOpt.get();

            // Update fields if provided
            if (updateUserDto.getUsername() != null && !updateUserDto.getUsername().trim().isEmpty()) {
                // Check if username already exists for another user
                Optional<User> existingUser = userRepository.findByUsername(updateUserDto.getUsername());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    return new CustomResponse(409, "Username already exists", null);
                }
                user.setUsername(updateUserDto.getUsername().trim());
            }

            if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().trim().isEmpty()) {
                // Check if email already exists for another user
                Optional<User> existingUser = userRepository.findByEmail(updateUserDto.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    return new CustomResponse(409, "Email already exists", null);
                }
                user.setEmail(updateUserDto.getEmail().trim());
            }

            if (updateUserDto.getPhoneNumber() != null && !updateUserDto.getPhoneNumber().trim().isEmpty()) {
                // Check if phone number already exists for another user
                Optional<User> existingUser = userRepository.findByPhoneNumber(updateUserDto.getPhoneNumber());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    return new CustomResponse(409, "Phone number already exists", null);
                }
                user.setPhoneNumber(updateUserDto.getPhoneNumber().trim());
            }

            if (updateUserDto.getFullName() != null && !updateUserDto.getFullName().trim().isEmpty()) {
                user.setFullName(updateUserDto.getFullName().trim());
            }

            if (updateUserDto.getRoleId() != null) {
                Optional<Role> roleOpt = roleRepo.findById(updateUserDto.getRoleId().intValue());
                if (roleOpt.isEmpty()) {
                    return new CustomResponse(400, "Invalid role ID", null);
                }
                user.setRole(roleOpt.get());
            }

            if (updateUserDto.getStatus() != null) {
                user.setStatus(updateUserDto.getStatus());
            }

            userRepository.save(user);

            log.info("User updated successfully: {}", userId);
            return new CustomResponse(200, "User updated successfully", null);

        } catch (Exception e) {
            log.error("Error updating user", e);
            return new CustomResponse(500, "Unable to update user", null);
        }
    }

    public CustomResponse authenticateWithGoogle(GoogleAuthDto googleAuthDto) {
        try {
            if (googleAuthDto.getGoogleToken() == null || googleAuthDto.getGoogleToken().trim().isEmpty()) {
                return new CustomResponse(400, "Google token is required", null);
            }

            if (googleAuthDto.getEmail() == null || googleAuthDto.getEmail().trim().isEmpty()) {
                return new CustomResponse(400, "Email is required from Google", null);
            }

            // Check if user already exists
            Optional<User> existingUser = userRepository.findByEmail(googleAuthDto.getEmail());
            
            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                log.info("Existing user authenticated with Google: {}", user.getEmail());
            } else {
                // Create new user
                Optional<Role> customerRole = roleRepo.findByName("CUSTOMER");
                if (customerRole.isEmpty()) {
                    return new CustomResponse(500, "Customer role not found", null);
                }

                String fullName = "";
                if (googleAuthDto.getFirstName() != null) {
                    fullName += googleAuthDto.getFirstName();
                }
                if (googleAuthDto.getLastName() != null) {
                    fullName += " " + googleAuthDto.getLastName();
                }

                // Generate username from email
                String username = googleAuthDto.getEmail().split("@")[0];
                
                // Check if username exists and make it unique if necessary
                int counter = 1;
                String originalUsername = username;
                while (userRepository.findByUsername(username).isPresent()) {
                    username = originalUsername + counter;
                    counter++;
                }

                user = User.builder()
                        .username(username)
                        .email(googleAuthDto.getEmail().toLowerCase().trim())
                        .fullName(fullName.trim())
                        .role(customerRole.get())
                        .status(1) // Active
                        .isPasswordChanged(true) // Google users don't need to change password
                        .createdDate(LocalDateTime.now())
                        .password(passwordEncoder.encode("GOOGLE_AUTH_" + System.currentTimeMillis())) // Random password
                        .build();

                userRepository.save(user);
                log.info("New user created via Google auth: {}", user.getEmail());
            }

            return new CustomResponse(200, "Google authentication successful", null);

        } catch (Exception e) {
            log.error("Error with Google authentication", e);
            return new CustomResponse(500, "Unable to authenticate with Google", null);
        }
    }
}
