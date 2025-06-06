<template>
  <div class="all-container">
    <!-- 顶部返回 -->
    <el-page-header title="返回" @back="backToList" content="商品详情">
      <template #icon>
        <img
          src="@/assets/icon/back.png"
          alt="返回"
          style="width: 25px; height: 25px; vertical-align: middle"
        />
      </template>
      <template #extra>
        <div class="cart-block" @click="goToCart">
          <el-badge :value="cartCount" class="cart-badge">
            <!-- 用 img 替换内置图标 -->
            <img
              src="@/assets/icon/购物车.svg"
              width="40px"
              height="40px"
              alt="购物车"
              class="cart-img"
            />
          </el-badge>
        </div>
      </template>
    </el-page-header>

    <div class="detail-container">
      <div class="image-panel">
        <!-- 1. 左侧：垂直缩略图 -->
        <div class="thumbs">
          <img
            v-for="(url, idx) in good.images"
            :key="idx"
            :src="url"
            :class="{ active: idx === current }"
            @mouseenter="selectImage(idx)"
          />
        </div>

        <!-- 2. 中间：主图 + 蒙版 -->
        <div
          class="left"
          @mouseenter="enterHandler"
          @mousemove="moveHandler"
          @mouseleave="outHandler"
        >
          <img class="leftImg" :src="good.images[current]" />
          <div class="maskTop" v-show="topShow" :style="topStyle"></div>
        </div>

        <!-- 3. 右侧：放大后的图 -->
        <div class="right" v-show="rShow">
          <img class="rightImg" :src="good.images[current]" :style="rStyle" />
        </div>
      </div>

      <!-- 4. 商品信息区 -->
      <div class="detailInfo">
        <div class="goodName">
          <h1 class="goodName">{{ good.goodName }}</h1>
        </div>
        <div class="price-box">
          <div class="price-left">
            <span class="currency">¥</span
            ><span class="amount">{{ good.price }}</span>
          </div>
          <div class="price-right">
            <img src="@/assets/icon/热门.svg" alt="火" class="fire-icon" />
          </div>
        </div>
        <div class="buy-buttons-fixed">
          <button class="buy-now" @click="handleBuyNow">立即购买</button>
          <button class="add-cart" @click="addToCart">加入购物车</button>
          <button class="favorite" @click="toggleFavorite">
            <img
              :src="
                isFavorite
                  ? require('@/assets/icon/收藏-已收藏.svg')
                  : require('@/assets/icon/收藏.svg')
              "
              alt="收藏"
              class="icon-svg"
            />
          </button>
        </div>
      </div>
      <!-- … 这里写你的按钮、价格、描述等 … -->
    </div>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useStore } from "vuex";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";
import OrderDialog from "@/components/OrderDialog.vue";
import { useAuth } from "@/composables/useAuth";

import { computed } from "vue";
const userInfo = computed(() => store.state.userInfo);
const { checkTokenValidity, refreshToken } = useAuth();
const router = useRouter();
const route = useRoute();
const store = useStore();

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
// 新增：处理“立即购买”按钮
function handleBuyNow() {
  // 先检查 token
  if (!checkTokenValidity()) return;
  // token 有效，就显示下单弹窗
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
  let x = e.clientX - rect.left;
  let y = e.clientY - rect.top;
  let topX = Math.max(0, Math.min(x - 125, 375));
  let topY = Math.max(0, Math.min(y - 125, 375));
  topStyle.value = { transform: `translate(${topX}px, ${topY}px)` };
  rStyle.value = { transform: `translate(-${2 * topX}px, -${2 * topY}px)` };
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
      ElMessage.success("🛒 商品已成功加入购物车！");
      cartCount.value++;
      store.commit("setCartCount", cartCount.value);
    } else {
      ElMessage.error(`加入购物车失败：${response.data.msg || "未知错误"}`);
    }
  } catch (error) {
    console.error("加入购物车失败：", error);
    ElMessage.error(
      error.response?.data?.msg
        ? `加入购物车失败：${error.response.data.msg}`
        : "加入购物车失败，请稍后再试。"
    );
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
        ElMessage.success("收藏成功！");
      } else {
        ElMessage.error(`收藏失败：${res.data.msg || "未知错误"}`);
      }
    } else {
      const res = await api.delete("/favorite/remove", {
        params: { userId, goodId },
      });
      if (res.data.code === "200") {
        isFavorite.value = false;
        ElMessage.success("取消收藏成功！");
      } else {
        ElMessage.error(`取消收藏失败：${res.data.msg || "未知错误"}`);
      }
    }
  } catch (e) {
    console.error("收藏操作异常", e);
    ElMessage.error("操作失败，请稍后再试。");
  }
}

onMounted(() => {
  refreshToken();
  fetchGood();
  if (store.state.isLogin) {
    fetchCartCount();
    fetchFavoriteStatus();
  } else {
    store.commit("setLogin", { isLogin: false, userId: null });
  }
});
</script>



