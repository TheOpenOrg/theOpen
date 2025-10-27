<template>
  <div class="card flex justify-center">
    <Stepper v-model:value="activeStep" class="basis-[40rem]">
      <StepList>
        <Step v-slot="{ activateCallback, value, a11yAttrs }" asChild :value="1">
          <div class="flex flex-row flex-auto gap-2" v-bind="a11yAttrs.root">
            <button class="bg-transparent border-0 inline-flex flex-col gap-2" @click="activateCallback" v-bind="a11yAttrs.header">
                            <span
                                :class="[
                                    'rounded-full border-2 w-12 h-12 inline-flex items-center justify-center',
                                    { 'bg-primary text-primary-contrast border-primary': value <= activeStep, 'border-surface-200 dark:border-surface-700': value > activeStep }
                                ]"
                            >
                                <i class="pi pi-server" />
                            </span>
            </button>
            <Divider />
          </div>
        </Step>
        <Step v-slot="{ activateCallback, value, a11yAttrs }" asChild :value="2">
          <div class="flex flex-row flex-auto gap-2 pl-2" v-bind="a11yAttrs.root">
            <button class="bg-transparent border-0 inline-flex flex-col gap-2" @click="activateCallback" v-bind="a11yAttrs.header">
                            <span
                                :class="[
                                    'rounded-full border-2 w-12 h-12 inline-flex items-center justify-center',
                                    { 'bg-primary text-primary-contrast border-primary': value <= activeStep, 'border-surface-200 dark:border-surface-700': value > activeStep }
                                ]"
                            >
                                <i class="pi pi-star" />
                            </span>
            </button>
            <Divider />
          </div>
        </Step>
        <Step v-slot="{ activateCallback, value, a11yAttrs }" asChild :value="3">
          <div class="flex flex-row pl-2" v-bind="a11yAttrs.root">
            <button class="bg-transparent border-0 inline-flex flex-col gap-2" @click="activateCallback" v-bind="a11yAttrs.header">
                            <span
                                :class="[
                                    'rounded-full border-2 w-12 h-12 inline-flex items-center justify-center',
                                    { 'bg-primary text-primary-contrast border-primary': value <= activeStep, 'border-surface-200 dark:border-surface-700': value > activeStep }
                                ]"
                            >
                                <i class="pi pi-shopping-cart" />
                            </span>
            </button>
          </div>
        </Step>
      </StepList>
      <StepPanels>
        <StepPanel v-slot="{ activateCallback }" :value="1">
          <div class="flex flex-col gap-2 mx-auto" style="min-height: 16rem; max-width: auto">
            <div class="text-center mt-4 mb-4 text-xl font-semibold">Выберите страну для подключения</div>
            <ServerList :selectable="true" :selected-country="selectedServer" @select="val => selectedServer = val"/>
          </div>
          <div class="flex pt-6 justify-end">
            <Button label="Next" icon="pi pi-arrow-right" :disabled="!selectedServer" @click="activateCallback(2)"/>
          </div>
        </StepPanel>
        <StepPanel v-slot="{ activateCallback }" :value="2">
          <div class="flex flex-col gap-4 mx-auto" style="min-height: 16rem; max-width: 32rem">
            <div class="text-center mt-4 mb-2 text-xl font-semibold">Выберите параметры подписки</div>

            <!-- Срок подписки -->
            <div class="card p-3 border-1 border-surface-200 dark:border-surface-700">
              <div class="font-medium mb-2">Срок подписки</div>
              <div class="flex flex-wrap gap-4">
                <div
                  v-for="period in subscriptionPeriods"
                  :key="period.value"
                  @click="selectedPeriod = period.value"
                  class="cursor-pointer flex-1 p-3 border-1 rounded-md transition-all duration-200 text-center"
                  :class="[selectedPeriod === period.value ?
                    'border-primary bg-primary-50 dark:bg-primary-900/20' :
                    'border-surface-200 dark:border-surface-700']"
                >
                  <div class="font-medium">{{ period.label }}</div>
                  <div class="text-sm text-surface-600 dark:text-surface-400">{{ period.description }}</div>
                  <div class="mt-2 font-bold">{{ period.price }}</div>
                </div>
              </div>
            </div>

            <!-- Количество устройств -->
            <div class="card p-3 border-1 border-surface-200 dark:border-surface-700">
              <div class="font-medium mb-2">Количество устройств</div>
              <div class="flex flex-wrap gap-4">
                <div
                  v-for="device in deviceOptions"
                  :key="device.value"
                  @click="selectedDeviceCount = device.value"
                  class="cursor-pointer flex-1 p-3 border-1 rounded-md transition-all duration-200 text-center"
                  :class="[selectedDeviceCount === device.value ?
                    'border-primary bg-primary-50 dark:bg-primary-900/20' :
                    'border-surface-200 dark:border-surface-700']"
                >
                  <div class="font-medium">{{ device.label }}</div>
                  <div class="text-sm text-surface-600 dark:text-surface-400">{{ device.description }}</div>
                  <div class="mt-2 font-bold">{{ device.price }}</div>
                </div>
              </div>
            </div>

            <!-- Итоговая стоимость -->
            <div class="card p-3 bg-surface-50 dark:bg-surface-800 border-1 border-surface-200 dark:border-surface-700">
              <div class="flex justify-between items-center">
                <div class="text-lg font-medium">Итоговая стоимость:</div>
                <div class="text-xl font-bold">{{ calculateTotalPrice() }} ₽</div>
              </div>
            </div>
          </div>
          <div class="flex pt-6 justify-between">
            <Button label="Назад" severity="secondary" icon="pi pi-arrow-left" @click="activateCallback(1)" />
            <Button label="Далее" icon="pi pi-arrow-right" iconPos="right" @click="activateCallback(3)" />
          </div>
        </StepPanel>
        <StepPanel v-slot="{ activateCallback }" :value="3">
          <div class="flex flex-col gap-4 mx-auto" style="min-height: 16rem; max-width: 32rem">
            <div class="text-center mt-4 mb-4 text-xl font-semibold">Оформление заказа</div>

            <!-- Сводка заказа -->
            <div class="card p-4 border-1 border-surface-200 dark:border-surface-700">
              <h3 class="mt-0 mb-4 text-lg font-medium">Информация о подписке</h3>
              <div class="flex flex-col gap-2">
                <div class="flex justify-between">
                  <span>Сервер:</span>
                  <span class="font-medium">{{ selectedServer?.nameRu }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Срок подписки:</span>
                  <span class="font-medium">{{ subscriptionPeriods.find(p => p.value === selectedPeriod)?.label }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Количество устройств:</span>
                  <span class="font-medium">{{ deviceOptions.find(d => d.value === selectedDeviceCount)?.label }}</span>
                </div>
                <div class="border-t-1 border-surface-200 dark:border-surface-700 my-2"></div>
                <div class="flex justify-between">
                  <span>Итого:</span>
                  <span class="font-bold">{{ calculateTotalPrice() }} ₽</span>
                </div>
                <div v-if="promoCodeApplied" class="flex justify-between text-green-500">
                  <span>Скидка по промокоду:</span>
                  <span class="font-bold">-{{ promoDiscount }} ₽</span>
                </div>
              </div>
            </div>

            <!-- Промокод -->
            <div class="card p-4 border-1 border-surface-200 dark:border-surface-700">
              <h3 class="mt-0 mb-3 text-lg font-medium">Промокод</h3>
              <div class="flex gap-2">
                <InputGroup class="flex-1">
                  <InputGroupAddon>
                    <i class="pi pi-tag"></i>
                  </InputGroupAddon>
                  <InputText v-model="promoCode" placeholder="Введите промокод" class="w-full" :disabled="promoCodeApplied"/>
                </InputGroup>
                <Button
                  v-if="!promoCodeApplied"
                  label="Применить"
                  icon="pi pi-check"
                  @click="applyPromoCode()"
                  :disabled="!promoCode"
                />
                <Button
                  v-else
                  label="Отменить"
                  icon="pi pi-times"
                  severity="secondary"
                  @click="promoCode = ''; promoCodeApplied = false; promoDiscount = 0"
                />
              </div>
              <small v-if="promoCodeApplied" class="text-green-500">Промокод успешно применен!</small>
            </div>

            <!-- Оплата -->
            <div class="flex justify-center mt-2">
              <Button
                label="Оплатить"
                icon="pi pi-credit-card"
                size="large"
                class="w-full"
                @click="pay()"
                :loading="isCreatingConfig"
              />
            </div>
          </div>
          <div class="flex pt-6 justify-start">
            <Button label="Назад" severity="secondary" icon="pi pi-arrow-left" @click="activateCallback(2)" />
          </div>
        </StepPanel>
      </StepPanels>
    </Stepper>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Stepper from 'primevue/stepper';
import Step from 'primevue/step';
import StepList from 'primevue/steplist';
import StepPanel from 'primevue/steppanel';
import StepPanels from 'primevue/steppanels';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import ServerList from './ServerList.vue';
// import { Toast } from 'primevue/toast';
import { useToast } from 'primevue/usetoast';
import { vpnApi, serverApi } from '../services/api';

const toast = useToast();
const activeStep = ref(1);
const selectedServer = ref(null);
const selectedPeriod = ref('1m');
const selectedDeviceCount = ref(1);
const promoCode = ref('');
const promoCodeApplied = ref(false);
const promoDiscount = ref(0);
const isCreatingConfig = ref(false);
const paymentUrlRef = ref(null); // Для хранения URL платежа
const showPaymentButton = ref(false); // Флаг отображения кнопки оплаты

// Определяем тарифные планы
const subscriptionPeriods = [
  {
    value: '1m',
    label: '1 месяц',
    description: 'Базовая подписка',
    price: '400 ₽',
    priceValue: 400
  },
  {
    value: '3m',
    label: '3 месяца',
    description: 'Выгоднее на 15%',
    price: '1020 ₽',
    priceValue: 1020
  },
  {
    value: '12m',
    label: '12 месяцев',
    description: 'Выгоднее на 25%',
    price: '3600 ₽',
    priceValue: 3600
  }
];

const deviceOptions = [
  {
    value: 1,
    label: '1 устройство',
    description: 'Базовый тариф',
    price: 'Включено',
    priceValue: 0
  },
  {
    value: 3,
    label: '3 устройства',
    description: 'Для семьи',
    price: '+200 ₽',
    priceValue: 200
  },
  {
    value: 5,
    label: '5 устройств',
    description: 'Максимум',
    price: '+300 ₽',
    priceValue: 300
  }
];

// Функция для расчета общей стоимости
const calculateTotalPrice = () => {
  const periodPrice = subscriptionPeriods.find(p => p.value === selectedPeriod.value)?.priceValue || 0;
  const devicePrice = deviceOptions.find(d => d.value === selectedDeviceCount.value)?.priceValue || 0;
  let total = periodPrice + devicePrice;

  if (promoCodeApplied.value) {
    total -= promoDiscount.value;
  }

  return total;
};

// Применение промокода
const applyPromoCode = () => {
  // Здесь должна быть логика проверки промокода через API
  // Пока сделаем просто имитацию
  if (promoCode.value) {
    setTimeout(() => {
      promoCodeApplied.value = true;
      promoDiscount.value = 100; // Представим, что скидка 100 рублей
      toast.add({ severity: 'success', summary: 'Успешно!', detail: 'Промокод успешно применен', life: 3000 });
    }, 500);
  }
};

// Функция для оплаты и создания конфигурации
const pay = async () => {
  isCreatingConfig.value = true;

  try {
    // Сначала создаем объект с параметрами заказа
    const orderParams = {
      countryId: selectedServer.value?.id,
      months: selectedPeriod.value === '1m' ? 1 : (selectedPeriod.value === '3m' ? 3 : 12),
      configsCount: selectedDeviceCount.value
      // promoCode: promoCodeApplied.value ? promoCode.value : undefined
    };

    // Отправляем запрос на создание конфигурации VPN
    const response = await vpnApi.createVpnConfigs(orderParams);

    // Если в ответе есть paymentUrl
    if (response && response.paymentUrl) {
      // Сохраняем URL для возможности повторного открытия
      paymentUrlRef.value = response.paymentUrl;
      showPaymentButton.value = true;

      // Проверяем, запущены ли мы в Telegram Mini App
      if (window.Telegram?.WebApp) {
        // Для Telegram используем специальный метод открытия в браузере
        window.Telegram.WebApp.openLink(response.paymentUrl);
      } else {
        // Пробуем открыть в дефолтном браузере системы
        try {
          // Для iOS/macOS могут работать URI схемы
          if (/(iPhone|iPad|Macintosh)/i.test(navigator.userAgent)) {
            window.location.href = `x-web-search://?${encodeURIComponent(response.paymentUrl)}`;
          } else {
            // Стандартный способ открытия в новом окне
            window.open(response.paymentUrl, '_blank', 'noopener,noreferrer');
          }
        } catch (e) {
          console.error('Ошибка при открытии ссылки:', e);
          // Если не удалось открыть, просто открываем в новом окне
          window.open(response.paymentUrl, '_blank');
        }
      }
      return; // Не продолжаем дальнейшее выполнение
    }

    // Показываем сообщение об успешном создании
    toast.add({
      severity: 'success',
      summary: 'Успех!',
      detail: 'Конфигурация VPN успешно создана. Теперь вы можете скачать её в разделе "Мои конфигурации".',
      life: 5000
    });

    // Уведомляем родительский компонент о создании конфигурации
    emit('config-created', response);

  } catch (error) {
    console.error('Ошибка при создании конфигурации:', error);
    toast.add({
      severity: 'error',
      summary: 'Ошибка!',
      detail: 'Не удалось создать VPN конфигурацию. Пожалуйста, попробуйте снова.',
      life: 5000
    });
  } finally {
    isCreatingConfig.value = false;
  }
};

// Определяем emits для связи с родительским компонентом
const emit = defineEmits(['config-created']);
</script>
