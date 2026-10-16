import { createApp } from 'vue'
import '@fontsource/ibm-plex-sans/400.css'
import '@fontsource/ibm-plex-sans/500.css'
import '@fontsource/ibm-plex-sans/600.css'
import './styles/main.css'
import App from './App.vue'
import router from './router'

createApp(App).use(router).mount('#app')
