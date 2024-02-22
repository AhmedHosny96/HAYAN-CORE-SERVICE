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

        CommissionType commissionType = commissionTypeRepo.findById(createCommissionDto.commissionTypeId()).orElse(null);
        if (commissionType == null) {
            return new CustomResponse(404, "CommissionType not found");
        }

        var commissionTypes = commissionTypeRepo.findById(createCommissionDto.commissionTypeId()).get();


        Commission commission = Commission.builder()
                .commissionTypeId(commissionTypes)
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


}
