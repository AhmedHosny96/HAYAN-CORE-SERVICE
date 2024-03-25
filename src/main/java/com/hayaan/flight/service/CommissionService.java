package com.hayaan.flight.service;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.CreateCommissionDto;
import com.hayaan.flight.object.dto.CreateCommissionTypeDto;
import com.hayaan.flight.object.entity.Commission;
import com.hayaan.flight.object.entity.CommissionType;
import com.hayaan.flight.repo.CommissionRepo;
import com.hayaan.flight.repo.CommissionTypeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommissionService {


    private final CommissionTypeRepo commissionTypeRepo;

    private final CommissionRepo commissionRepo;

    // COMMISSIONS

    public CustomResponse createCommission(CreateCommissionDto createCommissionDto) {

        Optional<CommissionType> byId = commissionTypeRepo.findById(createCommissionDto.commissionTypeId());

        if (!byId.isPresent()) {
            return new CustomResponse(404, "CommissionType not found");
        }

        Commission commission = Commission.builder()
                .commissionTypeId(byId.get().getId())
                .value(createCommissionDto.value())
                .createdAt(LocalDateTime.now())
                .status(1)
                .build();

        commissionRepo.save(commission);

        return new CustomResponse(200, "Commission created successfully");
    }

    public List<Commission> getAllCommissions() {
        return commissionRepo.findAll();
    }

    public CustomResponse createCommissionType(CreateCommissionTypeDto commissionTypeDto) {
        Optional<CommissionType> byType = commissionTypeRepo.findByType(commissionTypeDto.type());
        if (byType.isPresent()) {
            return new CustomResponse(400, "Type already exists");
        }
        CommissionType commissionType = new CommissionType();
        commissionType.setType(commissionTypeDto.type());
        commissionType.setRate(commissionTypeDto.rate());
        commissionTypeRepo.save(commissionType);

        return new CustomResponse(200, "CommissionType created successfully");
    }

    public List<CommissionType> getAllCommissionTypes() {
        return commissionTypeRepo.findAll();
    }

    public CustomResponse updateCommissionType(int id, CreateCommissionTypeDto updateCommissionTypeDto) {
        Optional<CommissionType> optionalCommissionType = commissionTypeRepo.findById(id);
        if (optionalCommissionType.isEmpty()) {
            return new CustomResponse(404, "CommissionType not found");
        }

        CommissionType commissionType = optionalCommissionType.get();
        commissionType.setType(updateCommissionTypeDto.type());
        commissionTypeRepo.save(commissionType);

        return new CustomResponse(200, "CommissionType updated successfully");
    }

    public Optional<CommissionType> getCommissionTypeById(int id) {
        return commissionTypeRepo.findById(id);
    }


    //
    public double calculateCommission(double price) {
        // Retrieve commission rate from the database
        CommissionType commission = commissionTypeRepo.findFirstByOrderById();

        if (commission != null) {
            // Calculate commission based on percentage rate
            double percentageRate = commission.getRate();
            double commissionAmount = price * (percentageRate / 100.0);
            return Math.round(commissionAmount * 100.0) / 100.0; // Round to 2 decimal places
        } else {
            throw new IllegalArgumentException("Commission rate not found in the database.");
        }
    }
}
