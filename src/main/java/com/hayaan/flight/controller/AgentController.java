package com.hayaan.flight.controller;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.AgentStatusChangeDto;
import com.hayaan.flight.object.dto.CreateAgentDto;
import com.hayaan.flight.object.dto.CreateAgentTypeDto;
import com.hayaan.flight.object.entity.Agent;
import com.hayaan.flight.object.entity.AgentType;
import com.hayaan.flight.service.AgentService;
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

public class AgentController {

    // TODO: 2/13/2024 create agent , deactivate , update , getAll , getById

    private final AgentService agentService;

    // AGENT TYPE ENDPOINTS

    @PostMapping("/agent-types")
    public ResponseEntity<?> createAgentType(@RequestBody CreateAgentTypeDto agentTypeDto) {
        CustomResponse response = agentService.createAgentType(agentTypeDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

    @GetMapping("/agent-types")
    public ResponseEntity<List<AgentType>> getAllAgentTypes() {
        List<AgentType> agentTypes = agentService.getAllTypes();
        return new ResponseEntity<>(agentTypes, HttpStatus.OK);
    }

    @PutMapping("/agent-types/{id}")
    public ResponseEntity<CustomResponse> updateAgentType(@PathVariable int id, @RequestBody CreateAgentTypeDto updateAgentTypeDto) {
        CustomResponse response = agentService.updateAgentType(id, updateAgentTypeDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

    @GetMapping("/agent-types/{id}")
    public ResponseEntity<AgentType> getAgentTypeById(@PathVariable int id) {
        Optional<AgentType> agentType = agentService.getAgentTypeById(id);
        return agentType.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // AGENT ENDPOINTS

    @PostMapping("/agents")
    public ResponseEntity<?> createAgent(@RequestBody CreateAgentDto createAgentDto) {
        CustomResponse response = agentService.createAgent(createAgentDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

    // ACTIVATION AND DEACTIVATION
    @PostMapping("/agents/status")
    public ResponseEntity<CustomResponse> changeAgentStatus(@RequestBody AgentStatusChangeDto statusChangeDto) {
        CustomResponse response = agentService.changeAgentStatus(statusChangeDto);
        HttpStatus httpStatus = HttpStatus.valueOf(response.status());
        return new ResponseEntity<>(response, httpStatus);
    }

    @GetMapping("/agents")
    public ResponseEntity<List<?>> getAllAgents() {
        List<?> agents = agentService.getAllAgents();
        return new ResponseEntity<>(agents, HttpStatus.OK);
    }

    @PutMapping("/agents/{id}")
    public ResponseEntity<CustomResponse> updateAgent(@PathVariable Long id, @RequestBody CreateAgentDto createAgentDto) {
        CustomResponse response = agentService.updateAgent(id, createAgentDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
    }

    @GetMapping("/agents/{id}")
    public ResponseEntity<Agent> getAgentById(@PathVariable Long id) {
        Agent agent = agentService.getAgentById(id);
        if (agent != null) {
            return new ResponseEntity<>(agent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
