import './assets/main.css'

import { createApp } from 'vue'
import { VueTelegramPlugin } from 'vue-tg'
import App from './App.vue'

const app = createApp(App)

if (window.Telegram?.WebApp) {
    window.Telegram.WebApp.ready();  // Сообщаем, что WebApp готов
    window.Telegram.WebApp.expand(); // Делаем окно по всей высоте
}

app.use(VueTelegramPlugin)
    .mount('#app')