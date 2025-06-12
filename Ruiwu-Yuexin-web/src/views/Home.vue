<template>
  <div class="home-container">
    <!-- 顶部标题栏 -->
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-home"></i>
        <h1>首页</h1>
      </div>
    </div>

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
                  @click="select(opt.value)"
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

      <!-- 右侧操作按钮 -->
      <div class="right-actions">
        <div class="action-blocks">
          <div class="action-item" @click="goToFavorite">
            <img src="@/assets/icon/收藏home.svg" alt="收藏" />
          </div>
          <div class="action-item" @click="goToCart">
            <el-badge :value="cartCount" class="cart-badge">
              <img src="@/assets/icon/购物车.svg" alt="购物车" />
            </el-badge>
          </div>
          <div class="action-item" @click="goToOrder">
            <img src="@/assets/icon/订单查询.svg" alt="订单" />
          </div>
          <div class="action-item user-section">
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
        </div>
      </div>
    </div>

    <!-- 产品列表 -->
    <div class="content-area">
      <div class="loader-animation" v-if="loading">
        <div class="spinner"></div>
      </div>

      <div v-show="!loading">
        <div v-if="goodList.length" class="good-grid">
          <div
            v-for="it in goodList"
            :key="it.id"
            class="good-card"
            :style="{ '--delay': Math.random() * 0.3 + 's' }"
            @click="() => toDetail(it)"
          >
            <div class="image-container">
              <img :src="imageUrl(it.goodImage)" class="good-image" />
            </div>
            <div class="good-name">{{ it.goodName }}</div>
            <div class="price">
              <span class="currency">¥</span>
              <span class="amount">{{ it.price }}</span>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <i class="fas fa-box-open"></i>
          <h3>暂无商品信息</h3>
          <p>没有找到相关商品</p>
        </div>
      </div>
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
  const token = userInfo.value?.token;
  try {
    if (token) {
      await api.post(
        "/logout",
        {},
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
    }
  } catch (error) {
    console.warn("Logout request failed:", error.message);
  } finally {
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");
    userInfo.value = null;
    await clearLocal();
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
    setTimeout(() => {
      loading.value = false;
    }, 500);
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
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  overflow: hidden;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

/* 顶部标题样式 */
.app-header {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
  padding: 2px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.app-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.app-title h1 {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.app-title i {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 32px;
}

/* 搜索栏样式 */
.search-bar {
  width: 100%;
  height: 64px;
  padding: 2px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box {
  flex: 1;
  max-width: 800px;
  display: flex;
}

.search-inner {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 10px;
}

.custom-select {
  position: relative;
  min-width: 100px;
  z-index: 20;
}

.select-trigger {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 46px;
  cursor: pointer;
  border-right: 1px solid #eaeef5;
  background: #f8fafc;
  border-radius: 10px 0 0 10px;
  font-weight: 500;
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
  border: 1px solid #e4e7ed;
  border-radius: 0 0 10px 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-top: 4px;
  padding: 8px 0;
  list-style: none;
}

.dropdown-menu li {
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.dropdown-menu li:hover {
  background: #f5f7fa;
  color: #2766f5;
}

.dropdown-menu li.active {
  color: #2766f5;
  font-weight: 600;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 20px;
  font-size: 16px;
  background: transparent;
  height: 46px;
}

.search-input::placeholder {
  color: #c0c4cc;
}

.search-button {
  background: linear-gradient(to right, #2766f5, #3a8dff);
  color: #fff;
  border: none;
  border-radius: 0 10px 10px 0;
  padding: 0 30px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  height: 46px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 15px rgba(39, 102, 245, 0.35);
}

/* 右侧按钮样式 */
.right-actions {
  display: flex;
  align-items: center;
}

.action-blocks {
  display: flex;
  align-items: center;
  gap: 40px;
}

.action-item {
  cursor: pointer;
  transition: transform 0.2s;
  display: flex;
  align-items: center;
  position: relative;
}

.action-item img {
  width: 36px;
  height: 36px;
  transition: all 0.3s ease;
}

.action-item:hover img {
  transform: scale(1.1);
}

:deep(.el-badge__content.is-fixed) {
  transform: translateY(-35%) translateX(60%);
  box-shadow: none;
  border: none;
  background: linear-gradient(to right, #f6d365, #fda085);
  font-weight: bold;
}

.login-button {
  padding: 8px 18px;
  background: linear-gradient(to right, #2766f5, #3a8dff);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(39, 102, 245, 0.35);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #f5f7fa;
  border: 2px solid #e4e7ed;
}

.username {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 用户菜单样式 */
.user-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  width: 180px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.menu-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #eee;
  background: linear-gradient(to right, #6a11cb 0%, #2575fc 100%);
}

.menu-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-bottom: 10px;
  border: 3px solid rgba(255, 255, 255, 0.5);
}

.menu-username {
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.menu-items {
  padding: 10px 0;
}

.menu-item {
  padding: 12px 20px;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
  cursor: pointer;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #2766f5;
}

.logout {
  color: #f56c6c;
  font-weight: 500;
}

/* 产品列表区域 */
.content-area {
  position: relative;
  min-height: 500px;
}

.good-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 25px;
  margin-top: 10px;
}

.good-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
  padding: 1px;
  transition: all 0.3s ease;
  cursor: pointer;
  opacity: 0;
  transform: translateY(20px);
  animation: cardAppear 0.5s ease forwards;
  animation-delay: var(--delay);
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.good-card:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.image-container {
  height: 240px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 12px;
  background: #f8fafc;
}

.good-image {
  max-width: 100%;
  max-height: 100%;
  border-radius: 12px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.good-card:hover .good-image {
  transform: scale(1.05);
}

.good-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-top: 15px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: #ff5000;
  margin-top: 10px;
}

.currency {
  font-size: 16px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  margin-top: 30px;
}

.empty-state i {
  font-size: 70px;
  color: #d1e0fc;
  margin-bottom: 25px;
  opacity: 0.6;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #2c3e50;
}

.empty-state p {
  max-width: 500px;
  margin: 0 auto;
  color: #6c757d;
  font-size: 16px;
  line-height: 1.6;
}

/* 加载动画 */
.loader-animation {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(39, 102, 245, 0.1);
  border-radius: 50%;
  border-top: 4px solid #2766f5;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 40px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
}

.pagination button {
  padding: 10px 25px;
  border: none;
  background: linear-gradient(to right, #2766f5, #3a8dff);
  color: #fff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
  font-weight: 500;
}

.pagination button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(39, 102, 245, 0.35);
}

.pagination button:disabled {
  background: #c5d8ff;
  cursor: not-allowed;
  opacity: 0.7;
}

.pagination span {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
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

/* 响应式适配 */
@media (max-width: 992px) {
  .good-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}

@media (max-width: 768px) {
  .app-title h1 {
    font-size: 24px;
  }

  .search-bar {
    flex-direction: column;
    height: auto;
    padding: 15px;
    gap: 15px;
  }

  .good-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .image-container {
    height: 160px;
  }
}

@media (max-width: 480px) {
  .good-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    flex-direction: column;
    gap: 10px;
  }
}
</style>