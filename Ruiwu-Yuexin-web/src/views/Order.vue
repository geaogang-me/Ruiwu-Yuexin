<template>
  <div class="order-container">
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-box-open"></i>
        <h1>订单管理</h1>
      </div>
      <el-button plain class="back-button" @click="navigateTo('/home')">
        <i class="fas fa-arrow-left"></i> 返回
      </el-button>
    </div>

    <!-- 筛选按钮 -->
    <div class="order-filter-container">
      <el-button
        v-for="s in filterStatuses"
        :key="s.name"
        :class="['filter-button', s.class, { active: filterStatus === s.name }]"
        @click="onFilter(s.name)"
      >
        {{ s.label }}
        <span class="count-badge">{{ countByStatus(s.name) }}</span>
      </el-button>
    </div>

    <!-- 列表或加载中 -->
    <div class="orders-list-container">
      <div v-if="loading" class="loader-animation">
        <div class="spinner"></div>
      </div>

      <div v-else class="orders-list">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-card"
          :class="{ 'new-item': isNew(order.id) }"
        >
          <!-- 头部信息 -->
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
            <div :class="['order-status', statusMap[order.status].class]">
              {{ statusMap[order.status].label }}
            </div>
          </div>

          <!-- 内容 & 操作按钮 -->
          <div class="order-content">
            <div class="product-info">
              <img :src="order.goodImageUrl" class="product-image" />
              <div class="product-details">
                <h3>{{ order.goodName }}</h3>
                <div class="product-meta">
                  <span>数量: {{ order.num }}</span> ·
                  <span>单价: ¥{{ order.unitPrice }}</span>
                </div>
              </div>
            </div>
            <div class="order-summary">
              <el-button
                v-if="order.status === 1"
                type="primary"
                class="gopay-button"
                @click="navigateTo('/payment', { orderId: order.id })"
              >
                去支付
              </el-button>
              <el-button
                v-else-if="order.status === 3"
                type="primary"
                class="takegoods-button"
                @click="onConfirmReceipt(order.id)"
              >
                确认收货
              </el-button>
              <el-button
                v-else-if="order.status === 4"
                type="primary"
                class="gocomment-button"
                @click="
                  navigateTo('/evaluate', {
                    orderId: order.id,
                    goodId: order.goodId,
                  })
                "
              >
                去评价
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="!filteredOrders.length" class="empty-state">
          <i class="fas fa-inbox"></i>
          <h3>暂无{{ currentLabel }}订单</h3>
          <p>您还没有任何{{ currentLabel }}订单记录</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import Swal from "sweetalert2";

// （A）用于筛选按钮的状态数组
const filterStatuses = [
  { name: null, label: "所有订单", class: "all-orders" },
  { name: 1, label: "待付款", class: "Pending-payment" },
  { name: 2, label: "待发货", class: "pending-shipment" },
  { name: 3, label: "待收货", class: "Shipped" },
  { name: 4, label: "待评价", class: "no_comment" },
];

// （B）用于卡片头部映射所有状态
const statusMap = {
  1: { label: "待付款", class: "Pending-payment" },
  2: { label: "待发货", class: "pending-shipment" },
  3: { label: "待收货", class: "Shipped" },
  4: { label: "待评价", class: "no_comment" },
  5: { label: "已完成", class: "completed" },
};

// 通用弹窗
const swalSuccess = (msg) =>
  Swal.fire({
    icon: "success",
    title: msg,
    timer: 1000,
    showConfirmButton: false,
  });
const swalError = (msg) =>
  Swal.fire({
    icon: "error",
    title: msg,
    timer: 1500,
    showConfirmButton: false,
  });
const confirmDialog = (title, text) =>
  Swal.fire({
    title,
    text,
    icon: "question",
    showCancelButton: true,
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  });

// 组合式状态
const router = useRouter();
const allOrders = ref([]);
const filterStatus = ref(null);
const loading = ref(false);
const displayed = ref(new Set());
const newItems = ref(new Set());

// 计算属性
const filteredOrders = computed(() =>
  filterStatus.value == null
    ? allOrders.value
    : allOrders.value.filter((o) => o.status === filterStatus.value)
);

const currentLabel = computed(() => {
  const found = filterStatuses.find((s) => s.name === filterStatus.value);
  return found ? found.label : "所有订单";
});

// 在 order 对象上挂 statusClass/statusLabel/url
function enrichOrders(raw) {
  return raw.map((o) => ({
    ...o,
    statusClass: statusMap[o.status]?.class || "",
    statusLabel: statusMap[o.status]?.label || "未知",
    goodImageUrl: `data:image/png;base64,${o.goodImage}`,
  }));
}

// 辅助函数
async function withLoading(fn) {
  loading.value = true;
  try {
    await fn();
  } finally {
    loading.value = false;
  }
}

function markNewItems(prev, list) {
  list.forEach((o) => {
    if (!prev.has(o.id)) newItems.value.add(o.id);
  });
  prev.clear();
  setTimeout(() => newItems.value.clear(), 4000);
}

function isNew(id) {
  return newItems.value.has(id);
}

