import { createApp } from 'vue'

const app = createApp({})

export default app.config.globalProperties.$bus = new app()
