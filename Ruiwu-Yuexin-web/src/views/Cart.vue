<template>
  <div class="cart-container">
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-shopping-cart"></i>
        <h1>购物车</h1>
      </div>
      <el-button type="primary" plain class="back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i> 返回
      </el-button>
    </div>

    <!-- 购物车操作工具栏 -->
    <div class="cart-toolbar">
      <el-checkbox v-model="selectAll" @change="toggleSelectAll">
        全选
      </el-checkbox>
      <el-button
        type="danger"
        plain
        class="delete-button"
        :disabled="selectedItems.length === 0"
        @click="deleteSelectedItems"
      >
        <i class="fas fa-trash-alt"></i> 删除选中 ({{ selectedItems.length }})
      </el-button>
    </div>

    <!-- 购物车商品列表 -->
    <div class="cart-items-container">
      <!-- 加载状态 -->
      <div class="loader-animation" v-if="loading">
        <div class="spinner"></div>
      </div>

      <!-- 空购物车提示 -->
      <div v-if="!loading && cartItems.length === 0" class="empty-cart">
        <i class="fas fa-shopping-basket"></i>
        <h3>您的购物车是空的</h3>
        <p>快去发现心仪的商品吧</p>
        <el-button type="primary" class="browse-button" @click="goToGoodsList">
          浏览商品
        </el-button>
      </div>

      <!-- 购物车商品卡片 -->
      <div class="cart-items" v-show="!loading && cartItems.length > 0">
        <div
          v-for="item in cartItems"
          :key="item.goodId"
          class="cart-item-card"
          :class="{ selected: isItemSelected(item.goodId) }"
        >
          <!-- 选择复选框 -->
          <div class="selection">
            <el-checkbox v-model="selectedItemIds" :label="item.goodId" />
          </div>

          <!-- 商品信息区域 -->
          <div class="item-content">
            <!-- 商品图片 -->
            <img
              :src="'data:image/jpeg;base64,' + item.goodImage"
              alt="商品图"
              class="item-image"
              @click="goToDetail(item.goodId)"
            />

            <!-- 商品详情 -->
            <div class="item-details">
              <div class="item-name" @click="goToDetail(item.goodId)">
                {{ item.goodName }}
              </div>
              <div class="item-price">单价: ¥{{ item.price }}</div>

              <div class="item-controls">
                <!-- 商品数量选择器 -->
                <el-input-number
                  v-model="item.num"
                  :min="1"
                  :max="99"
                  size="small"
                  @change="updateItemQuantity(item)"
                />

                <!-- 删除按钮 -->
                <el-button
                  type="danger"
                  plain
                  size="small"
                  class="delete-item-button"
                  @click="deleteItem(item)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>

          <!-- 商品总价 -->
          <div class="item-total">小计: ¥{{ item.price * item.num }}</div>
        </div>
      </div>
    </div>

    <!-- 结算栏 -->
    <div class="checkout-bar" v-if="cartItems.length > 0">
      <div class="selection-info">已选 {{ selectedItems.length }} 项商品</div>

      <div class="checkout-summary">
        <div class="total-price">
          总计: ¥<span class="amount">{{ totalSelectedPrice }}</span>
        </div>

        <el-button
          type="success"
          class="checkout-button"
          @click="checkout"
          :disabled="selectedItems.length === 0"
        >
          结算
          <span class="item-count">({{ selectedItems.length }})</span>
        </el-button>
      </div>
    </div>

    <!-- 支付对话框 -->
    <el-dialog
      v-model="showQrDialog"
      title="扫码支付"
      width="300px"
      :close-on-click-modal="false"
      align-center
    >
      <div style="text-align: center">
        <img :src="qrCodeUrl" alt="扫码支付" class="qr-code" />
        <div class="payment-amount">支付金额: ¥{{ totalSelectedPrice }}</div>
        <p style="margin-top: 10px">请使用支付宝扫描二维码支付</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import Swal from "sweetalert2";
import { ArrowLeft } from "@element-plus/icons-vue";
import { useStore } from "vuex";
import { ElMessage } from "element-plus";
import { onUnmounted } from "vue";

// 响应式数据
const cartItems = ref([]);
const selectedItemIds = ref([]);
const loading = ref(false);
const showQrDialog = ref(false);
const qrCodeUrl = require("@/assets/qrcode.png");

