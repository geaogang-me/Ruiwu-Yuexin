<template>
  <div class="detail-container">
    <!-- 顶部标题栏 -->
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-tag"></i>
        <h1>商品详情</h1>
      </div>
      <el-button type="primary" plain class="back-button" @click="backToList">
        <i class="fas fa-arrow-left"></i> 返回
      </el-button>
    </div>

    <!-- 商品主要内容 -->
    <div class="product-detail">
      <!-- 左侧图片展示区 -->
      <div class="image-panel">
        <!-- 缩略图列表 -->
        <div class="thumbnails">
          <div
            v-for="(url, idx) in good.images"
            :key="idx"
            class="thumbnail-container"
            :class="{ active: idx === current }"
            @click="selectImage(idx)"
          >
            <img :src="url" alt="商品缩略图" class="thumbnail" />
          </div>
        </div>

        <!-- 主图展示区 -->
        <div
          class="main-image-container"
          @mouseenter="enterHandler"
          @mousemove="moveHandler"
          @mouseleave="outHandler"
        >
          <img :src="good.images[current]" alt="商品主图" class="main-image" />
          <div class="magnifier" v-show="topShow" :style="topStyle"></div>
        </div>

        <!-- 放大图区域 -->
        <div class="magnified-view" v-show="rShow">
          <img
            :src="good.images[current]"
            class="magnified-image"
            :style="rStyle"
          />
        </div>
      </div>

      <!-- 右侧商品信息区 -->
      <div class="product-info">
        <!-- 商品标题 -->
        <div class="name-price">
          <h1 class="product-name">{{ good.goodName }}</h1>

          <!-- 价格区域 -->
          <div class="price-section">
            <div class="price-label">价格</div>
            <div class="price-display">
              <span class="currency">¥</span>
              <span class="amount">{{ good.price }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" class="buy-button" @click="handleBuyNow">
            立即购买
          </el-button>

          <el-button type="warning" class="cart-button" @click="addToCart">
            加入购物车
          </el-button>

          <el-button
            plain
            class="favorite-button"
            :class="{ 'is-favorite': isFavorite }"
            @click="toggleFavorite"
          >
            <i class="fas" :class="isFavorite ? 'fa-heart' : 'fa-heart-o'"></i>
            {{ isFavorite ? "已收藏" : "收藏" }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 购买对话框 -->
    <OrderDialog
      v-model:visible="orderVisible"
      :good="{
        id: parseInt($route.query.goodId),
        name: good.goodName,
        price: good.price,
        image: good.images[0],
      }"
      @order-submitted="onOrderSubmitted"
    />

    <!-- 顶部购物车图标 -->
    <div class="cart-block" @click="goToCart">
      <el-badge :value="cartCount" class="cart-badge">
        <i class="fas fa-shopping-cart cart-icon"></i>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import api from "@/plugins/axios";
import Swal from "sweetalert2";
import OrderDialog from "@/components/OrderDialog.vue";
import { useAuth } from "@/composables/useAuth";
import { computed } from "vue";
const store = useStore();
const userInfo = computed(() => store.state.userInfo);
const { checkTokenValidity, refreshToken } = useAuth();
const router = useRouter();
const route = useRoute();
const good = reactive({
  goodName: "",
  images: [],
  price: 0,
});
const current = ref(0);
const topShow = ref(false);
const rShow = ref(false);
const topStyle = ref({});
const rStyle = ref({});
const cartCount = ref(0);
const isFavorite = ref(false);
const orderVisible = ref(false);

function backToList() {
  router.back();
}

function handleBuyNow() {
  if (!checkTokenValidity()) return;
  orderVisible.value = true;
}

function onOrderSubmitted() {
  router.push({ path: "/orders" });
}

function goToCart() {
  if (!checkTokenValidity()) return;
  router.push({ path: "/cart" });
}

function selectImage(idx) {
  current.value = idx;
}

function enterHandler() {
  topShow.value = true;
  rShow.value = true;
}

function moveHandler(e) {
  const rect = e.currentTarget.getBoundingClientRect();
  const magnifierSize = 150; // 放大镜尺寸
  const magnifierHalf = magnifierSize / 2;

  // 动态获取主图容器的实际宽度和高度
  const containerWidth = rect.width;
  const containerHeight = rect.height;

  // 计算鼠标在容器中的相对位置
  let x = e.clientX - rect.left;
  let y = e.clientY - rect.top;

  // 计算放大镜左上角的坐标，确保在容器边界内
  let topX = Math.max(
    0,
    Math.min(x - magnifierHalf, containerWidth - magnifierSize)
  );
  let topY = Math.max(
    0,
    Math.min(y - magnifierHalf, containerHeight - magnifierSize)
  );

  // 计算放大镜位置
  topStyle.value = {
    transform: `translate(${topX}px, ${topY}px)`,
    width: `${magnifierSize}px`,
    height: `${magnifierSize}px`,
  };

  // 计算放大图像的位置（按比例计算偏移）
  const zoomLevel = 2.2; // 放大倍数
  rStyle.value = {
    transform: `translate(-${zoomLevel * topX}px, -${zoomLevel * topY}px)`,
    width: `${zoomLevel * containerWidth}px`,
    height: `${zoomLevel * containerHeight}px`,
  };
}

function outHandler() {
  topShow.value = false;
  rShow.value = false;
}

async function fetchGood() {
  const id = parseInt(route.query.goodId);
  const res = await api.get(`/good/${id}`);
  const dto = res.data.data;
  good.goodName = dto.goodName;
  good.price = dto.price;
  good.images = dto.images.map((b64) => `data:image/png;base64,${b64}`);
}

async function addToCart() {
  if (!checkTokenValidity()) return;

  try {
    const goodId = parseInt(route.query.goodId);
    const cartItem = {
      goodId,
      num: 1,
    };
    const response = await api.post("/addToCart", cartItem, {
      headers: { Authorization: `Bearer ${userInfo.value.token}` },
    });
    if (response.data.code === "200") {
      Swal.fire({
        icon: "success",
        title: "🛒 商品已成功加入购物车！",
        timer: 1500,
        showConfirmButton: false,
      });
      cartCount.value++;
      store.commit("setCartCount", cartCount.value);
    } else {
      Swal.fire({
        icon: "error",
        title: "加入购物车失败",
        text: response.data.msg || "未知错误",
        timer: 1500,
        showConfirmButton: false,
      });
    }
  } catch (error) {
    console.error("加入购物车失败：", error);
    Swal.fire({
      icon: "error",
      title: error.response?.data?.msg
        ? `加入购物车失败：${error.response.data.msg}`
        : "加入购物车失败，请稍后再试。",
      timer: 1500,
      showConfirmButton: false,
    });
  }
}

async function fetchCartCount() {
  if (!store.state.isLogin) return;
  try {
    const res = await api.get(`/cart/count?userId=${store.state.userId}`);
    if (res.data.code === "200") {
      cartCount.value = res.data.data;
      store.commit("setCartCount", cartCount.value);
    }
  } catch (e) {
    console.error("请求购物车数量异常", e);
  }
}

async function fetchFavoriteStatus() {
  if (!store.state.isLogin) {
    isFavorite.value = false;
    return;
  }
  try {
    const userId = store.state.userId;
    const goodId = parseInt(route.query.goodId);
    const res = await api.get(`/favorite/check`, {
      params: { userId, goodId },
    });
    if (res.data.code === "200") {
      isFavorite.value = res.data.data;
    }
  } catch (e) {
    console.error("获取收藏状态异常", e);
  }
}

async function toggleFavorite() {
  if (!checkTokenValidity()) return;
  try {
    const userId = store.state.userId;
    const goodId = parseInt(route.query.goodId);
    if (!isFavorite.value) {
      const res = await api.get("/favorite/add", {
        params: { userId, goodId },
      });
      if (res.data.code === "200") {
        isFavorite.value = true;
        Swal.fire({
          icon: "success",
          title: "收藏成功",
          timer: 1000,
          showConfirmButton: false,
        });
      } else {
        Swal.fire({
          icon: "error",
          title: "收藏失败",
          text: response.data.msg || "未知错误",
          timer: 1500,
          showConfirmButton: false,
        });
      }
    } else {
      const res = await api.delete("/favorite/remove", {
        params: { userId, goodId },
      });
      if (res.data.code === "200") {
        isFavorite.value = false;
        Swal.fire({
          icon: "success",
          title: "已取消收藏",
          timer: 1000,
          showConfirmButton: false,
        });
      } else {
        Swal.fire({
          icon: "error",
          title: "取消收藏失败",
          text: response.data.msg || "未知错误",
          timer: 1500,
          showConfirmButton: false,
        });
      }
    }
  } catch (e) {
    console.error("收藏操作异常", e);
    Swal.fire({
      icon: "error",
      title: "操作失败，请稍后重试",
      timer: 1500,
      showConfirmButton: false,
    });
  }
}

onMounted(() => {
  refreshToken();
  fetchGood();
  if (store.state.isLogin) {
    fetchCartCount();
    fetchFavoriteStatus();
  }
});
</script>

<style scoped>
.detail-container {
  padding: 20px;
  max-width: 1300px;
  margin: 0 auto;
  position: relative;
}

/* 顶部标题栏 */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
  padding: 15px 0;
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
  background: linear-gradient(135deg, #3498db 0%, #2c3e50 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 32px;
}

/* 返回按钮样式 */
.back-button {
  background: #fff;
  border-radius: 12px;
  padding: 12px 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.07);
  border: 1px solid #eaeef5;
  transition: all 0.3s ease;
  font-weight: 600;
}

