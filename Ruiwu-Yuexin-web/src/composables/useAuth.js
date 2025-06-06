// src/composables/useAuth.js
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import { jwtDecode } from 'jwt-decode';
import store from "@/store";

export function useAuth() {
  const router = useRouter();
  const tokenRef = ref(localStorage.getItem('token'));

  // 返回一个 Promise，让调用方在 .then 里自己清空组件 state
  async function clearLocal() {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    tokenRef.value = null;
    store.commit('setLogin', { isLogin: false, userId: null });
    store.commit('setUserInfo', null);
    await router.push('/home');
    // 这里可以 resolve 一个标识，让组件知道清理完毕
    return Promise.resolve();
  }

  function checkTokenValidity() {
    if (!tokenRef.value) {
      Swal.fire({
        icon: 'warning',
        title: '请先登录',
        confirmButtonText: '确定',
        allowOutsideClick: false,
        allowEscapeKey: false,
      }).then(() => {
        // 等路由跳转完成后，调用 clearLocal 并把 Promise 回传给调用方
        clearLocal().then(() => {
          // nothing, 组件自行处理 userInfo
        });
      });
      return false;
    }

    try {
      const decoded = jwtDecode(tokenRef.value);
      const expMs = decoded.exp * 1000;
      if (expMs < Date.now()) {
        Swal.fire({
          icon: 'error',
          title: '登录已过期',
          text: '请求失败！登录已过期，请重新登录',
          confirmButtonText: '确定',
          allowOutsideClick: false,
          allowEscapeKey: false,
        }).then(() => {
          clearLocal().then(() => {
            // 组件自行处理 userInfo
          });
        });
        return false;
      }
      return true;
    } catch (e) {
      Swal.fire({
        icon: 'error',
        title: 'Token 无效',
        text: '请重新登录',
        confirmButtonText: '确定',
        allowOutsideClick: false,
        allowEscapeKey: false,
      }).then(() => {
        clearLocal().then(() => {
          // 组件自行处理 userInfo
        });
      });
      return false;
    }
  }

  function refreshToken() {
    tokenRef.value = localStorage.getItem('token');
  }

  return {
    checkTokenValidity,
    clearLocal,
    refreshToken,
  };
}
