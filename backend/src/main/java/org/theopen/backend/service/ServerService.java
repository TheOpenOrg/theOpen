package org.theopen.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.theopen.backend.dto.ServerDto;
import org.theopen.backend.exception.ServerNotFoundException;
import org.theopen.backend.model.Server;
import org.theopen.backend.repo.ServerRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;

    public List<ServerDto> getAllServers() {
        return serverRepository.findAll().stream()
                .map(ServerDto::fromEntity)
                .toList();
    }

    public ServerDto getServerById(Long id) {
        return serverRepository.findById(id)
                .map(ServerDto::fromEntity)
                .orElseThrow(ServerNotFoundException::new);
    }

    public Optional<Server> findRandom() {
        List<Server> servers = serverRepository.findAll();
        return servers.isEmpty() ? Optional.empty() :
            Optional.of(servers.get(ThreadLocalRandom.current().nextInt(servers.size())));
    }
}