.back-button:hover {
  box-shadow: 0 8px 25px rgba(52, 152, 219, 0.2);
  transform: translateY(-2px);
  color: #3498db;
}

/* 商品详情主体布局 */
.product-detail {
  display: flex;
  gap: 40px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
  position: relative;
}

/* 图片展示区 */
.image-panel {
  display: flex;
  gap: 20px;
  width: 55%;
}

/* 缩略图区域 */
.thumbnails {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-height: 375px;
  overflow-y: auto;
  width: 80px;
  padding: 5px;
}

.thumbnail-container {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;
}

.thumbnail-container:hover {
  transform: scale(1.05);
}

.thumbnail-container.active {
  border-color: #3498db;
}

.thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 主图区域 */
.main-image-container {
  position: relative;
  width: 375px;
  height: 375px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.main-image-container:hover .main-image {
  transform: scale(1.05);
}

/* 放大镜效果 */
.magnifier {
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid #ddd;
  pointer-events: none;
  cursor: crosshair;
  left: 0;
  top: 0;
  z-index: 10;
}

/* 放大图区域 */
.magnified-view {
  position: absolute;
  left: calc(38% + 12px); /* 主图右边留空 */
  top: 20;
  width: 375px;
  height: 375px;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
  border: 1px solid #eee;
  background: #fff;
  z-index: 10;
}

.magnified-image {
  position: absolute;
  top: 0;
  left: 0;
  object-fit: contain;
}

/* 商品信息区域 */
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 两端对齐 */
}

