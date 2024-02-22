package com.hayaan.auth.service;

import com.hayaan.auth.object.dto.CreateRoleDto;
import com.hayaan.auth.object.dto.CreateUserDto;
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
            return new CustomResponse(400, "Role with the same name already exists");
        }

        var role = Role.builder()
                .name(roleDto.name())
                .description(roleDto.description())
                .status(1)
                .build();
        roleRepo.save(role);

        return new CustomResponse(200, "Role created successfully");
    }


    // USERS
    public CustomResponse createUser(CreateUserDto userDto) throws MessagingException {

//        Optional<User> byUsernameOrEmailOrPhoneNumber = userRepository.findByUsernameOrEmailOrPhoneNumber(userDto.username(), userDto.email(), userDto.phoneNumber());

//        if (!byUsernameOrEmailOrPhoneNumber.isPresent()) {

        String generatedPassword = utilService.generatePassword(8);

        log.info("GENERATED PASSWORD : {}", generatedPassword);
        String hashedPassword = passwordEncoder.encode(generatedPassword);

        var agent = agentRepo.findById(userDto.agentId());


        var role = roleRepo.findById(userDto.roleId());

        if (!agent.isPresent()) {
            return new CustomResponse(400, "AgentId not found");

        }
        if (!role.isPresent()) {
            return new CustomResponse(400, "RoleId not found");

        }

        User user = User.builder()
                .username(userDto.username())
                .email(userDto.email())
                .phoneNumber(userDto.phoneNumber())
                .fullName(userDto.fullName())
                .agent(agent.get())
                .role(role.get())
                .status(0)
                .firstLogin(true)
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
        notificationService.sendMail(userDto.email(), "Onetime password", "email-template", context);
        // send sms
        String smsBody = "Dear " + userDto.username() + ", your one-time password is: " + generatedPassword + " Remember to change it after login.";
        notificationService.sendSms(user.getPhoneNumber(), smsBody);

        return new CustomResponse(200, "User created successfully");

    }
}
