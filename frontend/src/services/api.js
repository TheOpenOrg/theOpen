import axios from 'axios';

// Создаем экземпляр axios с базовыми настройками
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Обработчик ответов
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // Логирование ошибок и централизованная обработка
    console.error('API Error:', error);

    // Можно добавить разную обработку в зависимости от кода ошибки
    if (error.response && error.response.status === 401) {
      // Обработка неавторизованного доступа
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
  telegramAuth: (data) => api.post('/api/users/auth/telegram', data),
};

// Методы для работы с VPN конфигурациями
export const vpnApi = {
  // Создать VPN конфигурации
  createVpnConfigs: (params) => {
    const { countryId, months, configsCount, telegramId } = params;
    return api.get(`/api/vpn/configs`, {
      params: { countryId, months, configsCount, telegramId },
      withCredentials: true,
      // Обрабатываем случай, когда приходит ответ с перенаправлением
      validateStatus: status => {
        return (status >= 200 && status < 300) || status === 302;
      },
      maxRedirects: 0
    });
  }
};

// Методы для работы с платежами
export const paymentApi = {
  // Создать платеж
  createPayment: (data) => api.post('/api/payments/create', data),

  // Проверить статус п��атежа
  checkPaymentStatus: (id) => api.get(`/api/payments/${id}/status`),
};

export default api;
