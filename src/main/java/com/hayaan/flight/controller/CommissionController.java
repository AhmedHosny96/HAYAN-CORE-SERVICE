package com.hayaan.flight.controller;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.CreateCommissionDto;
import com.hayaan.flight.object.dto.CreateCommissionTypeDto;
import com.hayaan.flight.object.entity.Commission;
import com.hayaan.flight.object.entity.CommissionType;
import com.hayaan.flight.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommissionController {

    private final CommissionService commissionService;

    @PostMapping("/commissions")
    public ResponseEntity<CustomResponse> createCommission(@RequestBody CreateCommissionDto createCommissionDto) {
        CustomResponse response = commissionService.createCommission(createCommissionDto);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/commissions")
    public ResponseEntity<List<Commission>> getAllCommissions() {
        List<Commission> commissions = commissionService.getAllCommissions();
        return ResponseEntity.ok(commissions);
    }

    @PostMapping("/commission-types")
    public ResponseEntity<CustomResponse> createCommissionType(@RequestBody CreateCommissionTypeDto commissionTypeDto) {
        CustomResponse response = commissionService.createCommissionType(commissionTypeDto);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/commission-types")
    public ResponseEntity<List<CommissionType>> getAllCommissionTypes() {
        List<CommissionType> commissionTypes = commissionService.getAllCommissionTypes();
        return ResponseEntity.ok(commissionTypes);
    }

    @PutMapping("/commission-types/{id}")
    public ResponseEntity<CustomResponse> updateCommissionType(@PathVariable int id, @RequestBody CreateCommissionTypeDto updateCommissionTypeDto) {
        CustomResponse response = commissionService.updateCommissionType(id, updateCommissionTypeDto);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/commission-types/{id}")
    public ResponseEntity<CommissionType> getCommissionTypeById(@PathVariable int id) {
        Optional<CommissionType> commissionType = commissionService.getCommissionTypeById(id);
        return commissionType.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

}
