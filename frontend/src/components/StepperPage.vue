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
            <ServerList :selectable="true" :selectedId="selectedServer" @select="val => selectedServer = val"/>
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
                  <span class="font-medium">{{ selectedServer }}</span>
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
import {ref} from 'vue';
import Stepper from 'primevue/stepper';
import Button from 'primevue/button';
import ServerList from './ServerList.vue';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';

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
  // Здесь логика оплаты
  alert('Оплата!');
}

function calculateTotalPrice() {
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

function applyPromoCode() {
  // Здесь может быть логика проверки промокода через API
  if (promoCode.value) {
    // Пример проверки промокода (в реальном приложении должна быть проверка на сервере)
    if (promoCode.value.toUpperCase() === 'СКИДКА20') {
      promoDiscount.value = Math.round(calculateTotalPrice() * 0.2); // 20% скидка
      promoCodeApplied.value = true;
      return true;
    } else if (promoCode.value.toUpperCase() === 'VPN500') {
      promoDiscount.value = 500; // Фиксированная скидка 500 рублей
      promoCodeApplied.value = true;
      return true;
    } else {
      promoCodeApplied.value = false;
      promoDiscount.value = 0;
      alert('Введенный промокод недействителен');
      return false;
    }
  }
  return false;
}
</script>

<style scoped>
</style>

