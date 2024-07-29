package com.hayaan.flight.service;


import com.hayaan.auth.object.entity.Role;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.FlightType;
import com.hayaan.flight.object.dto.CreateCommissionDto;
import com.hayaan.flight.object.dto.CreateCommissionTypeDto;
import com.hayaan.flight.object.dto.UserCommissionResponse;
import com.hayaan.flight.object.entity.Commission;
import com.hayaan.flight.object.entity.CommissionType;
import com.hayaan.flight.repo.CommissionRepo;
import com.hayaan.flight.repo.CommissionTypeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public double calculateCommission(double price) {
        // Retrieve commission rate from the database
        CommissionType commission = commissionTypeRepo.findFirstByOrderById();

        if (commission != null) {
            // Calculate commission based on percentage rate
            double percentageRate = 10;
            double commissionAmount = price * (percentageRate / 100.0);
            return Math.round(commissionAmount * 100.0) / 100.0; // Round to 2 decimal places
        } else {
            throw new IllegalArgumentException("Commission rate not found in the database.");
        }
    }
}
