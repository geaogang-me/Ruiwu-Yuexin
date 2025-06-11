import axios from 'axios';
import router from '@/router';
import Swal from 'sweetalert2';

const api = axios.create({
  baseURL: 'http://127.0.0.1:4000/api',
  timeout: 5000,
});

let hasShownExpiredBox = false;

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
      console.log('显示 Swal 弹窗');
      Swal.fire({
        icon: 'error',
        title: '登录过期',
        text: '请求失败！登录已过期，请重新登录',
        showCloseButton: false,
        confirmButtonText: '确定',
        customClass: {
          popup: 'swal-custom-class'
        }
      }).then(() => {
        hasShownExpiredBox = false;
        localStorage.removeItem('token');
        router.push('/home');
      });
    }

    return Promise.reject(error);
  }
);

export default api;
