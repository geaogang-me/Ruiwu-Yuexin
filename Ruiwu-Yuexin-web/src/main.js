import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import VueImageMagnifier from 'vue-image-magnifier';
import api from './plugins/axios';    // <--- 你封装的 axios 实例
import 'sweetalert2/dist/sweetalert2.min.css';
import '@/assets/css/global.css';
import '@fortawesome/fontawesome-free/css/all.css'

const app = createApp(App);

app.use(router);
app.use(store);
app.use(VueImageMagnifier);
app.use(ElementPlus);

// 注入全局属性，组件里用 this.$api 或直接 import api
app.config.globalProperties.$api = api;

app.mount('#app');
