<template>
  <div class="home-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-box">
        <div class="search-inner">
          <div
            class="custom-select"
            @mouseenter="showDropdown = true"
            @mouseleave="showDropdown = false"
          >
            <div class="select-trigger">
              <span>{{ searchType === "good" ? "商品" : "店铺" }}</span>
              <svg class="arrow-icon" viewBox="0 0 24 24">
                <path d="M7 10l5 5 5-5z" />
              </svg>
            </div>
            <transition name="fade">
              <ul v-show="showDropdown" class="dropdown-menu">
                <li
                  v-for="opt in options"
                  :key="opt.value"
                  @click="() => select(opt.value)"
                  :class="{ active: searchType === opt.value }"
                >
                  {{ opt.label }}
                </li>
              </ul>
            </transition>
          </div>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="请输入关键词"
            @keyup.enter="search"
          />
          <button class="search-button" @click="search">搜索</button>
        </div>
      </div>
      <div class="action-blocks">
        <div class="action-item Favorite-block" @click="goToFavorite">
          <img src="@/assets/icon/收藏home.svg" alt="收藏" />
        </div>
        <div class="action-item cart-block" @click="goToCart">
          <el-badge :value="cartCount" class="cart-badge">
            <img src="@/assets/icon/购物车.svg" alt="购物车" />
          </el-badge>
        </div>
        <div class="action-item order-block" @click="goToOrder">
          <img src="@/assets/icon/订单查询.svg" alt="订单" />
        </div>
      </div>
      <!-- 登录/用户信息 -->
      <button v-if="!userInfo" class="login-button" @click="goLogin">
        登录/注册
      </button>
      <div
        v-else
        class="user-info"
        @mouseenter="showUserMenu = true"
        @mouseleave="showUserMenu = false"
      >
        <img :src="userInfo.avatar || defaultAvatar" class="avatar" />
        <span class="username">{{ truncatedName }}</span>
        <transition name="slide-fade">
          <div v-show="showUserMenu" class="user-menu">
            <div class="menu-header">
              <img
                :src="userInfo.avatar || defaultAvatar"
                class="menu-avatar"
              />
              <div class="menu-username">{{ userInfo.username }}</div>
            </div>
            <div class="menu-items">
              <div class="menu-item" @click="goProfile">管理个人信息</div>
              <div class="menu-item logout" @click="logout">退出登录</div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- 产品列表 -->
    <div v-if="goodList.length" class="good-grid">
      <div
        v-for="it in goodList"
        :key="it.id"
        class="good-card"
        @click="() => toDetail(it)"
      >
        <img :src="imageUrl(it.goodImage)" class="good-image" />
        <div class="good-name">{{ it.goodName }}</div>
        <div class="price">
          <span class="currency">¥</span
          ><span class="amount">{{ it.price }}</span>
        </div>
      </div>
    </div>
    <div v-else class="loading">
      {{ loading ? "正在加载产品信息..." : "未找到相关商品" }}
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <button @click="prev" :disabled="page === 1">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button @click="next" :disabled="page === totalPages">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import defaultAvatar from "@/assets/images/touxiang.png";
//import axios from "axios";
import api from "@/plugins/axios";
import { useStore } from "vuex";
import { useAuth } from "@/composables/useAuth";
const { checkTokenValidity, clearLocal } = useAuth();
const router = useRouter();
const store = useStore();

// 本地状态
const cartCount = ref(0);

const goToFavorite = () => {
  if (!checkTokenValidity()) {
    // 等待 clearLocal 完成后，手动把组件的 userInfo 清空
    clearLocal().then(() => {
      userInfo.value = null;
      store.commit("setLogin", { isLogin: false, userId: null });
    });
    return;
  }
  router.push("/favorite");
};
// 跳转到购物车页
const goToCart = () => {
  if (!checkTokenValidity()) {
    // 等待 clearLocal 完成后，手动把组件的 userInfo 清空
    clearLocal().then(() => {
      userInfo.value = null;
      store.commit("setLogin", { isLogin: false, userId: null });
    });
    return;
  }
  router.push("/cart");
};
const goToOrder = () => {
  if (!checkTokenValidity()) {
    // 等待 clearLocal 完成后，手动把组件的 userInfo 清空
    clearLocal().then(() => {
      userInfo.value = null;
      store.commit("setLogin", { isLogin: false, userId: null });
    });
    return;
  }
  router.push("/order");
};

