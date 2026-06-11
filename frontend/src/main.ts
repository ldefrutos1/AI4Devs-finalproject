import '@fontsource/inter/400.css'
import '@fontsource/inter/600.css'
import '@fontsource/inter/700.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/style.css'
import App from '@/App.vue'
import router from '@/router'
import { i18n, initializeI18n } from '@/i18n'

void initializeI18n()

createApp(App).use(createPinia()).use(router).use(i18n).mount('#app')
