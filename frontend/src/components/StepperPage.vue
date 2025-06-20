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
import {ref, onMounted} from 'vue';
import Stepper from 'primevue/stepper';
import Button from 'primevue/button';
import ServerList from './ServerList.vue';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import axios from 'axios';
import { vpnApi } from '../services/api';

const activeStep = ref(1);
const name = ref();
const email = ref();
const password = ref();
const option1 = ref(false);
const option2 = ref(false);
const option3 = ref(false);
const option4 = ref(false);
const option5 = ref(false);
const option6 = ref(false);
const option7 = ref(false);
const option8 = ref(false);
const option9 = ref(false);
const option10 = ref(false);

const telegramId = ref(null);
const telegramInitData = ref(null);

// Инициализация и получение данных из Telegram WebApp
onMounted(async () => {
  await fetchTelegramData();

  // Получаем и обрабатываем параметр user из URL
  parseUserParamFromUrl();
});

function fetchTelegramData() {
  try {
    // Проверяем наличие Telegram WebApp API
    if (window.Telegram && window.Telegram.WebApp) {
      // Получаем и сохраняем полный объект initData
      telegramInitData.value = window.Telegram.WebApp.initData;

      // Выводим данные через alert для проверки
      alert(`Telegram WebApp initData: ${telegramInitData.value}`);
      console.log('Telegram WebApp initData:', telegramInitData.value);

      // Если есть данные пользователя, также сохраняем их
      if (window.Telegram.WebApp.initDataUnsafe && window.Telegram.WebApp.initDataUnsafe.user) {
        const tg_user = window.Telegram.WebApp.initDataUnsafe.user;
        telegramId.value = tg_user.id;

        // Также выводим информацию о пользователе
        alert(`Telegram ID: ${tg_user.id}\nИмя: ${tg_user.first_name || 'Не указано'}`);

        // Сохраняем информацию в localStorage для дальнейшего использования
        localStorage.setItem('userInfo', JSON.stringify({
          tgId: tg_user.id,
          name: tg_user.first_name,
          initData: telegramInitData.value
        }));
      } else {
        console.warn('Данные пользователя Telegram не доступны');
      }
    } else {
      console.warn('Telegram WebApp API не доступен. Используем тестовый режим.');

      // Если не найден Telegram API, используем тестовый ID
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        const parsedInfo = JSON.parse(userInfo);
        telegramId.value = parsedInfo.tgId;
        telegramInitData.value = parsedInfo.initData;

        alert(`Используются сохраненные данные из localStorage:\nTelegram ID: ${telegramId.value}\nInitData: ${telegramInitData.value}`);
      } else {
        // Тестовый ID для разработки
        telegramId.value = 12345678;
        alert(`Используется тестовый Telegram ID: ${telegramId.value}\nInitData отсутствует`);
      }
    }

    // Можно также отправить данные на сервер для верификации
    if (telegramInitData.value) {
      vpnApi.validateTelegramAuthFormUrlencoded(telegramInitData.value)
          .then(response => {
            console.log('Данные успешно отправлены на сервер:', response);
            // Здесь можно обработать ответ от сервера
          })
          .catch(error => {
            console.error('Ошибка при отправке данных на сервер:', error);
            // Здесь можно обработать ошибку
          });
    }

  } catch (error) {
    console.error('Ошибка при получении данных из Telegram WebApp:', error);
    alert(`Ошибка при получении данных из Telegram WebApp: ${error.message}`);
  }
}

// Функция для получения, декодирования и отправки параметра user из URL
function parseUserParamFromUrl() {
  try {
    // Получаем текущий URL
    const url = new URL(window.location.href);
    const searchParams = url.searchParams;

    // Инициализируем объект для сбора всех параметров
    const telegramAuthData = {};

    // Получаем параметр user из URL
    const userParam = searchParams.get('user');

    if (userParam) {
      // Декодируем параметр
      const decodedUser = decodeURIComponent(userParam);
      console.log('Декодированный параметр user:', decodedUser);

      // Преобразуем строку в объект
      const userObject = JSON.parse(decodedUser);
      console.log('Объект пользователя после JSON.parse:', userObject);

      // Добавляем объект пользователя в данные для валидации
      telegramAuthData.user = userObject;

      // Если объект содержит идентификатор пользователя, сохраняем его
      if (userObject.id) {
        telegramId.value = userObject.id;
      }
    } else {
      console.log('Параметр user не найден в URL');
      return; // Выходим, если нет главного параметра
    }

    // Получаем остальные параметры для валидации
    const requiredParams = [
      'auth_date',
      'hash',
      'signature',
      'chat_instance',
      'chat_type'
    ];

    // Получаем все остальные параметры из URL
    requiredParams.forEach(param => {
      const value = searchParams.get(param);
      if (value) {
        telegramAuthData[param] = value;
      }
    });

    // Проверяем наличие минимально необходимых параметров (user, auth_date и hash)
    if (!telegramAuthData.auth_date || !telegramAuthData.hash) {
      console.warn('Не найдены обязательные параметры auth_date или hash в URL');
      return;
    }

    // Проверяем актуальность данных (не более 24 часов)
    const authTimestamp = parseInt(telegramAuthData.auth_date, 10);
    const currentTimestamp = Math.floor(Date.now() / 1000);

    if (currentTimestamp - authTimestamp > 86400) {
      console.warn('Данные авторизации Telegram устарели (более 24 часов)');
      alert('Данные авторизации устарели, пожалуйста, авторизуйтесь заново');
      return;
    }

    console.log('Собранные данные авторизации Telegram:', telegramAuthData);

    // Добавляем текущую временную метку
    telegramAuthData.timestamp = new Date().toISOString();

    // Отправляем данные на сервер для проверки
    sendUserDataToServer(telegramAuthData);

  } catch (error) {
    console.error('Ошибка при обработке параметров Telegram из URL:', error);
    alert(`Ошибка при обработке данных авторизации: ${error.message}`);
  }
}

