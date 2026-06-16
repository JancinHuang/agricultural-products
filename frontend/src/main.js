import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'
import router from './router'
import App from './App.vue'
import { installElementPlus } from './plugins/elementPlus'
import './assets/styles/variables.css'
import './assets/styles/layout.css'
import './assets/styles/components.css'
import './assets/styles/admin.css'
import './assets/styles/shop.css'
import './assets/styles/main.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
installElementPlus(app)
app.mount('#app')
