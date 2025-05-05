package com.hayaan.flight.service;


import com.hayaan.auth.object.entity.Role;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.FlightType;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.entity.Commission;
import com.hayaan.flight.object.entity.CommissionType;
import com.hayaan.flight.repo.CommissionRepo;
import com.hayaan.flight.repo.CommissionTypeRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionService {


    private final CommissionTypeRepo commissionTypeRepo;

    private final CommissionRepo commissionRepo;

    private final UserRepository userRepository;


    // COMMISSIONS
    public CustomResponse commissionSetUp(CreateCommissionDto createCommissionDto) {

        Optional<User> byId = userRepository.findById(createCommissionDto.userId());

        if (!byId.isPresent()) {
            return new CustomResponse(404, "Invalid user ID", null);
        }

        User user = byId.get();

        Role role = user.getRole();

        Optional<CommissionType> commissionType = commissionTypeRepo.findById(createCommissionDto.commissionTypeId());

        CommissionType type = commissionType.get();


        Optional<List<Commission>> userCommission = commissionRepo.findByUser(user);

        if (userCommission.get().size() == 2) {
            return new CustomResponse(400, "Can't create new commission record as both domestic and internation commission is set by this client ", null);
        }

        Commission commission = Commission.builder()
//                .commissionTypeId(byId.get().getId())
                .amount(createCommissionDto.amount())
                .flightType(createCommissionDto.flightType())
                .user(user)
                .createdAt(LocalDateTime.now())
                .commissionType(type)
                .currency(createCommissionDto.currency())
                .UserType(role.getName())
                .status(1)
                .build();

        commissionRepo.save(commission);

        return new CustomResponse(200, "Commission created successfully", null);
    }

    public List<Commission> getAllCommissions() {
        return commissionRepo.findAll();
    }


    // GET COMMISION BY USER TYPE

    public CustomResponse createCommissionType(CreateCommissionTypeDto commissionTypeDto) {
        Optional<CommissionType> byType = commissionTypeRepo.findByType(commissionTypeDto.type());
        if (byType.isPresent()) {
            return new CustomResponse(400, "Type already exists", null);
        }
        CommissionType commissionType = new CommissionType();
        commissionType.setType(commissionTypeDto.type());
//        commissionType.setRate(commissionTypeDto.rate());
        commissionTypeRepo.save(commissionType);

        return new CustomResponse(200, "CommissionType created successfully", null);
    }

    public List<CommissionType> getAllCommissionTypes() {
        return commissionTypeRepo.findAll();
    }

    public CommissionResponse getCommissionById(Long commissionId) {

        Optional<Commission> commissionOptional = commissionRepo.findById(commissionId);

        if (!commissionOptional.isPresent()) {

            return CommissionResponse.builder()
                    .status(400)
                    .message("commission not found")
                    .commission(null)
                    .build();
        }

        return
                CommissionResponse.builder()
                        .status(200)
                        .message("success")
                        .commission(commissionOptional.get())
                        .build();

    }

    // upadte commision by id

    public CustomResponse updateComission(Long commissionId, UpdateCommissionDto updateCommissionDto) {

        Optional<Commission> byId = commissionRepo.findById(commissionId);

        if (!byId.isPresent()) {
            return new CustomResponse(400, "Invalid commission id", null);
        }

        Optional<User> userById = userRepository.findById(updateCommissionDto.userId());

        if (!userById.isPresent()) {
            return new CustomResponse(400, "Invalid user id", null);
        }


        Optional<CommissionType> commissionTypeRepoById = commissionTypeRepo.findById(updateCommissionDto.commissionTypeId());

        if (!commissionTypeRepoById.isPresent()) {
            return new CustomResponse(400, "Invalid commision type id", null);
        }


        Commission existingCommission = byId.get();

        existingCommission.setAmount(updateCommissionDto.amount());
        existingCommission.setUser(userById.get());
        existingCommission.setCommissionType(commissionTypeRepoById.get());
        existingCommission.setFlightType(updateCommissionDto.flightType());
        existingCommission.setCurrency(updateCommissionDto.currency());


        commissionRepo.save(existingCommission);

        String message = String.format("Commission type %s 's amount is updated to %s successfully", existingCommission.getCommissionType().getType(), updateCommissionDto.amount());

        return new CustomResponse(200, message, null);

    }


    // update commission by user

    public CustomResponse updateClientCommission(Long userId, FlightType flightType, Double amount) {

        Optional<User> byId = userRepository.findById(userId);

        if (!byId.isPresent()) {
            return new CustomResponse(400, "Invalid user id", null);
        }

        User user = byId.get();

        Optional<Commission> byUserAndFlightType = commissionRepo.findByUserAndFlightType(user, flightType);

        if (!byUserAndFlightType.isPresent()) {
            return new CustomResponse(400, "Invalid user id or flight type", null);
        }

        Commission existingCommission = byUserAndFlightType.get();

        existingCommission.setAmount(amount);

        commissionRepo.save(existingCommission);

        String message = String.format("Commission type %s 's amount is updated to %s successfully", existingCommission.getCommissionType().getType(), amount);

        return new CustomResponse(200, message, null);

    }

    public UserCommissionResponse getCommissionByUser(Long userId) {

        Optional<User> userById = userRepository.findById(userId);

        if (!userById.isPresent()) {

            return UserCommissionResponse.builder()
                    .status(400)
                    .message("User not found")
                    .commissions(null)
                    .build();
        }


        User user = userById.get();

        Optional<List<Commission>> byCreatedBy = commissionRepo.findByUser(user);

        return
                UserCommissionResponse.builder()
                        .status(200)
                        .message("success")
                        .commissions(byCreatedBy.get())
                        .build();

    }

    public CustomResponse updateCommissionType(Long id, CreateCommissionTypeDto updateCommissionTypeDto) {
        Optional<CommissionType> optionalCommissionType = commissionTypeRepo.findById(id);
        if (optionalCommissionType.isEmpty()) {
            return new CustomResponse(404, "CommissionType not found", null);
        }

        CommissionType commissionType = optionalCommissionType.get();
        commissionType.setType(updateCommissionTypeDto.type());
        commissionTypeRepo.save(commissionType);

        return new CustomResponse(200, "CommissionType updated successfully", null);
    }

    public Optional<CommissionType> getCommissionTypeById(Long id) {
        return commissionTypeRepo.findById(id);
    }

    //
    public double calculateCommission(double price, FlightType flightType, String userType) {
        // Retrieve commission details from the repository
        List<Commission> commissions = commissionRepo.findByFlightTypeAndUserType(flightType, userType);

        if (commissions.isEmpty()) {
            throw new IllegalArgumentException("Commission not found for the given flight type and user type.");
        }

        for (Commission commission : commissions) {
            CommissionType commissionType = commission.getCommissionType();
            double amount = commission.getAmount();
            String type = commissionType.getType();

            if (type.equalsIgnoreCase("FIXED")) {
                return amount; // Fixed commission amount
            } else if (type.equalsIgnoreCase("PERCENTAGE")) {
                return Math.round(price * (amount / 100.0) * 100.0) / 100.0; // Percentage commission
            }
        }

        throw new IllegalArgumentException("Invalid commission type.");
    }

}
