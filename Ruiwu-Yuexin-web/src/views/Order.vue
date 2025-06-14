<!-- src/views/OrderList.vue -->
<template>
  <div class="order-container">
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-box-open"></i>
        <h1>订单管理</h1>
      </div>
      <el-button type="primary" plain class="back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i> 返回
      </el-button>
    </div>

    <div class="order-filter-container">
      <el-button
        v-for="status in orderStatuses"
        :key="status.name"
        :class="[
          'filter-button',
          status.class,
          { active: filterStatus === status.name },
        ]"
        @click="changeFilterStatus(status.name)"
      >
        {{ status.label }}
        <span class="count-badge">
          {{ getCount(status.name) }}
        </span>
      </el-button>
    </div>
    <div class="orders-list-container">
      <div class="loader-animation" v-if="loading">
        <div class="spinner"></div>
      </div>

      <div class="orders-list" v-show="!loading">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-card"
          :class="{ 'new-item': isNewOrder(order.id) }"
        >
          <div class="order-header">
            <div class="order-info">
              <div class="order-info-item">
                <span class="info-label">订单号</span>
                <span class="info-value">{{ order.id }}</span>
              </div>
              <div class="order-info-item">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ order.created }}</span>
              </div>
              <div class="order-info-item">
                <span class="info-label">订单金额</span>
                <span class="info-value">¥{{ order.price }}</span>
              </div>
            </div>
            <div
              :class="[
                'order-status',
                `status-${getStatusClass(order.status)}`,
              ]"
            >
              {{ getOrderStatus(order.status) }}
            </div>
          </div>

          <div class="order-content">
            <div class="product-info">
              <img
                :src="`data:image/png;base64,${order.goodImage}`"
                class="product-image"
                alt="商品图片"
              />
              <div class="product-details">
                <h3 class="product-name">{{ order.goodName }}</h3>
                <div class="product-meta">
                  <span>数量: {{ order.num }}</span>
                  <span> · </span>
                  <span>单价: ¥{{ order.unitPrice }}</span>
                </div>
                <div class="product-price">¥{{ order.price }}</div>
              </div>
            </div>

            <div class="order-summary">
              <div class="total-price">总价: ¥{{ order.price }}</div>
              <el-button
                v-if="order.status === 3"
                type="primary"
                class="action-button"
                @click="confirmReceipt(order.id)"
              >
                确认收货
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!filteredOrders.length && !loading" class="empty-state">
        <i class="fas fa-inbox"></i>
        <h3>暂无{{ filterStatusLabel }}订单</h3>
        <p>您还没有任何{{ filterStatusLabel }}订单记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";

const router = useRouter();
const allOrders = ref([]);
const filterStatus = ref(null);
const loading = ref(false);
const displayedOrders = ref(new Set());
const newOrders = ref(new Set());

// 订单状态定义
const orderStatuses = [
  {
    name: null,
    label: "所有订单",
    class: "all-orders",
  },
  {
    name: 1,
    label: "待支付",
    class: "Pending-payment",
  },
  {
    name: 2,
    label: "待发货",
    class: "pending-shipment",
  },
  {
    name: 3,
    label: "已发货",
    class: "Shipped",
  },
  {
    name: 4,
    label: "已完成",
    class: "completed",
  },
];

const filteredOrders = computed(() => {
  if (filterStatus.value === null) {
    return allOrders.value;
  }
  return allOrders.value.filter((order) => order.status === filterStatus.value);
});

const filterStatusLabel = computed(() => {
  const status = orderStatuses.find((s) => s.name === filterStatus.value);
  return status ? status.label : "";
});

const getStatusClass = (status) => {
  switch (status) {
    case 1:
      return "Pending-payment";
    case 2:
      return "pending-shipment";
    case 3:
      return "Shipped";
    case 4:
      return "completed";
    default:
      return "";
  }
};

