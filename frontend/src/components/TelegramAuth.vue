<template>
  <div class="telegram-auth-container">
    <div class="auth-card">
      <h1>Авторизация</h1>
      <div v-if="loading" class="loading">
        <i class="pi pi-spin pi-spinner" style="font-size: 2rem"></i>
        <p>Выполняется авторизация...</p>
      </div>
      <div v-else-if="error" class="error-message">
        <i class="pi pi-exclamation-triangle" style="color: red; font-size: 2rem;"></i>
        <p>{{ error }}</p>
        <Button label="Повторить" @click="initAuth" class="mt-3" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { userApi } from '../services/api';
import Button from 'primevue/button';

const loading = ref(true);
const error = ref(null);
const authenticated = ref(false);

const initAuth = async () => {
  loading.value = true;
  error.value = null;

  try {
    // Проверяем доступен ли WebApp Telegram
    if (!window.Telegram?.WebApp) {
      throw new Error('Телеграм WebApp не обнаружен. Пожалуйста, откройте приложение в Telegram.');
    }

    const telegramWebApp = window.Telegram.WebApp;

    // Собираем данные для авторизации
    const authData = {
      id: telegramWebApp.initDataUnsafe?.user?.id,
      first_name: telegramWebApp.initDataUnsafe?.user?.first_name,
      last_name: telegramWebApp.initDataUnsafe?.user?.last_name,
      username: telegramWebApp.initDataUnsafe?.user?.username,
      initData: telegramWebApp.initData,
      hash: telegramWebApp.initDataUnsafe?.hash
    };

    // Отправляем запрос на авторизацию
    const response = await userApi.telegramAuth(authData.initData);

    // Сохраняем токен если он есть в ответе
    if (response.accessToken) {
      localStorage.setItem('authToken', response.accessToken);
    }

    authenticated.value = true;

    // Сообщаем родительскому компоненту что авторизация успешна
    emit('authSuccess');

  } catch (err) {
    console.error('Ошибка авторизации:', err);
    error.value = 'Произошла ошибка при авторизации. Пожалуйста, попробуйте снова.';
  } finally {
    loading.value = false;
  }
};

const emit = defineEmits(['authSuccess']);

onMounted(() => {
  initAuth();
});
</script>

<style scoped>
.telegram-auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f7fafd;
}

.auth-card {
  background-color: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.loading, .error-message {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

h1 {
  color: #0088cc;
  margin-bottom: 1.5rem;
}
</style>
