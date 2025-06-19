package org.theopen.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.theopen.backend.dto.BuyConfigRequest;
import org.theopen.backend.dto.ConfigDto;
import org.theopen.backend.exception.UserNotFoundException;
import org.theopen.backend.exception.ServerNotFoundException;
import org.theopen.backend.exception.ConfigNotFoundException;
import org.theopen.backend.exception.InactiveConfigException;
import org.theopen.backend.exception.TrialAlreadyUsedException;
import org.theopen.backend.model.Config;
import org.theopen.backend.model.Server;
import org.theopen.backend.model.User;
import org.theopen.backend.repo.ConfigRepository;
import org.theopen.backend.repo.ServerRepository;
import org.theopen.backend.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;
    private final UserRepository userRepository;
    private final ServerService serverService;
    private final ServerRepository serverRepository;

    public List<ConfigDto> getConfigsForUser(Long tgId) {
        return configRepository.findAllByUser_TgId(tgId).stream()
                .map(ConfigDto::fromEntity)
                .toList();
    }

    public ConfigDto createConfig(BuyConfigRequest request) {
        User user = userRepository.findById(request.getTgId())
                .orElseThrow(UserNotFoundException::new);
        Server server = serverRepository.findById(request.getServerId())
                .orElseThrow(ServerNotFoundException::new);

        Config config = new Config();
        config.setUser(user);
        config.setServer(server);
        config.setMonthAmount(request.getMonthAmount());
        config.setBuyTime(LocalDateTime.now());
        config.setIsActive(true);

        Config saved = configRepository.save(config);
        return ConfigDto.fromEntity(saved);
    }

    public ConfigDto renewConfig(Long id, int months) {
        Config config = configRepository.findById(id)
                .orElseThrow(ConfigNotFoundException::new);
        if (!config.getIsActive()) throw new InactiveConfigException();

        config.setMonthAmount(config.getMonthAmount() + months);
        return ConfigDto.fromEntity(configRepository.save(config));
    }

    public ConfigDto getTrialConfig(Long tgId) {
        User user = userRepository.findById(tgId)
                .orElseThrow(UserNotFoundException::new);

        boolean alreadyTried = configRepository.existsByUser_TgIdAndMonthAmount(user.getTgId(), 0);
        if (alreadyTried) throw new TrialAlreadyUsedException();

        Server random = serverService.findRandom().orElseThrow();
        Config config = new Config();
        config.setUser(user);
        config.setServer(random);
        config.setMonthAmount(0);
        config.setBuyTime(LocalDateTime.now());
        config.setIsActive(true);
        config.setIsTrial(true);

        return ConfigDto.fromEntity(configRepository.save(config));
    }

    public void deactivateConfig(Long id) {
        Config config = configRepository.findById(id)
                .orElseThrow(ConfigNotFoundException::new);
        config.setIsActive(false);
        configRepository.save(config);
    }
}