// Функция для отправки данных пользователя на сервер
function sendUserDataToServer(userData) {
  console.log('Отправка данных пользователя на сервер...');

  // Создаем объект с данными для отправки
  // Вы можете выбрать нужные поля из userData или отправить весь объект
  const dataToSend = {
    user: userData,
    timestamp: new Date().toISOString()
  };

  // Отправляем данные на сервер
  vpnApi.validateTelegramAuthFormUrlencoded(dataToSend)
    .then(response => {
      console.log('Данные успешно отправлены на сервер:', response);
      // Здесь можно обработать ответ от сервера
    })
    .catch(error => {
      console.error('Ошибка при отправке данных на сервер:', error);
      // Здесь можно обработать ошибку
    });
}

// Функция для отправки initData на сервер (для верификации)
function sendInitDataToServer(initData) {
  console.log('Отправка initData на сервер для верификации...');
  // Здесь можно добавить код для отправки initData на сервер
  // например, через API:
  // vpnApi.verifyTelegramData({ initData })
  //   .then(response => console.log('Верификация успешна:', response))
  //   .catch(error => console.error('Ошибка верификации:', error));
}

const selectedServer = ref(null);
const subscriptionPeriods = ref([
  { value: '1m', label: '1 месяц', description: 'Краткосрочная подписка', price: '500 ₽' },
  { value: '6m', label: '6 месяцев', description: 'Среднесрочная подписка', price: '2700 ₽' },
  { value: '12m', label: '12 месяцев', description: 'Долгосрочная подписка', price: '5000 ₽' }
]);
const deviceOptions = ref([
  { value: 1, label: '1 устройство', description: 'Для одного пользователя', price: '0 ₽' },
  { value: 3, label: '3 устройства', description: 'Для семьи', price: '+1000 ₽' },
  { value: 5, label: '5 устройств', description: 'Для команды', price: '+2000 ₽' }
]);
const selectedPeriod = ref('1m');
const selectedDeviceCount = ref(1);
const promoCode = ref('');
const promoCodeApplied = ref(false);
const promoDiscount = ref(0);

const calculateTotalPrice = () => {
  let periodPrice = 0;
  let devicePrice = 0;

  const periodItem = subscriptionPeriods.value.find(p => p.value === selectedPeriod.value);
  if (periodItem) {
    periodPrice = parseInt(periodItem.price.replace(/[^\d]/g, ''));
  }

  const deviceItem = deviceOptions.value.find(d => d.value === selectedDeviceCount.value);
  if (deviceItem) {
    devicePrice = parseInt(deviceItem.price.replace(/[^\d]/g, ''));
  }

  const totalBeforeDiscount = periodPrice + devicePrice;
  return promoCodeApplied.value ? totalBeforeDiscount - promoDiscount.value : totalBeforeDiscount;
}

function onServerSelect(server) {
  selectedServer.value = server;
}

function nextStep() {
  activeStep.value++;
}

function prevStep() {
  activeStep.value--;
}

function pay() {
  // Проверяем наличие telegramId
  if (!telegramId.value) {
    alert('Ошибка авторизации: Telegram ID не найден');
    return;
  }

  // Проверяем наличие выбранного сервера с подробной диагностикой
  if (!selectedServer.value) {
    alert('Не ��ыбран сервер');
    return;
  }

  // Выводим в консоль содержимое объекта сервера для отладки
  console.log('Выбранный сервер:', selectedServer.value);

  // Получаем countryId или id из выбранного сервера
  // Проверяем оба свойства, так как структура объекта сервера может отличаться
  const countryId = selectedServer.value.countryId || selectedServer.value.id;

  if (!countryId) {
    // Выводим полную информацию о сервере для диагностики
    console.error('Ошибка: Не найден countryId в объекте сервера:', selectedServer.value);
    alert(`Не найден идентификатор сервера в выбранном сервере: ${selectedServer.value.nameRu || 'Неизвестный'}`);
    return;
  }

  // Определяем количество месяцев на основе выбранного периода
  let months = 1;
  switch (selectedPeriod.value) {
    case '1m': months = 1; break;
    case '6m': months = 6; break;
    case '12m': months = 12; break;
  }

  // Используем количество устройств как configsCount
  const configsCount = selectedDeviceCount.value;

  // Выводим информацию об отправляемых параметрах для проверки
  console.log('Параметры запроса:', {
    countryId,
    months,
    configsCount,
    telegramId: telegramId.value
  });

  // Индикатор загрузки
  const isLoading = ref(true);

  // Отправляем запрос на бэкенд
  vpnApi.createVpnConfigs({ countryId, months, configsCount, telegramId: telegramId.value })
      .then(response => {
        // Теперь сервер возвращает JSON с URL
        if (response.status === "success" && response.paymentUrl) {
          // Переходим на страницу оплаты в этой же вкладке
          window.location.href = response.paymentUrl;
        } else {
          alert(`Ошибка: ${response.message || 'Не удалось получить URL для оплаты'}`);
        }
      })
      .catch(error => {
        console.error('Ошибка при создании VPN конфигурации:', error);
        alert(`Ошибка при создании VPN конфигурации: ${error.response?.data?.message || error.message}`);
      });
}
</script>