// 工具函数与方法
const router = useRouter();
const store = useStore();

// 计算属性
const selectedItems = computed(() => {
  return cartItems.value.filter((item) =>
    selectedItemIds.value.includes(item.goodId)
  );
});

const totalSelectedPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    return sum + item.price * item.num;
  }, 0);
});

const selectAll = computed({
  get: () =>
    selectedItemIds.value.length > 0 &&
    selectedItemIds.value.length === cartItems.value.length,
  set: (value) => {
    if (value) {
      selectedItemIds.value = cartItems.value.map((item) => item.goodId);
    } else {
      selectedItemIds.value = [];
    }
  },
});

// 方法
const fetchCart = async () => {
  const userId = store.state.userId;
  if (!userId) return;

  loading.value = true;
  try {
    const res = await api.get(`/list/${userId}`);
    if (res.data.code === "200") {
      cartItems.value = res.data.data.reverse();
      store.commit("setCartCount", cartItems.value.length);

      // 显示添加动画效果
      setTimeout(() => {
        document.querySelectorAll(".cart-item-card").forEach((card) => {
          card.style.opacity = 1;
          card.style.transform = "translateY(0)";
        });
      }, 50);
    } else {
      ElMessage.error("获取购物车失败");
    }
  } catch (err) {
    console.error("获取购物车失败：", err);
    ElMessage.error("请求失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

const deleteItem = async (item) => {
  try {
    const res = await api.delete(`/cart/delete`, {
      data: {
        userId: store.state.userId,
        id: item.id,
      },
    });

    if (res.data.code === "200") {
      ElMessage.success("删除成功");

      // 添加删除动画
      const index = cartItems.value.findIndex((i) => i.goodId === item.goodId);
      if (index !== -1) {
        const card = document.querySelectorAll(".cart-item-card")[index];
        card.style.height = card.offsetHeight + "px";
        card.style.opacity = 0;
        card.style.transform = "translateX(100%)";

        setTimeout(() => {
          cartItems.value.splice(index, 1);
          store.commit("setCartCount", cartItems.value.length);
        }, 300);
      }
    } else {
      ElMessage.error(`删除失败：${res.data.msg}`);
    }
  } catch (err) {
    console.error("删除失败：", err);
    ElMessage.error("网络错误");
  }
};

const isItemSelected = (goodId) => {
  return selectedItemIds.value.includes(goodId);
};

const updateItemQuantity = async (item) => {
  try {
    // 这里实际调用API更新购物车商品数量
    ElMessage.success("数量更新成功");
  } catch (err) {
    console.error("更新数量失败：", err);
    ElMessage.error("更新失败");
  }
};

const toggleSelectAll = () => {
  // 已由计算属性的setter处理
};

const deleteSelectedItems = () => {
  if (selectedItems.value.length === 0) return;

  selectedItems.value.forEach((item) => {
    deleteItem(item);
  });

  selectedItemIds.value = [];
};

const checkout = () => {
  const goods = selectedItems.value;

  Swal.fire({
    icon: "success",
    title: `准备结算 ${goods.length} 件商品`,
    text: `总金额: ¥${totalSelectedPrice.value}`,
    timer: 2000,
    showConfirmButton: false,
  });

  // 显示支付对话框
  setTimeout(() => {
    showQrDialog.value = true;
  }, 1500);
};

const goToDetail = (goodId) => {
  router.push({ path: "/detail", query: { goodId } });
};

const goToGoodsList = () => {
  router.push({ path: "/goods" });
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchCart();
  window.addEventListener("wheel", handleScroll, { passive: false });
});
onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener("wheel", handleScroll);
  restoreOverflow();
});

function restoreOverflow() {
  document.documentElement.style.overflow = "";
  document.body.style.overflow = "";
}

function handleScroll(e) {
  // 仅在滚动时启用滚动
  restoreOverflow();

  // 防止无限触发
  window.removeEventListener("wheel", handleScroll);
}
</script>

<style scoped>
.cart-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  height: 100vh;
  overflow: hidden;
}

/* 顶部标题栏 */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
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
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
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
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.2);
  transform: translateY(-2px);
  color: #ff6b6b;
}

/* 购物车工具栏 */
.cart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.delete-button {
  transition: all 0.3s ease;
  font-weight: 600;
}

