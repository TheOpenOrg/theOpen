<template>
  <div class="payment-container">
    <h2 class="text-center my-4">Оплата подписки</h2>

    <div v-if="isLoading" class="flex flex-column align-items-center">
      <ProgressSpinner />
      <p class="mt-3">Загрузка способов оплаты...</p>
    </div>

    <div v-else-if="paymentError" class="error-message">
      <i class="pi pi-exclamation-triangle mr-2"></i>
      <span>{{ paymentError }}</span>
      <Button label="Повторить" icon="pi pi-refresh" class="mt-3" @click="initPayment" />
    </div>

    <div v-else-if="paymentUrl">
      <div class="payment-methods card p-4">
        <h3>Выберите способ оплаты</h3>

        <!-- QR-код для оплаты -->
        <div class="qr-container text-center my-3">
          <QRCode :value="paymentUrl" :size="200" class="mx-auto" />
          <p class="mt-2">Отсканируйте для оплаты</p>
        </div>

        <!-- Кнопки п��пулярных банков -->
        <div class="bank-buttons my-3">
          <h4>Оплата в приложении банка</h4>
          <div class="flex flex-wrap gap-2 justify-content-center">
            <Button label="Сбербанк" icon="pi pi-credit-card" class="p-button-outlined"
                    @click="openBankApp('sberbank', paymentUrl)" />
            <Button label="Тинькофф" icon="pi pi-credit-card" class="p-button-outlined"
                    @click="openBankApp('tinkoff', paymentUrl)" />
            <Button label="Альфа-банк" icon="pi pi-credit-card" class="p-button-outlined"
                    @click="openBankApp('alfabank', paymentUrl)" />
          </div>
        </div>

        <!-- Открыть в браузере -->
        <div class="browser-payment text-center mt-3">
          <Button label="Открыть в браузере" icon="pi pi-external-link"
                  @click="openInBrowser(paymentUrl)" class="mb-2 w-full" />
          <Button label="Скопировать ссылку" icon="pi pi-copy" severity="secondary"
                  @click="copyPaymentLink(paymentUrl)" class="w-full" />
        </div>
      </div>

      <!-- Информация о заказе -->
      <div class="order-info card p-4 mt-3">
        <h3>Информация о заказе</h3>
        <div class="my-3">
          <div class="flex justify-content-between mb-2">
            <span>Сервер:</span>
            <span class="font-medium">{{ orderDetails.server }}</span>
          </div>
          <div class="flex justify-content-between mb-2">
            <span>Срок подписки:</span>
            <span class="font-medium">{{ orderDetails.period }}</span>
          </div>
          <div class="flex justify-content-between mb-2">
            <span>Количество устройств:</span>
            <span class="font-medium">{{ orderDetails.devices }}</span>
          </div>
          <div class="flex justify-content-between font-bold">
            <span>Итого:</span>
            <span>{{ orderDetails.total }} ₽</span>
          </div>
        </div>
      </div>

      <!-- Статус оплаты -->
      <div class="payment-status text-center my-4">
        <Button label="Проверить статус оплаты" icon="pi pi-check-circle"
                @click="checkPaymentStatus" />
      </div>
    </div>

    <div v-else class="text-center">
      <Button label="Инициировать оплату" icon="pi pi-credit-card" @click="initPayment" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineProps } from 'vue';
import { useToast } from 'primevue/usetoast';
import ProgressSpinner from 'primevue/progressspinner';
import Button from 'primevue/button';
import QRCode from 'qrcode.vue';
import { vpnApi } from '../services/api';

// Принимаем параметры заказа
const props = defineProps({
  orderId: {
    type: String,
    default: null
  },
  orderParams: {
    type: Object,
    default: () => ({})
  }
});

const toast = useToast();
const isLoading = ref(false);
const paymentUrl = ref(null);
const paymentError = ref(null);
const orderDetails = ref({
  server: '',
  period: '',
  devices: '',
  total: 0
});

