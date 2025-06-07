import './assets/main.css'

import {createApp} from 'vue'
import {VueTelegramPlugin} from 'vue-tg'
import App from './App.vue'
import PrimeVue from 'primevue/config';
import Noir from './presets/Noir.js';
import "primeicons/primeicons.css";
import "./style.css";
import "./flags.css";

import ConfirmationService from 'primevue/confirmationservice'
import DialogService from 'primevue/dialogservice'
import ToastService from 'primevue/toastservice';



const app = createApp(App)

if (window.Telegram?.WebApp) {
    window.Telegram.WebApp.ready();  // Сообщаем, что WebApp готов
    window.Telegram.WebApp.expand(); // Делаем окно по всей высоте
}

app.use(VueTelegramPlugin);

app.use(PrimeVue, {
    theme: {
        preset: Noir,
        options: {
            prefix: 'p',
            darkModeSelector: '.p-dark',
            cssLayer: false,
        }
    }
});
app.use(ConfirmationService);
app.use(ToastService);
app.use(DialogService);

app.mount("#app");

