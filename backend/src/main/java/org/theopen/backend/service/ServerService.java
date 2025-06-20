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

    public Server getServerById(Long id) {
        return serverRepository.findById(id)
                .orElseThrow(ServerNotFoundException::new);
    }

    public Optional<Server> findRandom() {
        List<Server> servers = serverRepository.findAll();
        return servers.isEmpty() ? Optional.empty() :
            Optional.of(servers.get(ThreadLocalRandom.current().nextInt(servers.size())));
    }

    /**
     * Возвращает список уникальных серверов, по одному для каждой доступной страны.
     * Если в стране несколько серверов, выбирается первый.
     */
    public List<ServerDto> getAvailableCountries() {
        return serverRepository.findAll().stream()
                .filter(server -> server.getCountryEntity() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        server -> server.getCountryEntity().getId(),
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toList(),
                                list -> list.get(0)
                        )
                ))
                .values()
                .stream()
                .map(ServerDto::fromEntity)
                .toList();
    }
}
