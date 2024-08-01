package com.hayaan.auth.service;

import com.hayaan.auth.object.dto.ChangePasswordDto;
import com.hayaan.auth.object.dto.CreateRoleDto;
import com.hayaan.auth.object.dto.CreateUserDto;
import com.hayaan.auth.object.dto.UserDetailsResp;
import com.hayaan.auth.object.entity.Role;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.RoleRepo;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.repo.AgentRepo;
import com.hayaan.config.UtilService;
import com.hayaan.notification.NotificationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final RoleRepo roleRepo;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UtilService utilService;

    private final AgentRepo agentRepo;

    private final NotificationService notificationService;

    // TODO: 2/14/2024 USER AND USER ROLE ALL RELATED FEATURES

    // ROLES

    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    public Role getRoleById(int id) {
        return roleRepo.findById(id).orElse(null);
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


        existingUser.setPassword(changePasswordDto.newPassword());
        existingUser.setStatus(1); // active
        existingUser.setPasswordChanged(true);

        userRepository.save(existingUser);

        return new CustomResponse(200, "Password changed successfully", null);

    }


    // CREATE NEW USER
    public CustomResponse createUser(CreateUserDto userDto) throws MessagingException {


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

        log.info("USER : {} GENERATED PASSWORD", userDto.email() );
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

        notificationService.sendMail(userDto.email(), "Onetime password", "user-credentials", context);
        // send sms
        String smsBody = "Dear " + userDto.username() + ", your one-time password is: " + generatedPassword + " Remember to change it after login.";

        notificationService.sendSms(user.getPhoneNumber(), smsBody);


        return new CustomResponse(200, "User created successfully", null);

    }
}
