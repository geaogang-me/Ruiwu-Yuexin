<template>
  <div class="order-container">
    <div class="back-button-wrapper">
      <el-button type="primary" @click="goBack" plain>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="order-filter-buttons">
      <el-button
        v-for="status in orderStatuses"
        :key="status.name"
        :type="filterStatus === status.name ? 'primary' : 'default'"
        @click="filterStatus = status.name"
      >
        {{ status.label }}
      </el-button>
    </div>

    <div v-for="order in filteredOrders" :key="order.id" class="order-card">
      <div class="order-header">
        <span>订单号: {{ order.id }}</span>
        <span>创建时间: {{ order.created }}</span>
        <span>订单状态: {{ getOrderStatus(order.status) }}</span>
      </div>

      <el-table :data="[order]" border style="width: 100%">
        <el-table-column label="商品" width="100">
          <template #default="{ row }">
            <img
              :src="`data:image/png;base64,${row.goodImage}`"
              class="order-product-image"
            />
          </template>
        </el-table-column>
        <el-table-column prop="goodName" label="商品名称" />
        <el-table-column prop="num" label="数量" width="80" />
        <el-table-column prop="unitPrice" label="单价" width="100" />
        <el-table-column prop="price" label="总计" width="100" />
      </el-table>
      <div v-if="order.status === 2" class="action-buttons">
        <el-button type="success" @click="confirmReceipt(order.id)"
          >确认收货</el-button
        >
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";
import { ArrowLeft } from "@element-plus/icons-vue";

const allOrders = ref([]);
const filterStatus = ref(null); // 当前筛选的状态

// 订单状态定义
const orderStatuses = [
  { name: null, label: "所有订单" },
  { name: 2, label: "已发货" },
  { name: 1, label: "待发货" },
  { name: 4, label: "已完成" },
];

const filteredOrders = computed(() => {
  if (filterStatus.value === null) {
    return allOrders.value;
  }
  return allOrders.value.filter((order) => order.status === filterStatus.value);
});

const fetchOrders = async () => {
  try {
    const userId = JSON.parse(localStorage.getItem("userInfo")).id;
    const res = await api.get("/order/list", {
      params: { userId },
    });
    if (res.data.code === "200") {
      allOrders.value = res.data.data;
    } else {
      ElMessage.error("加载订单失败");
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("获取订单出错");
  }
};

const goBack = () => {
  router.back();
};

const getOrderStatus = (status) => {
  switch (status) {
    case 2:
      return "已发货";
    case 1:
      return "待发货";
    case 2:
      return "待收货";
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
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error("确认收货失败");
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("确认收货出错");
  }
};

const router = useRouter();

onMounted(fetchOrders);
</script>

<style scoped>
.order-container {
  padding: 20px;
}

.back-button-wrapper {
  margin-bottom: 20px;
  text-align: left;
}

.order-filter-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.order-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.order-product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
}

.el-table {
  width: 100%;
}
</style>