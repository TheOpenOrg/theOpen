import axios from 'axios';

// Создаем экземпляр axios с базовыми настройками
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Добавляем обработчик запросов для отправки токена авторизации
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Обработчик ответов
api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        // Логирование ошибок и централизованная обработка
        console.error('API Error:', error);

        // Можно добавить разную обработку в зависимости от кода ошибки
        if (error.response && error.response.status === 401) {
            // Обработка неавторизованного доступа
            localStorage.removeItem('authToken');
            window.location.reload();
        }

        return Promise.reject(error);
    }
);

// Методы для работы с серверами
export const serverApi = {
    // Получить все серверы
    getAllServers: () => api.get('/api/servers/available'),

    // Получить уникальные доступные страны с серверами
    getAvailableCountries: () => api.get('/api/servers/available-countries'),

    // Получить сервер по ID
    getServerById: (id) => api.get(`/api/servers/${id}`),
};

// Методы для работы с пользователями
export const userApi = {
    // Получить текущего пользователя
    getCurrentUser: () => api.get('/api/users/current'),

    // Авторизация через Telegram
    telegramAuth: (initData) => {
        // Отправка данных для валидации через form-urlencoded
        return api.post('/api/user/auth', new URLSearchParams({
            initData: initData
        }), {
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
    },

    // Проверка авторизации
    checkAuth: async () => {
        try {
            // Сначала пытаемся получить информацию о пользователе
            const response = await api.get('/api/user/me');
            return response;
        } catch (error) {
            // Если получили ошибку 403, пытаемся повторно авторизоваться
            if (error.response && error.response.status === 403) {
                console.log('Токен недействителен (403), выполняем повторную авторизацию');

                // Получаем initData для повторной авторизации как строку
                const initData = localStorage.getItem('telegramInitData');
                if (!initData) {
                    throw new Error('Требуется повторная авторизация, но отсутствуют данные для авторизации');
                }

                // Отправляем запрос на авторизацию с правильно закодированным параметром initData
                // Важно: передаём строку initData как есть, не пытаясь её преобразовать в объект
                const authResponse = await api({
                    method: 'post',
                    url: '/api/user/auth',
                    params: {
                        initData: initData
                    }
                });

                // Сохраняем новый токен, если он получен в ответе
                if (authResponse && authResponse.accessToken) {
                    localStorage.setItem('authToken', authResponse.accessToken);
                }

                return authResponse;
            }

            // Если другая ошибка, пробрасываем ее дальше
            throw error;
        }
    },
};

// Методы для работы с VPN конфигурациями
export const vpnApi = {
    // Создать VPN конфигурации
    createVpnConfigs: (params) => api.get('/api/vpn/configs', { params }),

    // Получить все конфигурации пользователя
    getUserConfigs: () => api.get('/api/config/my'),

    // Скачать конфигурацию по ID
    downloadConfig: (id) => api.get(`/api/vpn/configs/${id}/download`, {responseType: 'blob'}),

    // Получить детали конфигурации по ID
    getConfigDetails: (id) => api.get(`/api/vpn/configs/${id}`),
};

export default api;