// 业务逻辑
async function fetchOrders() {
  await withLoading(async () => {
    const userId = JSON.parse(localStorage.getItem("userInfo")).id;
    const res = await api.get("/order/list", { params: { userId } });
    if (res.data.code !== "200") return swalError("加载订单失败");
    const enriched = enrichOrders(res.data.data);
    markNewItems(displayed.value, enriched);
    allOrders.value = enriched;
  });
}

function countByStatus(status) {
  return status == null
    ? allOrders.value.length
    : allOrders.value.filter((o) => o.status === status).length;
}

function onFilter(status) {
  allOrders.value.forEach((o) => displayed.value.add(o.id));
  filterStatus.value = status;
  markNewItems(displayed.value, filteredOrders.value);
}

function navigateTo(path, query = {}) {
  router.push({ path, query });
}

async function onConfirmReceipt(orderId) {
  const { isConfirmed } = await confirmDialog(
    "确认收货？",
    "请确认您已收到商品"
  );
  if (!isConfirmed) return;
  await withLoading(async () => {
    const res = await api.post(`/order/confirmReceipt/${orderId}`);
    if (res.data.code !== "200") return swalError("确认收货失败");
    const idx = allOrders.value.findIndex((o) => o.id === orderId);
    if (idx > -1) allOrders.value[idx].status = 4;
    swalSuccess("确认收货成功");
    navigateTo("/evaluate", { orderId, goodId: allOrders.value[idx].goodId });
  });
}

onMounted(fetchOrders);
</script>


<style scoped>
.order-container {
  height: 100vh;
  overflow: hidden;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}
.app-title {
  display: flex;
  align-items: center;
  gap: 15px;
}
.app-title i {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 32px;
}
.back-button {
  background: #fff;
  border-radius: 12px;
  padding: 12px 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.07);
  border: 1px solid #eaeef5;
  font-weight: 600;
  transition: transform 0.3s, box-shadow 0.3s;
}
.back-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(39, 102, 245, 0.2);
  color: #2766f5;
}

.order-filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 30px;
}
.filter-button {
  flex: 1;
  min-width: 120px;
  border-radius: 10px;
  padding: 15px 20px;
  font-weight: 600;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}
.filter-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 14px rgba(0, 0, 0, 0.1);
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
  border-radius: 50%;
  transition: width 0.5s ease, height 0.5s ease;
}
.filter-button:active::after {
  width: 500px;
  height: 500px;
}
.count-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 25px;
  height: 25px;
  line-height: 25px;
  background: #fff;
  color: #2766f5;
  border: 2px solid #2766f5;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
}

.orders-list-container {
  max-height: calc(100vh - 250px);
  overflow-y: auto;
  padding-right: 8px;
}
.loader-animation {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(39, 102, 245, 0.3);
  border-top: 3px solid #2766f5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.orders-list {
  position: relative;
}
.order-card {
  background: #fff;
  border-radius: 16px;
  margin-bottom: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
  border: 1px solid #edf2f9;
  transition: transform 0.3s, box-shadow 0.3s, opacity 0.3s;
}
.order-card.new-item {
  animation: fadeIn 0.5s ease forwards;
}
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
.order-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  background: #f8fafc;
  border-bottom: 1px solid #edf2f9;
}
.order-info {
  display: flex;
  gap: 20px;
}
.order-info-item {
  display: flex;
  flex-direction: column;
}
.info-label {
  font-size: 12px;
  color: #6c757d;
}
.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-top: 5px;
}
.order-status {
  padding: 6px 15px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 13px;
}

.order-content {
  padding: 25px;
}
.product-info {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 20px;
}
.product-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid #f1f5f9;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.product-details h3 {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 5px;
}
.product-meta {
  color: #6c757d;
  font-size: 14px;
}

.order-summary {
  display: flex;
  justify-content: flex-end; /* 水平右对齐 */
  gap: 15px; /* 各按钮间距 */
}

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
}
.empty-state h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #2c3e50;
}
.empty-state p {
  color: #6c757d;
  font-size: 16px;
  line-height: 1.6;
}
/* 订单状态不同类型的背景色 & 文字色保留 */
.status-Pending-payment,
.Pending-payment {
  background: linear-gradient(to right, #f6d365, #ef4444);
  color: #fff;
}
.status-pending-shipment,
.pending-shipment {
  background: linear-gradient(to right, #f6d365, #fda085);
  color: #fff;
}
.status-Shipped,
.Shipped {
  background: linear-gradient(to right, #5ee7df, #6a87d5);
  color: #fff;
}
.status-no_comment,
.no_comment {
  background: linear-gradient(to right, #87e3f4, #90ea99);
  color: #fff;
}
.status-completed,
.completed {
  background: linear-gradient(to right, #87e3f4, #90ea99);
  color: #fff;
}
.gopay-button {
  padding: 10px 25px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
  background: linear-gradient(to right, #59d04c, #39cc5b);
  border: none;
  color: white;
}
.takegoods-button {
  padding: 10px 25px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
  background: linear-gradient(to right, #b84cd0, #a539cc);
  border: none;
  color: white;
}
.gocomment-button {
  padding: 10px 25px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
  background: linear-gradient(to right, #4cc9d0, #39bbcc);
  border: none;
  color: white;
}
/* 响应式 */
@media (max-width: 768px) {
  .app-header,
  .order-header,
  .order-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  .product-info {
    flex-direction: column;
  }
}
</style>