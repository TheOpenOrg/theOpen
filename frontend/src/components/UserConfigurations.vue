<template>
  <div class="user-configs-container">
    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size: 2rem"></i>
      <p>Загрузка конфигураций...</p>
    </div>
    <div v-else-if="error" class="error-state">
      <i class="pi pi-exclamation-triangle" style="color: red; font-size: 2rem"></i>
      <p>{{ error }}</p>
      <Button label="Повторить" @click="loadUserConfigs" />
    </div>
    <div v-else>
      <div class="flex justify-between align-items-center mb-4">
        <h2>Ваши конфигурации</h2>
        <Button
          label="Создать новую"
          icon="pi pi-plus"
          @click="navigateToStepper"
          class="p-button-success"
        />
      </div>

      <div v-if="configurations.length === 0" class="empty-state">
        <i class="pi pi-info-circle" style="font-size: 3rem; color: #6c757d;"></i>
        <p class="mt-3">У вас пока нет конфигураций</p>
        <Button
          label="Создать конфигурацию"
          icon="pi pi-plus"
          @click="navigateToStepper"
          class="p-button-primary mt-3"
        />
      </div>

      <div v-else class="grid">
        <div v-for="config in configurations" :key="config.id" class="col-12 md:col-6 xl:col-4 p-2">
          <Card class="h-full config-card">
            <template #header>
              <div class="flex align-items-center justify-content-between">
                <div class="flex align-items-center">
                  <i class="pi pi-shield mr-2" style="font-size: 1.5rem;"></i>
                  <h3>{{ config.name }}</h3>
                </div>
                <Tag :value="config.status" :severity="getStatusSeverity(config.status)" />
              </div>
            </template>
            <template #content>
              <div class="flex flex-column">
                <div class="mb-3">
                  <span class="font-bold">Сервер:</span>
                  <span>{{ config.server?.name || 'Не указан' }}</span>
                </div>
                <div class="mb-3">
                  <span class="font-bold">Страна:</span>
                  <span>{{ config.server?.country || 'Не указана' }}</span>
                </div>
                <div>
                  <span class="font-bold">Создано:</span>
                  <span>{{ formatDate(config.createdAt) }}</span>
                </div>
              </div>
            </template>
            <template #footer>
              <div class="flex justify-content-between">
                <Button
                  label="Скачать"
                  icon="pi pi-download"
                  class="p-button-outlined"
                  @click="downloadConfig(config)"
                />
                <Button
                  label="Подробнее"
                  icon="pi pi-info-circle"
                  @click="viewConfigDetails(config)"
                />
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>

    <Dialog
      v-model:visible="detailsVisible"
      :header="selectedConfig?.name || 'Детали конфигурации'"
      :modal="true"
      :style="{width: '50vw'}"
    >
      <div v-if="selectedConfig" class="config-details">
        <div class="mb-3">
          <h4>Основная информация</h4>
          <div class="grid">
            <div class="col-6"><span class="font-bold">Сервер:</span> {{ selectedConfig.server?.name }}</div>
            <div class="col-6"><span class="font-bold">Страна:</span> {{ selectedConfig.server?.country }}</div>
            <div class="col-6"><span class="font-bold">Статус:</span> {{ selectedConfig.status }}</div>
            <div class="col-6"><span class="font-bold">Дата создания:</span> {{ formatDate(selectedConfig.createdAt) }}</div>
          </div>
        </div>

        <div class="mb-3">
          <h4>Технические детали</h4>
          <div class="grid">
            <div class="col-6"><span class="font-bold">Протокол:</span> {{ selectedConfig.protocol || 'WireGuard' }}</div>
            <div class="col-6"><span class="font-bold">IP адрес:</span> {{ selectedConfig.ipAddress || 'Автоматический' }}</div>
          </div>
        </div>

        <div class="text-center mt-4">
          <Button
            label="Скачать конфигурацию"
            icon="pi pi-download"
            @click="downloadConfig(selectedConfig)"
            class="p-button-primary"
          />
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { vpnApi } from '../services/api';
import Card from 'primevue/card';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Tag from 'primevue/tag';

const configurations = ref([]);
const loading = ref(false);
const error = ref(null);
const detailsVisible = ref(false);
const selectedConfig = ref(null);

const loadUserConfigs = async () => {
  loading.value = true;
  error.value = null;

  try {
    // Предполагаем, что в API есть метод для получения конфигураций пользователя
    const response = await vpnApi.getUserConfigs();
    configurations.value = response || [];
  } catch (err) {
    console.error('Ошибка загрузки конфигураций:', err);
    error.value = 'Не удалось загрузить ваши конфигурации. Пожалуйста, попробуйте снова.';
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return 'Не указана';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('ru-RU').format(date);
};

const getStatusSeverity = (status) => {
  switch (status?.toLowerCase()) {
    case 'active':
    case 'активна':
      return 'success';
    case 'pending':
    case 'в обработке':
      return 'warning';
    case 'expired':
    case 'истек срок':
      return 'danger';
    default:
      return 'info';
  }
};

const viewConfigDetails = (config) => {
  selectedConfig.value = config;
  detailsVisible.value = true;
};

const downloadConfig = async (config) => {
  try {
    const response = await vpnApi.downloadConfig(config.id);

    // Создаем Blob из данных конфигурации
    const blob = new Blob([response], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);

    // Создаем временную ссылку для скачивания
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = `${config.name || 'vpn-config'}.conf`;

    // Добавляем в DOM, "кликаем" по ней и удаляем
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  } catch (err) {
    console.error('Ошибка скачивания конфигурации:', err);
  }
};

const navigateToStepper = () => {
  emit('navigateToStepper');
};

const emit = defineEmits(['navigateToStepper']);

onMounted(() => {
  loadUserConfigs();
});
</script>

<style scoped>
.user-configs-container {
  padding: 1.5rem;
}

.loading-state, .error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
}

.config-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.config-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.config-details h4 {
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
}
</style>
