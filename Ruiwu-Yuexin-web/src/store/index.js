import { createStore } from 'vuex';

export default createStore({
  state: {
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    token: localStorage.getItem('token') || null,
    cartCount: 0,
  },
  mutations: {
    // 更新用户信息
    updateUserInfo(state, payload) {
      state.userInfo = payload;
      // 同步到 localStorage
      state.userId = payload.id; 
      localStorage.setItem('userInfo', JSON.stringify(payload));
    },
    updateToken(state, payload) {
      state.token = payload;
      // 同步到 localStorage
      localStorage.setItem('token', payload);
    },
    // 更新登录状态
    setLogin(state, payload) {
      state.isLogin = payload.isLogin;
      // 确保 userId 更新
      if (payload.userId) {
        state.userId = payload.userId;
      }
    },
  
    // 更新 Token
    
    // 用户登出
    logout(state) {
      state.userInfo = null;
      state.isLogin = false;
      state.token = null;
      // 清除 localStorage
      localStorage.removeItem('userInfo');
      localStorage.removeItem('token');
    },
    setCartCount(state, count) {
      state.cartCount = count;
    },
  },
  getters: {
    
    // 获取用户 ID
    userId: state => state.userInfo?.id || null,
    // 获取用户信息
    userInfo: (state) => state.userInfo,
    // 获取登录状态
    isLogin: state => !!state.token,
    // 获取 Token
    token: (state) => state.token,
  },
  actions: {
    // 用户登录
    async login({ commit }, payload) {
      try {
        const response = await axios.post('http://localhost:4000/api/login', {
          username: payload.username,
          password: payload.password,
        });

        if (response.data.code === 200) {
          const { token, userInfo } = response.data.data;
          // 更新 Token
          commit('updateToken', token);
          // 更新用户信息
          commit('updateUserInfo', userInfo);
          // 更新登录状态
          commit('setLogin', { isLogin: true, userId: userInfo.id });
          return true;
        }
        return false;
      } catch (error) {
        console.error('登录失败：', error);
        return false;
      }
    },
    // 用户登出
    logout({ commit }) {
      commit('logout');
    },
  },
  modules: {},
});