const changeFilterStatus = (status) => {
  loading.value = true;

  // 标记当前显示的所有订单为"旧"订单
  allOrders.value.forEach((order) => {
    displayedOrders.value.add(order.id);
  });

  filterStatus.value = status;

  setTimeout(() => {
    loading.value = false;
    // 确保订单更新后，新显示的订单有动画
    allOrders.value.forEach((order) => {
      if (!displayedOrders.value.has(order.id)) {
        newOrders.value.add(order.id);
      }
    });

    // 清除之前标记的订单
    displayedOrders.value = new Set();

    // 清除新订单标记（4秒后）
    setTimeout(() => {
      newOrders.value.clear();
    }, 4000);
  }, 500);
};

const isNewOrder = (orderId) => {
  return newOrders.value.has(orderId);
};

const fetchOrders = async () => {
  loading.value = true;
  try {
    const userId = JSON.parse(localStorage.getItem("userInfo")).id;
    const res = await api.get("/order/list", {
      params: { userId },
    });

    if (res.data.code === "200") {
      allOrders.value = res.data.data;

      // 标记所有获取到的订单为"新"订单用于入场动画
      res.data.data.forEach((order) => {
        newOrders.value.add(order.id);
      });

      // 清除新订单标记（4秒后）
      setTimeout(() => {
        newOrders.value.clear();
      }, 4000);
    } else {
      ElMessage.error("加载订单失败");
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("获取订单出错");
  } finally {
    setTimeout(() => {
      loading.value = false;
    }, 800);
  }
};

const getCount = (status) => {
  if (status === null) return allOrders.value.length;

  return allOrders.value.filter((order) => order.status === status).length;
};

const goBack = () => {
  router.push("/home");
};

const getOrderStatus = (status) => {
  switch (status) {
    case 1:
      return "待支付";
    case 2:
      return "待发货";
    case 3:
      return "已发货";
    case 4:
      return "已完成";
    default:
      return "未知";
  }
};

const confirmReceipt = async (orderId) => {
  try {
    const res = await api.post(`/order/confirmReceipt/${orderId}`);

    if (res.data.code === "200") {
      ElMessage.success("确认收货成功");

      // 更新订单状态
      const order = allOrders.value.find((o) => o.id === orderId);
      if (order) {
        order.status = 4;
      }
    } else {
      ElMessage.error("确认收货失败");
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("确认收货出错");
  }
};

onMounted(fetchOrders);
onMounted(() => {
  document.body.style.overflow = "hidden";
});
onUnmounted(() => {
  document.body.style.overflow = "";
});
</script>

<style scoped>
.order-container {
  height: 100vh;
  overflow: hidden;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.orders-list-container {
  max-height: calc(100vh - 250px);
  overflow-y: scroll;
  scrollbar-width: thin;
  scrollbar-color: rgba(128, 128, 128, 0.5) transparent;
  scroll-behavior: smooth;
  padding-right: 8px;
}

.orders-list-container::-webkit-scrollbar {
  width: 8px;
}
.orders-list-container::-webkit-scrollbar-track {
  background: transparent;
}
.orders-list-container::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.2);
  border-radius: 4px;
}
.orders-list-container:hover::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.5);
}

/* 调整空状态的高度 */

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
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
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
  box-shadow: 0 8px 25px rgba(39, 102, 245, 0.2);
  transform: translateY(-2px);
  color: #2766f5;
}

/* 订单过滤按钮 */
.order-filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
}

.filter-button {
  flex: 1;
  min-width: 150px;
  border-radius: 10px;
  padding: 15px 20px;
  font-weight: 600;
  transition: transform 0.3s ease, box-shadow 0.3s ease,
    background 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  text-align: center;
  box-shadow: 0 4px 6px rgba(50, 50, 93, 0.08), 0 1px 3px rgba(0, 0, 0, 0.05);
  border: none;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border: none;
}

.filter-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 14px rgba(50, 50, 93, 0.1), 0 3px 6px rgba(0, 0, 0, 0.08);
}

.filter-button.active {
  box-shadow: 0 4px 15px rgba(39, 102, 245, 0.3);
  transform: scale(1.02) translateY(-2px);
}

.filter-button::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 100%;
  transform: translate(-50%, -50%);
  transition: width 0.5s ease, height 0.5s ease;
}

.filter-button:active::after {
  width: 500px;
  height: 500px;
}

