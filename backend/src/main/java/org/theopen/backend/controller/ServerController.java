package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.theopen.backend.dto.ServerDto;
import org.theopen.backend.service.ServerService;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @GetMapping("/available")
    public ResponseEntity<List<ServerDto>> getAvailableServers() {
        List<ServerDto> servers = serverService.getAllServers();
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerDto> getServerById(@PathVariable Long id) {
        ServerDto server = serverService.getServerById(id);
        return ResponseEntity.ok(server);
    }
}