// 从后端拉取当前用户购物车数量
async function fetchCartCount() {
  const userId = store.state.userId;
  if (!userId) return;
  try {
    const res = await api.get(`/cart/count?userId=${userId}`);
    if (res.data.code === "200") {
      cartCount.value = res.data.data;
      store.commit("setCartCount", res.data.data);
    }
  } catch (e) {
    console.error("获取购物车数量失败", e);
  }
}
// --- 用户信息 ---
const userInfo = ref(null);
const showUserMenu = ref(false);
onMounted(() => {
  const u = localStorage.getItem("userInfo");
  if (u) userInfo.value = JSON.parse(u);
});
const truncatedName = computed(() =>
  userInfo.value
    ? userInfo.value.username.length > 6
      ? userInfo.value.username.slice(0, 6) + "..."
      : userInfo.value.username
    : ""
);
const goLogin = () => router.push("/login");
const goProfile = () => router.push("/Inform");
const logout = async () => {
  // 先拿本地的 token（有可能已经过期，也有可能不存在）
  const token = userInfo.value?.token;

  try {
    if (token) {
      // 如果有 token，就去向后端发一次 logout 请求
      // 让后端做：删 Redis／判断过期等逻辑
      await api.post(
        "/logout",
        {},
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      // 不管返回什么，catch 也会处理错误
    }
  } catch (error) {
    // 后端要么返回“Token 不存在或已过期”，要么直接报网络/500 错误
    // 这里我们只打印一下日志，不阻塞后续的本地清理
    console.warn(
      "Logout request failed:",
      error.response?.data?.message || error.message
    );
  } finally {
    // ——统一清理本地状态——
    // 1. 删掉 localStorage 里的 token 和 userInfo
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");

    userInfo.value = null;

    // 3. 让 useAuth 里封装的 clearLocal 同步清理一遍（它会把 router.push("/home")）
    //    也可以把清理和跳转拆成两步：先 clearLocal()，再 router.push()
    await clearLocal();

    // 4. 关掉下拉菜单
    showUserMenu.value = false;
  }
};

// --- 搜索 & 分页 ---
const searchType = ref("good");
const searchKeyword = ref("");
const showDropdown = ref(false);
const options = [
  { value: "good", label: "商品" },
  { value: "shop", label: "店铺" },
];
const select = (v) => {
  searchType.value = v;
  showDropdown.value = false;
};
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const goodList = ref([]);
const loading = ref(false);
const totalPages = computed(() => Math.ceil(total.value / pageSize.value));
const token = localStorage.getItem("token");
const userInfo1 = JSON.parse(localStorage.getItem("userInfo") || "null");
if (token && userInfo1) {
  store.commit("setLogin", { isLogin: true, userId: userInfo1.id });
  fetchCartCount();
}
const fetchgood = async () => {
  loading.value = true;
  try {
    const res = await api.get("/good", {
      params: {
        keyword: searchKeyword.value,
        type: searchType.value,
        page: page.value,
        size: pageSize.value,
      },
    });
    const d = res.data.data;
    goodList.value = d.records;
    total.value = d.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const search = () => {
  page.value = 1;
  fetchgood();
};
const prev = () => {
  if (page.value > 1) {
    page.value--;
    fetchgood();
  }
};
const next = () => {
  if (page.value < totalPages.value) {
    page.value++;
    fetchgood();
  }
};
const toDetail = (it) => {
  router.push({
    path: "/detail",
    query: { goodId: it.id, goodName: it.goodName },
  });
};

const imageUrl = (b64) =>
  b64 ? `data:image/png;base64,${b64}` : defaultAvatar;

onMounted(fetchgood);
</script>

<style scoped>
/* 登录按钮绝对定位在右上角 */
.login-button {
  position: absolute;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  padding: 8px 16px;
  background-color: #ffffff;
  color: #ff5000;
  border: 2px solid #ff5000;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.login-button:hover {
  background-color: #ff5000;
  color: #fff;
}
.user-info {
  position: absolute;
  right: -210px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #ccc;
}

.username {
  font-size: 14px;
  color: #333;
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 搜索栏样式 */
.search-bar {
  width: 100%;
  height: 44px;
  padding: 20px 0;
  display: flex;
  justify-content: center;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.cart-block {
  position: absolute;
  right: 235px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
}
.Favorite-block {
  position: absolute;
  right: 300px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
}
.Order-block {
  position: absolute;
  right: 170px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
}
:deep(.el-badge__content.is-fixed) {
  transform: translateY(-35%) translateX(60%);
  box-shadow: none;
  border: none;
}
.search-container {
  width: 60%;
  max-width: 800px;
  display: flex;
  gap: 10px;
}
.search-box {
  width: 60%;
  max-width: 800px;
  border: 2px solid #ff5000;
  border-radius: 12px;
  background-color: #fff;
  padding: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.search-inner {
  display: flex;
  flex: 1;
  align-items: center;
}

.search-select {
  border: none;
  outline: none;
  background: transparent;
  padding: 0 10px;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  border-right: 1px solid #ddd;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 15px;
  font-size: 16px;
  background: transparent;
}
.search-input:focus {
  border-color: #409eff;
}
.search-button {
  background-color: #ff5000;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 22px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.3s ease;
}
.search-button:hover {
  background-color: #ff7633;
}

/* 页面布局 */
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
.good-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(180px, 1fr));
  gap: 10px;
  margin-top: 10px;
}
.good-card {
  width: 225px;
  height: 390px;
  padding: 4px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  position: relative;
  border: 1px solid #ddd;
  user-select: none;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s ease, transform 0.2s ease, box-shadow 0.2s ease;
}
.good-card:hover {
  border-color: #fe8157;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 分页区域样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}
.pagination button {
  padding: 6px 12px;
  border: none;
  background-color: #409eff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s ease;
}
.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-container {
    width: 90%;
    flex-direction: column;
  }
  .search-select,
  .search-input,
  .search-button {
    width: 100%;
    padding: 10px 15px;
  }
}
@media (max-width: 1200px) {
  .good-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 992px) {
  .good-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 768px) {
  .good-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 480px) {
  .good-grid {
    grid-template-columns: 1fr;
  }
}
.good-image {
  width: 100%;
  height: 80%;
  object-fit: cover;
  border-radius: 10px;
  display: block;
}
.good-name {
  font-size: 1rem;
  font-weight: bold;
  text-align: left;
  padding: 5px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  height: 2.6rem; /* 根据 font-size 和 line-height 计算 */
  box-sizing: border-box;
}
.price {
  font-size: 1.4rem;
  font-weight: bold;
  color: #ff5000;
  text-align: left;
  padding: 5px;
}
.price .currency {
  font-size: 0.9rem; /* 比整体字体略小 */
  vertical-align: baseline; /* 可选，使 ¥ 符号靠上对齐金额 */
  margin-right: 2px; /* 给 ¥ 和金额留点空间 */
}

.loading {
  font-size: 1.2rem;
  color: #999;
  text-align: center;
  width: 100%;
}
.custom-select {
  position: relative;
  min-width: 80px;
  z-index: 20;
}

.select-trigger {
  display: flex;
  align-items: center;
  padding: 0 10px;
  height: 36px;
  cursor: pointer;
  border-right: 1px solid #ddd;
}
.action-blocks {
  position: absolute;
  right: 5px; /* 距离右侧 30px */
  top: 50%;
}

.action-item img {
  width: 32px;
  height: 32px;
  cursor: pointer;
  transition: transform 0.2s;
}
.action-item img:hover {
  transform: scale(1.1);
}
.arrow-icon {
  width: 16px;
  height: 16px;
  margin-left: 8px;
  fill: #666;
  transition: transform 0.2s;
}

.custom-select:hover .arrow-icon {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: white;
  border: 1px solid #ff5000;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-top: 4px;
  padding: 8px 0;
  list-style: none;
}

.dropdown-menu li {
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.dropdown-menu li:hover {
  background: #fff7f5;
  color: #ff5000;
}

.dropdown-menu li.active {
  color: #ff5000;
  font-weight: 500;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .custom-select {
    min-width: 60px;
  }

  .select-trigger span {
    font-size: 14px;
  }

  .arrow-icon {
    width: 14px;
    height: 14px;
  }
}
/* 新增样式 */
.user-info {
  position: relative;
  cursor: pointer;
}
.cart-block img {
  width: 40px; /* 新宽度 */
  height: 40px; /* 新高度 */
}

.user-menu {
  position: absolute;
  right: -70;
  top: calc(100% + 10px);
  width: 240px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.menu-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.menu-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  margin-bottom: 12px;
}

.menu-username {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.menu-items {
  padding: 8px 0;
}

.menu-item {
  padding: 12px 24px;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
  cursor: pointer;
}
.menu-item:hover {
  background: #f5f5f5;
  color: #ff5000;
}

.logout {
  color: #ff5000;
}

/* 过渡动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}
</style>
