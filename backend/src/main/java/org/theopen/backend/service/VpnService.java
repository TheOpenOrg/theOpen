package org.theopen.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theopen.backend.dto.ConfigsRequestDto;
import org.theopen.backend.dto.VpnConfigResponseDto;
import org.theopen.backend.dto.PaymentRequestDto;
import org.theopen.backend.model.Config;
import org.theopen.backend.model.Payment;
import org.theopen.backend.model.Server;
import org.theopen.backend.repo.ConfigRepository;
import org.theopen.backend.repo.ServerRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VpnService {

    private final RestTemplate restTemplate;
    private final ServerRepository serverRepository;
    private final ConfigRepository configRepository;
    private final PaymentService paymentService;

    @Value("${vpn.server.url}")
    private String vpnServerUrl;

    @Value("${vpn.server.token}")
    private String vpnServerToken;

    @Value("${vpn.configs.storage.path}")
    private String configsStoragePath;

    /**
     * Получает конфигурационный файл OpenVPN для указанного клиента
     *
     * @param clientName имя клиента
     * @return объект с результатом операции и возможный конфигурационный файл
     */
    public VpnConfigResponseDto getClientConfig(String clientName) {
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

            String url = vpnServerUrl + "/api/configs/" + clientName;

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String fileName = clientName + ".ovpn";
                byte[] configData = response.getBody();

                // Сохраняем файл в хранилище
                saveConfigToStorage(clientName, configData);

                return VpnConfigResponseDto.builder()
                        .status("success")
                        .configFile(configData)
                        .fileName(fileName)
                        .build();
            } else {
                return VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Failed to get config")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting client config for {}: {}", clientName, e.getMessage(), e);
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Блокирует клиента OpenVPN
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    public VpnConfigResponseDto blockClient(String clientName) {
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = vpnServerUrl + "/api/block/" + clientName;

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return VpnConfigResponseDto.builder()
                        .status("success")
                        .message("Client blocked successfully")
                        .build();
            } else {
                return VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Failed to block client")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error blocking client {}: {}", clientName, e.getMessage(), e);
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Разблокирует клиента OpenVPN
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    public VpnConfigResponseDto unblockClient(String clientName) {
        try {
            HttpHeaders headers = createAuthHeaders();

            String url = vpnServerUrl + "/api/block/" + clientName;

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return VpnConfigResponseDto.builder()
                        .status("success")
                        .message("Client unblocked successfully")
                        .build();
            } else {
                return VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Failed to unblock client")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error unblocking client {}: {}", clientName, e.getMessage(), e);
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Создает новую конфигурацию OpenVPN для клиента
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    public VpnConfigResponseDto createClientConfig(String clientName) {
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = vpnServerUrl + "/api/create/" + clientName;

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Map.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                // После успешного создания, получаем конфигурационный файл
                return getClientConfig(clientName);
            } else {
                return VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Failed to create client config")
                        .build();
            }
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Error creating client config for {}: {}", clientName, e.getMessage());
            String userMessage = "Не удалось создать конфигурацию клиента. Попробуйте позже.";
            try {
                // Пытаемся извлечь текст ошибки из тела ответа
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("error")) {
                    // Примитивное извлечение текста ошибки из JSON
                    int idx = responseBody.indexOf(":");
                    int endIdx = responseBody.lastIndexOf('"');
                    if (idx > 0 && endIdx > idx) {
                        userMessage = responseBody.substring(idx + 2, endIdx);
                    }
                }
            } catch (Exception ignored) {
            }
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error(userMessage)
                    .build();
        } catch (Exception e) {
            log.error("Error creating client config for {}: {}", clientName, e.getMessage());
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error("Не удалось создать конфигурацию клиента. Попробуйте позже.")
                    .build();
        }
    }

    /**
     * Получает количество активных пользователей OpenVPN
     *
     * @return объект с результатом операции
     */
    public VpnConfigResponseDto getActiveUsers() {
        try {
            HttpHeaders headers = createAuthHeaders();

            String url = vpnServerUrl + "/api/active-users";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Integer activeUsers = (Integer) response.getBody().get("active_users");
                return VpnConfigResponseDto.builder()
                        .status("success")
                        .activeUsers(activeUsers)
                        .build();
            } else {
                return VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Failed to get active users count")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting active users count: {}", e.getMessage(), e);
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Создает заголовки с авторизационным токеном
     *
     * @return объект HttpHeaders с заголовками
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + vpnServerToken);
        return headers;
    }

    /**
     * Сохраняет конфигурационный файл в файловое хранилище
     *
     * @param clientName имя клиента
     * @param configData данные конфигурационного файла
     * @throws IOException если произошла ошибка при записи файла
     */
    private void saveConfigToStorage(String clientName, byte[] configData) throws IOException {
        // Определяем базовую директорию для хранения
        Path storagePath;
        if (configsStoragePath != null && !configsStoragePath.isBlank()) {
            storagePath = Paths.get(configsStoragePath);
        } else {
            // Если путь не задан, используем директорию рядом с jar-файлом
            String jarDir = System.getProperty("user.dir");
            storagePath = Paths.get(jarDir, "vpn-configs");
        }
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        // Формируем уникальное имя файла с датой создания
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = clientName + "_" + timestamp + ".ovpn";
        Path filePath = storagePath.resolve(fileName);

        // Сохраняем файл
        Files.write(filePath, configData);

        log.info("Config file for client {} saved to {}", clientName, filePath);
    }

    /**
     * Создает несколько конфигураций на наименее загруженном сервере выбранной страны
     *
     * @param request объект запроса с параметрами
     * @return список результатов создания конфигураций
     */
    public Optional<Payment> createMultipleConfigs(ConfigsRequestDto request) {
        log.info("Creating {} configs for country ID {} for {} months",
                request.getConfigsCount(), request.getCountryId(), request.getMonths());

        // 1. Создать платеж асинхронно
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setAmount((long) (request.getConfigsCount() * 1000)); // 100 рублей за конфигурацию
        paymentRequest.setDescription("Оплата theOpen-конфигураций");
        Optional<Payment> payment = paymentService.createPaymentLink(paymentRequest);

        // 2. Продолжаем создание конфигураций
        List<Server> servers = serverRepository.findByCountryEntityId(request.getCountryId());
        if (servers.isEmpty()) {
            log.error("No servers found for country ID {}", request.getCountryId());
            return Optional.empty();
        }
        Server leastLoadedServer = findLeastLoadedServer(servers);
        if (leastLoadedServer == null) {
            log.error("Failed to find least loaded server for country ID {}", request.getCountryId());
            return Optional.empty();
        }
        log.info("Selected server {} for creating configs", leastLoadedServer.getName());
        List<VpnConfigResponseDto> results = new ArrayList<>();
        for (int i = 0; i < request.getConfigsCount(); i++) {
            String clientName = generateClientName(request.getCountryId(), i);
            VpnConfigResponseDto result = createClientConfigOnServer(clientName, leastLoadedServer, request.getMonths(), payment);
            results.add(result);
            if (!"success".equals(result.getStatus())) {
                log.error("Error creating config {} of {}", i + 1, request.getConfigsCount());
                break;
            }
        }
        return payment;
    }

    /**
     * Находит наименее загруженный сервер из списка
     *
     * @param servers список серверов
     * @return наименее загруженный сервер или null в случае ошибки
     */
    private Server findLeastLoadedServer(List<Server> servers) {
        if (servers.isEmpty()) {
            return null;
        }

        // Если сервер всего один, возвращаем его
        if (servers.size() == 1) {
            return servers.get(0);
        }

        // Получаем загруженность для каждого сервера
        Map<Server, Integer> serverLoads = new HashMap<>();
        for (Server server : servers) {
            try {
                Integer activeUsers = getActiveUsersForServer(server);
                serverLoads.put(server, activeUsers != null ? activeUsers : Integer.MAX_VALUE);
            } catch (Exception e) {
                log.error("Error getting active users for server {}: {}", server.getName(), e.getMessage());
                serverLoads.put(server, Integer.MAX_VALUE);
            }
        }

        // Находим минимальную загрузку
        Optional<Integer> minLoad = serverLoads.values().stream().min(Integer::compareTo);

        // Получаем все серверы с минимальной загрузкой
        List<Server> leastLoadedServers = serverLoads.entrySet().stream()
                .filter(entry -> entry.getValue().equals(minLoad.get()))
                .map(Map.Entry::getKey)
                .toList();

        // Если несколько серверов с одинаковой минимальной загрузкой, выбираем случайный
        if (leastLoadedServers.size() > 1) {
            int randomIndex = new Random().nextInt(leastLoadedServers.size());
            return leastLoadedServers.get(randomIndex);
        } else {
            return leastLoadedServers.get(0);
        }
    }

    /**
     * Получает количество активных пользователей для конкретного сервера
     *
     * @param server сервер
     * @return количество активных пользователей или null в случае ошибки
     */
    private Integer getActiveUsersForServer(Server server) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + server.getApiToken());

            String url = server.getApiUrl() + "/api/active-users";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (Integer) response.getBody().get("active_users");
            }
        } catch (Exception e) {
            log.error("Error getting active users for server {}: {}", server.getName(), e.getMessage());
        }
        return null;
    }

    /**
     * Создает конфигурацию клиента на конкретном сервере
     *
     * @param clientName  имя клиента
     * @param server      сервер
     * @param monthAmount количество месяцев на которое оформляется подписка
     * @param payment     платеж
     * @return результат операции
     */
    private VpnConfigResponseDto createClientConfigOnServer(String clientName, Server server, Integer monthAmount, Optional<Payment> payment) {
        try {
//            TODO проверь если у пользователя уже была триал версия, то не давать создавать новую
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + server.getApiToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = server.getApiUrl() + "/api/create/" + clientName;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Map.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                Config config = new Config();
                config.setServer(server);
                config.setBuyTime(LocalDateTime.now());
                config.setIsActive(payment.isEmpty());
                config.setMonthAmount(monthAmount);
                config.setName(clientName);
                config.setPayment(payment.orElse(null));
                config.setIsTrial(payment.isEmpty());
                configRepository.save(config);
                return VpnConfigResponseDto.builder()
                        .status("success")
                        .message("Config successfully created and saved to database")
                        .build();
            } else {
                return VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Failed to create client config")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error creating client config for {}: {}", clientName, e.getMessage());
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error("Не удалось создать конфигурацию клиента. Попробуйте позже.")
                    .build();
        }
    }

    /**
     * Получает конфигурационный файл с конкретного сервера
     *
     * @param clientName имя клиента
     * @param server     сервер
     * @return результат операции с конфигурационным файлом
     */
    private VpnConfigResponseDto getClientConfigFromServer(String clientName, Server server) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + server.getApiToken());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

            String url = server.getApiUrl() + "/api/configs/" + clientName;

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String fileName = clientName + ".ovpn";
                byte[] configData = response.getBody();

                // Сохраняем файл в хранилище
                saveConfigToStorage(clientName, configData);

                return VpnConfigResponseDto.builder()
                        .status("success")
                        .configFile(configData)
                        .fileName(fileName)
                        .build();
            } else {
                return VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Failed to get config")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting client config for {}: {}", clientName, e.getMessage());
            return VpnConfigResponseDto.builder()
                    .status("error")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Генерирует уникальное имя клиента на основе страны и индекса
     */
    private String generateClientName(Long countryId, int index) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return "client-" + countryId + "-" + timestamp + "-" + index;
    }
}

