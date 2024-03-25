package com.hayaan.flight.service;

import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.AgentStatusChangeDto;
import com.hayaan.flight.object.dto.CreateAgentDto;
import com.hayaan.flight.object.dto.CreateAgentTypeDto;
import com.hayaan.flight.object.entity.Agent;
import com.hayaan.flight.object.entity.AgentType;
import com.hayaan.flight.repo.AgentRepo;
import com.hayaan.flight.repo.AgentTypeRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepo agentRepo;
    private final AgentTypeRepo agentTypeRepo;

    private final ModelMapper modelMapper;

    // AGENT TYPE METHODS
    public CustomResponse createAgentType(CreateAgentTypeDto agentTypeDto) {
        Optional<AgentType> byType = agentTypeRepo.findByType(agentTypeDto.type());
        if (byType.isPresent()) {
            return new CustomResponse(400, "Type already exists");
        }
        var createAgentType = new AgentType();
        createAgentType.setType(agentTypeDto.type());
        agentTypeRepo.save(createAgentType);

        var customResponse = new CustomResponse(200, "AgentType created successfully");
        return customResponse;
    }

    public List<AgentType> getAllTypes() {
        return agentTypeRepo.findAll();
    }

    public CustomResponse updateAgentType(int id, CreateAgentTypeDto updateAgentTypeDto) {
        Optional<AgentType> optionalAgentType = agentTypeRepo.findById(id);
        if (optionalAgentType.isEmpty()) {
            return new CustomResponse(404, "AgentType not found");
        }

        AgentType agentType = optionalAgentType.get();
        agentType.setType(updateAgentTypeDto.type());
        agentTypeRepo.save(agentType);

        return new CustomResponse(200, "AgentType updated successfully");
    }

    public Optional<AgentType> getAgentTypeById(int id) {
        return agentTypeRepo.findById(id);
    }

    // AGENT FEATURES
    public CustomResponse createAgent(CreateAgentDto createAgentDto) {
        Optional<Agent> byName = agentRepo.findByName(createAgentDto.getName());

        if (byName.isPresent()) {
            return new CustomResponse(400, "Type already exists");
        }

        AgentType agentType = agentTypeRepo.findById(createAgentDto.getTypeId()).get();
        var agent = Agent.builder()
                .name(createAgentDto.getName())
                .contactPerson(createAgentDto.getContactPerson())
                .country(createAgentDto.getCountry())
                .city(createAgentDto.getCity())
                .type(agentType)
                .contactEmail(createAgentDto.getContactEmail())
                .status(1) // Set the status here
                .build();
        agentRepo.save(agent);

        return new CustomResponse(200, "Agent created successfully");
    }

    public List<?> getAllAgents() {
        return agentRepo.findAll();
    }

    public CustomResponse updateAgent(Long id, CreateAgentDto createAgentDto) {
        Optional<Agent> byId = agentRepo.findById(id);
        if (byId.isEmpty()) {
            return new CustomResponse(404, "AgentType not found");
        }

        ModelMapper modelMapper = new ModelMapper();
        Agent agent = byId.get();
        modelMapper.map(createAgentDto, agent);
        agent.setStatus(0);

        agentRepo.save(agent);

        return new CustomResponse(200, "AgentType updated successfully");
    }

    // AGENT STATUS CHANGE 0/1 ->
    public CustomResponse changeAgentStatus(AgentStatusChangeDto statusChangeDto) {
        Optional<Agent> optionalAgent = agentRepo.findById(statusChangeDto.agentId());
        if (optionalAgent.isPresent()) {
            Agent agent = optionalAgent.get();
            agent.setStatus(statusChangeDto.status());
            agentRepo.save(agent);
            return new CustomResponse(200, "Agent status changed successfully");
        } else {
            return new CustomResponse(404, "Agent not found");
        }
    }
    
    public Agent getAgentById(Long id) {
        Optional<Agent> byId = agentRepo.findById(id);
//        if (byId.isEmpty()) {
//            return new CustomResponse(404, "AgentType not found");
//        }
        return byId.get();
    }


}