// Телеграм объект
const tg = window.Telegram?.WebApp;

onMounted(() => {
  // Инициализируем Telegram Mini App
  if (tg) {
    tg.ready();
    tg.expand();

    // Устанавливаем заголовок
    tg.MainButton.text = "Вернуться";
    tg.MainButton.show();
    tg.MainButton.onClick(() => tg.close());

    // Если есть параметры заказа, инициируем оплату
    if (props.orderId || (props.orderParams && Object.keys(props.orderParams).length > 0)) {
      initPayment();
    }
  }
});

// Инициализация платежа
const initPayment = async () => {
  isLoading.value = true;
  paymentError.value = null;

  try {
    // Если есть orderId, получаем детали заказа
    if (props.orderId) {
      // Здесь должен быть запрос для получения деталей по ID
      const orderResponse = await vpnApi.getOrderDetails(props.orderId);
      setOrderDetails(orderResponse);
      paymentUrl.value = orderResponse.paymentUrl;
    }
    // Иначе создаем новый заказ
    else if (props.orderParams && Object.keys(props.orderParams).length > 0) {
      const response = await vpnApi.createVpnConfigs(props.orderParams);

      if (response && response.paymentUrl) {
        paymentUrl.value = response.paymentUrl;
        setOrderDetails({
          server: props.orderParams.serverName || 'Выбранный сервер',
          period: `${props.orderParams.months} мес.`,
          devices: props.orderParams.configsCount,
          total: calculateTotal(props.orderParams)
        });
      } else {
        throw new Error('Не удалось получить ссылку для оплаты');
      }
    } else {
      throw new Error('Недостаточно данных для создания платежа');
    }
  } catch (error) {
    console.error('Ошибка при инициализации платежа:', error);
    paymentError.value = 'Не удалось инициировать платеж. Пожалуйста, попробуйте снова.';

    toast.add({
      severity: 'error',
      summary: 'Ошибка!',
      detail: 'Не удалось инициировать платеж',
      life: 5000
    });
  } finally {
    isLoading.value = false;
  }
};

// Установка деталей заказа
const setOrderDetails = (orderData) => {
  orderDetails.value = {
    server: orderData.serverName || 'Выбранный сервер',
    period: orderData.period || `${orderData.months} мес.`,
    devices: orderData.configsCount || orderData.devicesCount || 1,
    total: orderData.total || calculateTotal(orderData)
  };
};

// Расчет общей стоимости (примерно)
const calculateTotal = (params) => {
  const basePrice = params.months * 400; // базовая цена в месяц
  const devicesPrice = (params.configsCount > 1) ? (params.configsCount - 1) * 100 : 0;
  return basePrice + devicesPrice;
};

// Открытие приложения банка
const openBankApp = (bank, url) => {
  // Получаем параметры из URL (для SBP-платежей)
  const urlParams = new URL(url);
  const bankApps = {
    sberbank: `sberbank://qr?code=${encodeURIComponent(url)}`,
    tinkoff: `tinkoffbank://qr?code=${encodeURIComponent(url)}`,
    alfabank: `alfabank://qr?code=${encodeURIComponent(url)}`
  };

  try {
    // Если это Telegram Mini App, используем специальный метод
    if (tg) {
      // Открытие внешней ссылки в Telegram
      tg.openLink(bankApps[bank] || url);
    } else {
      // Обычное открытие в браузере
      window.location.href = bankApps[bank] || url;
    }
  } catch (e) {
    console.error('Ошибка при открытии приложения банка:', e);
    toast.add({
      severity: 'info',
      summary: 'Внимание',
      detail: 'Не удалось открыть приложение банка. Попробуйте оплатить по QR-коду.',
      life: 5000
    });

    // Если не удалось открыть приложение, открываем в браузере
    openInBrowser(url);
  }
};