<style scoped>
.detail-container {
  display: flex;
  position: relative;
  padding: 20px 20px 20px 70px;
  gap: 150px;
}
.cart-block img {
  width: 50px; /* 新宽度 */
  height: 50px; /* 新高度 */
}
.cart-block {
  position: absolute;
  right: 40px; /* 相对于 page-header 调整 */
  top: 5%;
  transform: translateY(-50%);
  z-index: 10; /* 提升在最上层，免得被内容遮住 */
  cursor: pointer;
}
#app {
  margin-top: 0px;
}
:deep(.el-badge__content.is-fixed) {
  transform: translateY(-10%) translateX(50%);
  box-shadow: none;
  border: none;
}
/* 整体图片区：缩略 + 主 + 放大 横向 */
.image-panel {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}
.image-panel .right {
  position: absolute; /* 脱离文档流，改成绝对定位 */
  top: 20px; /* 距离 detail-container 顶部 20px（和 padding 保持一致）*/
  left: calc(45px + 500px + 45px);
  /* = container 左 padding(20px) + .left 宽度(500px) + 缩略图和主图间隙(20px) */
  width: 500px;
  height: 500px;
  overflow: hidden;
  z-index: 10; /* 确保覆盖文字 */
  margin-left: 63; /* 取消原来的 margin-left */
}

/* 缩略图列表 */
.thumbs {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 500px;
  overflow-y: auto;
}
.thumbs img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border: 2px solid transparent;
  cursor: pointer;
}
.thumbs img.active {
  border-color: #ff5000;
}

/* 主图区域 */
.left {
  position: relative;
  width: 500px;
  height: 500px;
  overflow: hidden;
}
.leftImg {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 小蒙版 */
.maskTop {
  position: absolute;
  width: 250px;
  height: 250px;
  background-color: rgba(200, 200, 200, 0.4);
  pointer-events: none;
  top: 0;
  left: 0;
}

/* 右侧放大图 */
.right {
  width: 500px;
  height: 500px;
  /* position: relative; */
  overflow: hidden;
  /* z-index: 2; */
  margin-left: 120px; /* ✅ 原图右边留点距离 */
}

.rightImg {
  position: absolute;
  width: 1000px;
  height: 1000px;
  object-fit: cover;
  top: 0;
  left: 0;
}

/* 右侧信息区 */
.detailInfo {
  display: flex;
  flex-direction: column; /* 从横向改为纵向 */
  align-items: flex-start; /* 左对齐 */
}
.goodName {
  font-size: 20px;
}

.price-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  border-radius: 10px;
  padding: 16px 20px;
  width: 450px;
  height: 50px;
  background: url("@/assets/icon/price-back.jpg") no-repeat center center;
  background-size: cover;
}

.price-box .price-left {
  display: flex;
}
.amount {
  font-size: 1.8em;
}
.currency {
  font-size: 0.9rem; /* 比整体字体略小 */
  padding-top: 12px;
  margin-right: 2px; /* 给 ¥ 和金额留点空间 */
}
.price-box .price {
  font-size: 20px;
  font-weight: bold;
}

.price-box .price-right {
  display: flex;
  align-items: center;
}

.fire-icon {
  width: 30px;
  height: 30px;
  margin-right: 10px;
  filter: invert(28%) sepia(93%) saturate(7488%) hue-rotate(0deg)
    brightness(100%) contrast(103%);
}

.promo .title {
  font-style: italic;
  font-weight: bold;
  font-size: 14px;
}
.el-page-header {
  padding-left: 20px;
}
.promo .time {
  font-size: 12px;
}

.detail {
  margin: 20px 0;
  width: 100%;
  height: 400px;
  display: flex; /* ✅ 横向排列 */
}
/* 固定在右侧底部的按钮区域 */
.buy-buttons-fixed {
  margin-top: 310px;
  display: flex;
}

.buy-now {
  height: 52px;
  width: 240px;
  font-size: 16px;
  border: none;
  color: #fff;
  border-radius: 10px 0 0 10px; /* 左圆角 */
  cursor: pointer;
  font-weight: bold;

  background: linear-gradient(to right, #ff6600, #ff3300);
}

.add-cart {
  height: 52px;
  width: 240px;
  font-size: 16px;
  border: none;
  color: #fff;
  border-radius: 0 10px 10px 0; /* 右圆角 */
  cursor: pointer;
  font-weight: bold;
  background: linear-gradient(to right, #ffcc00, #ff9900);
}

/* 去掉两按钮之间的间距 */
.buy-now,
.add-cart {
  margin: 0;
}

.icon-svg {
  width: 30px;
  height: 30px;
  vertical-align: middle;
  margin-right: 4px;
  padding-left: 10px;
}

/* 去掉收藏按钮的边框和背景 */
.favorite {
  border: none !important;
  background: none !important;
  padding: 0;
}
</style>
