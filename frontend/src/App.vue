<script setup>
import StepperPage from './components/StepperPage.vue'
import TelegramAuth from './components/TelegramAuth.vue'
import UserConfigurations from './components/UserConfigurations.vue'
import { ref, onMounted } from 'vue'
import { userApi } from './services/api'

const isAuthenticated = ref(false)
const loading = ref(true)
const activeView = ref('configs') // configs или stepper

// Проверяем статус авторизации при загрузке
const checkAuthStatus = async () => {
  try {
    // Проверяем, если есть токен в localStorage
    const token = localStorage.getItem('authToken')

    if (token) {
      // Проверяем валидность токена через API
      await userApi.checkAuth()
      isAuthenticated.value = true
    }
  } catch (error) {
    console.error('Error checking auth status:', error)
    localStorage.removeItem('authToken') // Удаляем невалидный токен
    isAuthenticated.value = false
  } finally {
    loading.value = false
  }
}

// Обработчик успешной авторизации
const handleAuthSuccess = () => {
  isAuthenticated.value = true
}

// Переключение на страницу StepperPage
const navigateToStepper = () => {
  activeView.value = 'stepper'
}

// Переключение на страницу конфигураций
const navigateToConfigs = () => {
  activeView.value = 'configs'
}

onMounted(() => {
  checkAuthStatus()
})
</script>

<template>
  <main>
    <div v-if="loading" class="loading-container">
      <i class="pi pi-spin pi-spinner" style="font-size: 2.5rem"></i>
      <p>Загрузка приложения...</p>
    </div>

    <template v-else>
      <!-- Компонент авторизации -->
      <TelegramAuth v-if="!isAuthenticated" @auth-success="handleAuthSuccess" />

      <!-- Авторизованный контент -->
      <div v-else class="app-content">
        <div class="navbar">
          <div class="logo">TheOpen VPN</div>
          <div class="nav-btns">
            <button
              class="nav-btn"
              :class="{ active: activeView === 'configs' }"
              @click="navigateToConfigs"
            >
              Мои конфигурации
            </button>
            <button
              class="nav-btn"
              :class="{ active: activeView === 'stepper' }"
              @click="navigateToStepper"
            >
              Новая конфигурация
            </button>
          </div>
        </div>

        <!-- Отображение компонента в зависимости от выбранного вида -->
        <UserConfigurations
          v-if="activeView === 'configs'"
          @navigate-to-stepper="navigateToStepper"
        />
        <StepperPage
          v-else
          @config-created="navigateToConfigs"
        />
      </div>
    </template>
  </main>
</template>

<style scoped>
main {
  min-height: 100vh;
  background: #f7fafd;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}

.app-content {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.navbar {
  background: #0088cc;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-btns {
  display: flex;
  gap: 1rem;
}

.nav-btn {
  background: transparent;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  font-size: 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-btn.active {
  background: rgba(255, 255, 255, 0.2);
  font-weight: bold;
}
</style>