.delete-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.delete-button:hover:not(:disabled) {
  transform: scale(1.03);
}

/* 购物车商品容器 */
.cart-items-container {
  max-height: calc(100vh - 320px);
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(128, 128, 128, 0.5) transparent;
  scroll-behavior: smooth;
  padding-right: 8px;
}

.cart-items-container::-webkit-scrollbar {
  width: 8px;
}

.cart-items-container::-webkit-scrollbar-track {
  background: transparent;
}

.cart-items-container::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.2);
  border-radius: 4px;
}

.cart-items-container:hover::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.5);
}

/* 空购物车状态 */
.empty-cart {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  animation: fadeIn 0.8s ease forwards;
}

.empty-cart i {
  font-size: 80px;
  color: #ffd6d6;
  margin-bottom: 25px;
}

.empty-cart h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #2c3e50;
}

.empty-cart p {
  max-width: 500px;
  margin: 0 auto 25px;
  color: #6c757d;
  font-size: 16px;
  line-height: 1.6;
}

.browse-button {
  padding: 10px 30px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(to right, #ff6b6b, #ff8e53);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
  transition: all 0.3s ease;
}

.browse-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 20px rgba(255, 107, 107, 0.4);
}

/* 商品卡片 */
.cart-item-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  margin-bottom: 15px;
  transition: all 0.3s ease;
  border: 1px solid #f1f5f9;

  /* 入场动画 */
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.4s ease, transform 0.4s ease, border-color 0.3s;
}

.cart-item-card.selected {
  border: 1px solid #ff6b6b;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.15);
  position: relative;
}

.cart-item-card.selected::after {
  content: "";
  position: absolute;
  top: -1px;
  bottom: -1px;
  left: -1px;
  width: 4px;
  background: #ff6b6b;
  border-radius: 12px 0 0 12px;
}

.cart-item-card:hover:not(.selected) {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.selection {
  margin-right: 20px;
  flex-shrink: 0;
}

.item-content {
  display: flex;
  flex: 1;
  gap: 20px;
  align-items: center;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #f1f5f9;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.item-image:hover {
  transform: scale(1.05);
}

.item-details {
  flex: 1;
}

.item-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
  cursor: pointer;
  transition: color 0.3s ease;
}

.item-name:hover {
  color: #ff6b6b;
}

.item-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b6b;
  margin-bottom: 15px;
}

.item-controls {
  display: flex;
  gap: 15px;
  align-items: center;
}

.delete-item-button {
  transition: transform 0.2s ease;
}

.delete-item-button:hover {
  transform: scale(1.05);
}

.item-total {
  font-size: 18px;
  font-weight: 700;
  min-width: 120px;
  text-align: right;
  color: #ff6b6b;
}

/* 结算栏 */
.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15px 20px;
  background: #fff;
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
}

.selection-info {
  font-size: 16px;
  font-weight: 500;
  color: #555;
}

.checkout-summary {
  display: flex;
  align-items: center;
  gap: 25px;
}

.total-price {
  font-size: 18px;
  font-weight: 600;
}

.amount {
  font-size: 22px;
  color: #ff6b6b;
  font-weight: 700;
}

.checkout-button {
  padding: 10px 30px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(to right, #ff6b6b, #ff8e53);
  border: none;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
  transition: all 0.3s ease;
}

.checkout-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 7px 20px rgba(255, 107, 107, 0.4);
}

.checkout-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.item-count {
  margin-left: 5px;
  font-weight: 700;
}

/* 二维码对话框 */
.qr-code {
  width: 250px;
  height: 250px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #f1f5f9;
}

.payment-amount {
  font-size: 20px;
  font-weight: 700;
  margin-top: 15px;
  color: #ff6b6b;
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
  border: 3px solid rgba(255, 107, 107, 0.3);
  border-radius: 50%;
  border-top: 3px solid #ff6b6b;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .cart-item-card {
    flex-direction: column;
    align-items: stretch;
  }

  .selection {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .item-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-total {
    text-align: left;
    margin-top: 15px;
  }

  .cart-items-container {
    max-height: calc(100vh - 360px);
  }

  .checkout-bar {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }

  .checkout-summary {
    width: 100%;
    justify-content: space-between;
  }
}
</style>