// Открытие в браузере
const openInBrowser = (url) => {
  if (tg) {
    // Для Telegram Mini Apps используем openTelegramLink для внутренних ссылок
    // и openLink для внешних с опцией try_instant_view
    try {
      // Попытка открыть как внешнюю ссылку с мгновенным просмотром
      tg.openLink(url, { try_instant_view: false });
    } catch (e) {
      console.error('Ошибка при открытии ссылки через openLink:', e);

      // Альтернативный метод: открытие через invoice (если это платежная ссылка)
      if (url.includes('invoice') || url.includes('payment')) {
        // Показываем инструкцию пользователю
        tg.showPopup({
          title: 'Открытие платежной формы',
          message: 'Нажмите "Открыть" для перехода к оплате. Если страница не открывается, скопируйте ссылку и откройте в браузере.',
          buttons: [
            { id: 'open', type: 'default', text: 'Открыть' },
            { id: 'copy', type: 'default', text: 'Скопировать ссылку' },
            { type: 'cancel' }
          ]
        }, (buttonId) => {
          if (buttonId === 'open') {
            // Принудительное открытие во внешнем браузере
            window.open(url, '_blank', 'noopener,noreferrer');
          } else if (buttonId === 'copy') {
            // Копирование ссылки в буфер обмена
            if (navigator.clipboard) {
              navigator.clipboard.writeText(url).then(() => {
                tg.showAlert('Ссылка скопирована! Откройте её в браузере для оплаты.');
              });
            } else {
              // Fallback для старых устройств
              tg.showAlert(`Скопируйте эту ссылку: ${url}`);
            }
          }
        });
      } else {
        // Для обычных ссылок просто открываем
        window.open(url, '_blank', 'noopener,noreferrer');
      }
    }
  } else {
    window.open(url, '_blank', 'noopener,noreferrer');
  }
};

// Копирование ссылки на оплату
const copyPaymentLink = (url) => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url).then(() => {
      toast.add({
        severity: 'success',
        summary: 'Ссылка скопирована!',
        detail: 'Ссылка на оплату скопирована в буфер обмена.',
        life: 5000
      });
    }).catch(err => {
      console.error('Ошибка при копировании ссылки:', err);
      toast.add({
        severity: 'error',
        summary: 'Ошибка копирования',
        detail: 'Не удалось скопировать ссылку. Попробуйте вручную.',
        life: 5000
      });
    });
  } else {
    // Fallback для старых устройств
    toast.add({
      severity: 'info',
      summary: 'Информация',
      detail: 'Долгое нажатие для копирования ссылки.',
      life: 5000
    });
  }
};

// Проверка статуса платежа
const checkPaymentStatus = async () => {
  isLoading.value = true;

  try {
    // Тут должен быть запрос для проверки статуса платежа
    const status = await vpnApi.checkPaymentStatus(props.orderId);

    if (status.paid) {
      toast.add({
        severity: 'success',
        summary: 'Успешно!',
        detail: 'Оплата прошла успешно! Ваша конфигурация готова.',
        life: 5000
      });

      // Если в Telegram, уведомляем об успешной оплате
      if (tg) {
        tg.showPopup({
          title: 'Оплата успешна',
          message: 'Ваша VPN конфигурация готова к использованию!',
          buttons: [{ type: 'ok' }]
        });
      }

      // Тут можно редиректить на страницу с созданными конфигурациями
    } else {
      toast.add({
        severity: 'info',
        summary: 'В обработке',
        detail: 'Платеж еще не обработан. Пожалуйста, попробуйте проверить позже.',
        life: 5000
      });
    }
  } catch (error) {
    console.error('Ошибка при проверке статуса платежа:', error);
    toast.add({
      severity: 'error',
      summary: 'Ошибка!',
      detail: 'Не удалось проверить статус платежа',
      life: 5000
    });
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.payment-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 1rem;
}

.qr-container {
  background-color: white;
  border-radius: 8px;
  padding: 1rem;
}

.bank-buttons {
  margin-top: 1rem;
}

.error-message {
  color: var(--red-500);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem;
}
</style>