.name-price {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-name {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1.3;
}

/* 价格区域 */
.price-section {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 5px 0;
}

.price-label {
  font-size: 18px;
  font-weight: 600;
  color: #7f8c8d;
}

.price-display {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 22px;
  font-weight: 600;
  color: #e74c3c;
}

.amount {
  font-size: 26px;
  font-weight: 700;
  color: #e74c3c;
  margin-left: 4px;
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.buy-button {
  padding: 12px 35px;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(to right, #ff6b6b, #ff8e53);
  border: none;
  transition: all 0.3s ease;
}

.buy-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(255, 107, 107, 0.4);
}

.cart-button {
  padding: 12px 35px;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(to right, #3498db, #2c3e50);
  border: none;
  transition: all 0.3s ease;
}

.cart-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(52, 152, 219, 0.4);
}

.favorite-button {
  padding: 12px 30px;
  font-size: 18px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.favorite-button.is-favorite {
  background-color: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border-color: #e74c3c;
}

.favorite-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

/* 购物车图标 */
.cart-block {
  position: fixed;
  top: 30px;
  right: 30px;
  width: 45px;
  height: 45px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s ease;
}

.cart-block:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.2);
}

.cart-icon {
  font-size: 25px;
  color: #3498db;
}

:deep(.el-badge__content) {
  transform: translate(50%, -50%);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .product-detail {
    flex-direction: column;
    gap: 30px;
  }

  .image-panel {
    width: 100%;
    justify-content: center;
  }

  .magnified-view {
    display: none;
  }

  .product-info {
    gap: 30px;
  }
}

@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .image-panel {
    flex-direction: column;
    align-items: center;
  }

  .thumbnails {
    flex-direction: row;
    max-height: 80px;
    width: 100%;
    overflow-x: auto;
    padding: 10px 0;
  }

  .action-buttons {
    flex-direction: column;
  }

  .buy-button,
  .cart-button,
  .favorite-button {
    width: 100%;
  }
}
</style>