.all-orders {
  background: linear-gradient(to right, #ece9e6, #ffffff);
  color: #6c757d;
}

.Pending-payment {
  background: linear-gradient(to right, #f6d365, #ef4444);
  color: #fff;
}

.pending-shipment {
  background: linear-gradient(to right, #f6d365, #fda085);
  color: #fff;
}

.shipped {
  background: linear-gradient(to right, #5ee7df, #6a87d5);
  color: #fff;
}

.completed {
  background: linear-gradient(to right, #87e3f4, #90ea99);
  color: #fff;
}

.count-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #fff;
  color: #2766f5;
  border: 2px solid #2766f5;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  animation: popIn 0.3s ease;
}

@keyframes popIn {
  0% {
    transform: scale(0);
  }
  80% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

/* 订单卡片 */
.orders-list {
  position: relative;
  transition: height 0.5s cubic-bezier(0.23, 1, 0.32, 1);
}

.order-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
  margin-bottom: 25px;
  overflow: hidden;
  transition: transform 0.5s cubic-bezier(0.23, 1, 0.32, 1), opacity 0.4s ease,
    box-shadow 0.3s ease;
  transform-origin: top center;
  border: 1px solid #edf2f9;
  animation: fadeIn 0.5s ease forwards;
}

/* .order-card.new-item {
  animation: highlight 1.5s ease;
} */

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes highlight {
  0% {
    background-color: rgba(255, 247, 175, 0.1);
    transform: translateY(20px);
    opacity: 0;
  }
  30% {
    background-color: rgba(255, 247, 175, 0.3);
    opacity: 1;
  }
  100% {
    background-color: white;
    transform: translateY(0);
  }
}

.order-card:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

/* 订单头部 */
.order-header {
  padding: 20px 25px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  background: #f8fafc;
  border-bottom: 1px solid #edf2f9;
  transition: all 0.3s ease;
}

.order-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.order-info-item {
  display: flex;
  flex-direction: column;
  min-width: 150px;
}

.info-label {
  font-size: 12px;
  color: #6c757d;
  font-weight: 500;
}

.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-top: 5px;
  transition: all 0.3s ease;
}

.order-status {
  padding: 6px 15px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
}

.status-Pending-payment {
  background-color: rgba(235, 113, 85, 0.15);
  color: #fb1212;
}
.status-pending-shipment {
  background-color: rgba(253, 182, 27, 0.15);
  color: #fdb61b;
}

.status-shipped {
  background-color: rgba(33, 150, 243, 0.15);
  color: #2196f3;
}

.status-completed {
  background-color: rgba(76, 175, 80, 0.15);
  color: #4caf50;
}

/* 订单内容 */
.order-content {
  padding: 25px;
  transition: all 0.3s ease;
}

.product-info {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.product-details {
  flex: 1;
  transition: all 0.3s ease;
}

.product-name {
  font-weight: 700;
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 5px;
  transition: all 0.3s ease;
}

.product-meta {
  color: #6c757d;
  font-size: 14px;
  transition: all 0.3s ease;
}

.product-price {
  font-size: 17px;
  font-weight: 700;
  color: #2766f5;
  margin-top: 8px;
  transition: all 0.3s ease;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.total-price {
  font-weight: 700;
  font-size: 20px;
  color: #2c3e50;
  transition: all 0.3s ease;
}

.action-button {
  padding: 10px 25px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
  background: linear-gradient(to right, #2766f5, #3a8dff);
  border: none;
  color: white;
}

.action-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(39, 102, 245, 0.35);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  margin-top: 30px;
  animation: fadeIn 0.8s ease forwards;
}

.empty-state i {
  font-size: 70px;
  color: #d1e0fc;
  margin-bottom: 25px;
  transition: all 0.5s ease;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #2c3e50;
  transition: all 0.5s ease;
}

.empty-state p {
  max-width: 500px;
  margin: 0 auto;
  color: #6c757d;
  font-size: 16px;
  line-height: 1.6;
  transition: all 0.5s ease;
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
  border: 3px solid rgba(39, 102, 245, 0.3);
  border-radius: 50%;
  border-top: 3px solid #2766f5;
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

  .order-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-info {
    width: 100%;
  }

  .filter-button {
    min-width: 120px;
    padding: 12px 15px;
    font-size: 14px;
  }

  .order-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
}
</style>