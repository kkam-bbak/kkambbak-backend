package com.kkambbak.domain.roleplay.service;

import com.kkambbak.core.repository.roleplay.RoleplayScenariosRepository;
import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleplayService {
    private final RoleplayScenariosRepository roleplayScenariosRepository;

    public List<RoleplayResponseDto> getAllRoleplayScenarios() {
        return roleplayScenariosRepository.findAll()
                .stream()
                .map(RoleplayResponseDto::fromEntity)
                .toList();
    }
}
