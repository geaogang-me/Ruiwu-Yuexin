<template>
  <div class="order-container">
    <div class="back-button-wrapper">
      <el-button type="primary" @click="goBack" plain>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    <el-tabs v-model="activeTab" class="order-tabs">
      <el-tab-pane label="所有订单" name="all-orders">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span>订单号: {{ order.id }}</span>
            <span>创建时间: {{ order.created }}</span>
            <span>订单状态: {{ getOrderStatus(order.status) }}</span>
          </div>

          <!-- 平铺的单条记录就当“行”渲染 -->
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
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";
import { ArrowLeft } from "@element-plus/icons-vue";

const activeTab = ref("all-orders");
const orders = ref([]);
const router = useRouter();
const fetchOrders = async () => {
  try {
    const userId = JSON.parse(localStorage.getItem("userInfo")).id;
    const res = await api.get("/order/list", {
      params: { userId },
    });
    if (res.data.code === "200") {
      orders.value = res.data.data;
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
    case 1:
      return "待发货";
    case 2:
      return "已发货";
    case 3:
      return "待收货";
    case 4:
      return "已完成";
    default:
      return "未知";
  }
};

onMounted(fetchOrders);
</script>

<style scoped>
.order-container {
  padding: 20px;
}
.order-card {
  margin-bottom: 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 12px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.order-product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
}
</style>

<style scoped>
.order-container {
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.order-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 20px;
}

.order-product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
}

.el-table {
  margin-top: 10px;
  width: 100%;
}
.back-button-wrapper {
  text-align: left; /* 确保按钮靠左 */
}
.el-tabs {
  margin-bottom: 20px;
}
.el-table-column {
  padding: 5px;
}
</style>
