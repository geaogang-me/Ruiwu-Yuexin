import axios from 'axios';
import router from '@/router';
import Swal from 'sweetalert2';
import store from '@/store';

const api = axios.create({
  baseURL: 'http://127.0.0.1:4000/api',
  timeout: 5000,
});

let hasShownExpiredBox = false;

// Helper to fully clear auth state, mirroring useAuth.clearLocal
function clearAuthState() {
  // 使用store中已定义的logout方法来清除所有认证状态
  store.dispatch('logout');
  router.push('/home');
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${token}`
    };
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  error => {
    const { response } = error;

    if (response?.status === 401 && !hasShownExpiredBox) {
      hasShownExpiredBox = true;
      Swal.fire({
        icon: 'error',
        title: '登录过期',
        text: '请求失败！登录已过期，请重新登录',
        confirmButtonText: '确定',
        allowOutsideClick: false,
        allowEscapeKey: false,
      }).then(() => {
        hasShownExpiredBox = false;
        clearAuthState();
      });
    }

    return Promise.reject(error);
  }
);

export default api;
