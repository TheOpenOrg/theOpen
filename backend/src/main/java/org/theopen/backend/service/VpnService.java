package org.theopen.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theopen.backend.dto.ConfigsRequestDto;
import org.theopen.backend.dto.VpnConfigResponseDto;
import org.theopen.backend.dto.PaymentRequestDto;
import org.theopen.backend.exception.ServerApiException;
import org.theopen.backend.exception.VpnConfigException;
import org.theopen.backend.model.Config;
import org.theopen.backend.model.Payment;
import org.theopen.backend.model.Server;
import org.theopen.backend.model.User;
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
    private final ServerService serverService;
    private final ConfigService configService;
    private final UserService userService;

    @Value("${vpn.configs.storage.path}")
    private String configsStoragePath;

    /**
     * Получает конфигурационный файл OpenVPN для указанного клиента
     * с расширенными проверками безопасности
     *
     * @param configId номер конфигурации
     * @param tgId идентификатор пользователя Telegram
     * @return объект с результатом операции и возможный конфигурационный файл
     */
    public VpnConfigResponseDto getClientConfig(UUID configId, Long tgId) {
        // Проверяем, что ID конфигурации не нулевой
        if (configId == null ) {
            throw new VpnConfigException("Некорректный ID конфигурации");
        }

        // Проверяем, что Telegram ID не нулевой
        if (tgId == null || tgId <= 0) {
            throw new VpnConfigException("Некорректный идентификатор пользователя");
        }

        // Получаем конфигурацию из базы данных
        Config config = configService.getConfigById(configId);

        // Проверяем, что конфигурация существует
        if (config == null) {
            log.warn("Попытка доступа к несуществующей конфигурации с ID: {}", configId);
            throw new VpnConfigException("Конфигурация не найдена");
        }

        // Проверяем, что у конфигурации есть вл��делец
        if (config.getUser() == null) {
            log.warn("Попытка доступа к конфигурации без владельца с ID: {}", configId);
            throw new VpnConfigException("Ошибка идентификации владельца конфигурации");
        }

        // Проверяем, что пользователь является владельцем конфигурации
        if (!Objects.equals(config.getUser().getTgId(), tgId)) {
            log.warn("Попытка несанкционированного доступа к конфигурации {} пользователем с TG ID: {}", configId, tgId);
            throw new VpnConfigException("У вас нет доступа к этой конфигурации");
        }

        // Проверяем, что конфигурация активна
        if (config.getIsActive() == null || !config.getIsActive()) {
            throw new VpnConfigException("Конфигурация неактивна или срок её действия истёк");
        }

        // Проверяем, что срок действия конфигурации не истек
        if (config.getBuyTime() != null && config.getMonthAmount() != null) {
            LocalDateTime expirationTime = config.getBuyTime().plusMonths(config.getMonthAmount());
            if (LocalDateTime.now().isAfter(expirationTime)) {
                log.info("Попытка доступа к просроченной конфигурации ID: {}", configId);
                throw new VpnConfigException("Срок действия конфигурации истек");
            }
        }

        // Проверяем, что сервер доступен
        if (config.getServer() == null) {
            throw new VpnConfigException("Ошибка получения информации о сервере ко��фигурации");
        }

        // Далее получаем конфигурацию с сервера
        HttpHeaders headers = createAuthHeaders(config.getServer().getApiToken());
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        String url = config.getServer().getApiUrl() + "/api/configs/" + config.getName();

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String fileName = config.getName() + ".ovpn";
            byte[] configData = response.getBody();

            // Сохраняем файл в хранилище
            try {
                saveConfigToStorage(config.getName(), configData);
            } catch (IOException e) {
                throw new VpnConfigException(e.getMessage());
            }

            return VpnConfigResponseDto.builder()
                    .status("success")
                    .configFile(configData)
                    .fileName(fileName)
                    .build();
        } else {
            throw new VpnConfigException("Не удалось получить конфигурацию клиента " + config.getName());
        }
    }

    /**
     * Блокирует клиента OpenVPN
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    public VpnConfigResponseDto blockClient(String clientName, Long serverId) {
        Server server = serverService.getServerById(serverId);
        HttpHeaders headers = createAuthHeaders(server.getApiToken());
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        String url = server.getApiUrl() + "/api/block/" + clientName;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return VpnConfigResponseDto.builder()
                    .status("success")
                    .message("Client blocked successfully")
                    .build();
        } else {
            throw new VpnConfigException("Не удалось заблокировать клиента " + clientName);
        }
    }

    /**
     * Разблокирует клиента OpenVPN
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    public VpnConfigResponseDto unblockClient(String clientName, Long serverId) {
        Server server = serverService.getServerById(serverId);
        HttpHeaders headers = createAuthHeaders(server.getApiToken());
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));


        String url = server.getApiUrl() + "/api/block/" + clientName;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return VpnConfigResponseDto.builder()
                    .status("success")
                    .message("Client unblocked successfully")
                    .build();
        } else {
            throw new VpnConfigException("Не удалось разблокировать клиента " + clientName);
        }
    }


    /**
     * Получает количество активных пользователей OpenVPN
     *
     * @return объект с результатом операции
     */
    public VpnConfigResponseDto getActiveUsers(Long serverId) {
        Server server = serverService.getServerById(serverId);
        HttpHeaders headers = createAuthHeaders(server.getApiToken());
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        String url = server.getApiUrl() + "/api/active-users";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Integer activeUsers = (Integer) response.getBody().get("active_users");
            return VpnConfigResponseDto.builder()
                    .status("success")
                    .activeUsers(activeUsers)
                    .build();
        } else {
            throw new VpnConfigException("Не удалось получить количество активных пользователей");
        }
    }

    /**
     * Создает заголовки с авторизационным токеном
     *
     * @return объект HttpHeaders с заголовками
     */
    private HttpHeaders createAuthHeaders(String vpnServerToken) {
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
     * @return платеж, связанный с созданными конфигурациями
     */
    public Optional<Payment> createMultipleConfigs(ConfigsRequestDto request) {
        log.info("Creating {} configs for country ID {} for {} months",
                request.getConfigsCount(), request.getCountryId(), request.getMonths());

        // 1. Создать платеж асинхронно
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setAmount((long) (request.getConfigsCount() * 1000)); // 100 рублей за конфигурацию
        paymentRequest.setDescription("Оплата theOpen-конфигураций");
        Optional<Payment> payment = paymentService.createPaymentLink(paymentRequest);

        if (payment.isEmpty()) {
            // Если не удалось создать платеж, выходим с ошибкой
            log.error("Failed to create payment for configs");
            return Optional.empty();
        }

        try {
            // 2. Продолжаем создание конфигураций
            List<Server> servers = serverRepository.findByCountryEntityId(request.getCountryId());
            log.info("{} servers created", servers.size());
            log.info("Servers found for country ID {}: {}", request.getCountryId(), servers);
            if (servers.isEmpty()) {
                throw new VpnConfigException("Серверы для выбранной страны (ID: " + request.getCountryId() + ") не найдены");
            }

            Server leastLoadedServer = findLeastLoadedServer(servers);
            if (leastLoadedServer == null) {
                throw new VpnConfigException("Не удалось выбрать подходящий сервер для страны с ID: " + request.getCountryId());
            }

            log.info("Selected server {} for creating configs", leastLoadedServer.getName());

            for (int i = 0; i < request.getConfigsCount(); i++) {
                String clientName = generateClientName(request.getCountryId(), i);
                VpnConfigResponseDto result = createClientConfigOnServer(clientName, leastLoadedServer, request.getMonths(), payment, request.getTelegramId());
                if (!"success".equals(result.getStatus())) {
                    throw new VpnConfigException("Не удалось создать конфигурацию " + (i + 1) + " из " + request.getConfigsCount());
                }
            }

            return payment;
        } catch (Exception e) {
            // Если произошла ошибка при создании конфигураций, отменяем платеж
            log.error("Error creating configs after payment: {}", e.getMessage());
            Payment paymentEntity = payment.get();
            boolean canceled = paymentService.cancelPayment(paymentEntity);

            if (canceled) {
                log.info("Payment {} successfully canceled due to error in config creation", paymentEntity.getPaymentId());
            } else {
                log.error("Failed to cancel payment {} after error in config creation", paymentEntity.getPaymentId());
            }

            // Независимо от результата отмены платежа, пробрасываем исходное исключение дальше
            if (e instanceof VpnConfigException) {
                throw (VpnConfigException) e;
            } else {
                throw new VpnConfigException("Ошибка при создании конфигураций: " + e.getMessage());
            }
        }
    }

    /**
     * Находит наименее загруженный сервер из списка
     *
     * @param servers список серверов
     * @return наименее загруженный сервер
     */
    private Server findLeastLoadedServer(List<Server> servers) {
        if (servers.isEmpty()) {
            throw new VpnConfigException("Список серверов пуст");
        }

        // Если сервер всего один, возвращаем его
        if (servers.size() == 1) {
            return servers.get(0);
        }

        // Получаем загруженность для каждого сервера
        Map<Server, Integer> serverLoads = new HashMap<>();
        for (Server server : servers) {
            Integer activeUsers = getActiveUsersForServer(server);
            serverLoads.put(server, activeUsers != null ? activeUsers : Integer.MAX_VALUE);
        }

        // Находим минимальную загрузку
        int minLoad = serverLoads.values().stream().min(Integer::compareTo)
                .orElseThrow(() -> new VpnConfigException("Не удалось определить нагрузку серверов"));

        // Получаем все серверы с минимальной загрузкой
        List<Server> leastLoadedServers = serverLoads.entrySet().stream()
                .filter(entry -> entry.getValue().equals(minLoad))
                .map(Map.Entry::getKey)
                .toList();

        // Если несколько серверов с одинаковой минимальной загрузкой, выбираем случайный
        if (leastLoadedServers.size() > 1) {
            int randomIndex = new Random().nextInt(leastLoadedServers.size());
            return leastLoadedServers.get(randomIndex);
        } else if (!leastLoadedServers.isEmpty()) {
            return leastLoadedServers.get(0);
        } else {
            throw new VpnConfigException("Не найден подходящий сервер с минимальной нагрузкой");
        }
    }

    /**
     * Получает количество активных пользователей для конкретного сервера
     *
     * @param server сервер
     * @return количество активных пользователей
     */
    private Integer getActiveUsersForServer(Server server) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + server.getApiToken());

            String url = server.getApiUrl() + "/api/active-users";

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            log.info("Response from server {}: {}", server.getName(), response.getBody());
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (Integer) response.getBody().get("active_users");
            }
            throw new ServerApiException("Не удалось получить количество активных пользователей для сервера " + server.getName());
        } catch (Exception e) {
            throw new ServerApiException("Ошибка при получении данных о пользователях сервера " + server.getName());
        }
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
    private VpnConfigResponseDto createClientConfigOnServer(String clientName, Server server, Integer monthAmount, Optional<Payment> payment, Long telegramId) {
        //TODO проверь если у пользователя уже была триал версия, то не давать создавать новую
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + server.getApiToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = server.getApiUrl() + "/api/create/" + clientName;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        User user = userService.findByTelegramId(telegramId);
        if (response.getStatusCode() == HttpStatus.OK) {
            Config config = new Config();
            config.setUser(user);
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
            throw new VpnConfigException("Не удалось создать конфигурацию клиента на сервере " + server.getName());
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

