package com.hayaan.flight.controller;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.FlightType;
import com.hayaan.flight.object.dto.*;
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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class CommissionController {

    private final CommissionService commissionService;

    @PostMapping("/commissions")
    public ResponseEntity<CustomResponse> createCommission(@RequestBody CreateCommissionDto createCommissionDto) {
        CustomResponse response = commissionService.commissionSetUp(createCommissionDto);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/commissions")
    public ResponseEntity<List<Commission>> getAllCommissions() {
        List<Commission> commissions = commissionService.getAllCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/commission/{commissionId}")
    public ResponseEntity<CommissionResponse> getCommissionById(@PathVariable Long commissionId) {
        CommissionResponse commissionById = commissionService.getCommissionById(commissionId);
        return ResponseEntity.status(commissionById.getStatus()).body(commissionById);
    }

    @GetMapping("/commissions/user/{userId}")
    public ResponseEntity<UserCommissionResponse> getCommissionByUser(@PathVariable Long userId) {
        UserCommissionResponse commissionByUser = commissionService.getCommissionByUser(userId);
        return ResponseEntity.status(commissionByUser.getStatus()).body(commissionByUser);
    }

    @PutMapping("/commission/{commissionId}")
    public ResponseEntity<CustomResponse> updateCommision(@PathVariable Long commissionId, @RequestBody UpdateCommissionDto updateCommissionDto) {
        CustomResponse customResponse = commissionService.updateComission(commissionId , updateCommissionDto);
        return ResponseEntity.status(customResponse.status()).body(customResponse);
    }
    @PutMapping("/commissions/user")
    public ResponseEntity<CustomResponse> updateClientCommission(@RequestParam Long userId, @RequestParam FlightType flightType, @RequestParam Double amount) {
        CustomResponse customResponse = commissionService.updateClientCommission(userId, flightType, amount);
        return ResponseEntity.status(customResponse.status()).body(customResponse);
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
    public ResponseEntity<CustomResponse> updateCommissionType(@PathVariable Long id, @RequestBody CreateCommissionTypeDto updateCommissionTypeDto) {
        CustomResponse response = commissionService.updateCommissionType(id, updateCommissionTypeDto);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/commission-types/{id}")
    public ResponseEntity<CommissionType> getCommissionTypeById(@PathVariable Long id) {
        Optional<CommissionType> commissionType = commissionService.getCommissionTypeById(id);
        return commissionType.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

}
