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
  },

  // Проверить данные авторизации Telegram
  verifyUserData: (data) => {
    // Создаем базовый объект с датой запроса
    const telegramAuthData = {
      timestamp: new Date().toISOString(),
    };

    // Добавляем только существующие поля из данных авторизации
    const fieldsToInclude = [
      'user', 'auth_date', 'hash', 'chat_instance',
      'chat_type', 'signature', 'query_id', 'start_param'
    ];

    fieldsToInclude.forEach(field => {
      if (data[field] !== undefined && data[field] !== null) {
        telegramAuthData[field] = data[field];
      }
    });

    // Добавляем информацию о клиенте для дополнительной безопасности
    if (!telegramAuthData.clientInfo) {
      telegramAuthData.clientInfo = {};
    }

    // Собираем только доступные данные об окружении клиента
    if (navigator) {
      if (navigator.userAgent) telegramAuthData.clientInfo.userAgent = navigator.userAgent;
      if (navigator.language) telegramAuthData.clientInfo.language = navigator.language;
      if (navigator.platform) telegramAuthData.clientInfo.platform = navigator.platform;
    }

    if (window && window.screen) {
      telegramAuthData.clientInfo.screenResolution = `${window.screen.width}x${window.screen.height}`;
    }

    try {
      telegramAuthData.clientInfo.timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    } catch (e) {
      console.warn('Не удалось определить временную зону:', e);
    }

    // Проверяем наличие данных для журналирования и добавляем их при наличии
    if (data.timestamp) {
      telegramAuthData.originalTimestamp = data.timestamp;
    }
    console.log('Отправляемые данные для валидации:', telegramAuthData);

    // Отправляем данные на сервер для валидации
    return api.post('/api/user/auth', telegramAuthData);
  },

  // Отправка данных для валидации через form-urlencoded
  validateTelegramAuthFormUrlencoded: (initData) => {
    return fetch('/auth/telegram', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        initData: initData
      })
    });
  }
};

// Методы для работы с платежами
export const paymentApi = {
  // Создать платеж
  createPayment: (data) => api.post('/api/payments/create', data),

  // Проверить статус платежа
  checkPaymentStatus: (id) => api.get(`/api/payments/${id}/status`),
};

export default api;
