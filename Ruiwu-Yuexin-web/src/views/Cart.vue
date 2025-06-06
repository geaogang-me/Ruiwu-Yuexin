<template>
  <div class="ack-button-wrapper">
    <div class="back-button-wrapper">
      <el-button type="primary" @click="goBack" plain>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    <el-table
      v-loading="loading"
      :data="cartItems"
      border
      style="width: 100%; margin-top: 20px"
      @selection-change="handleSelectionChange"
    >
      <!-- 多选框 -->
      <el-table-column type="selection" width="55" />

      <el-table-column prop="goodName" label="商品名" />
      <el-table-column label="图片">
        <template #default="scope">
          <img
            :src="'data:image/jpeg;base64,' + scope.row.goodImage"
            alt="商品图"
            style="height: 80px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="num" label="数量" />
      <el-table-column label="总价">
        <template #default="scope">
          ¥{{ scope.row.price * scope.row.num }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="danger" size="small" @click="deleteItem(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 结算区域 -->
    <div class="checkout-bar">
      <span>已选 {{ selectedItems.length }} 项</span>
      <span>总价：¥{{ totalSelectedPrice }}</span>
      <el-button
        type="primary"
        size="medium"
        @click="checkout"
        :disabled="!selectedItems.length"
      >
        结算
      </el-button>
    </div>
    <el-dialog
      v-model="showQrDialog"
      title="扫码支付"
      width="300px"
      :close-on-click-modal="false"
      align-center
    >
      <div style="text-align: center">
        <img
          :src="qrCodeUrl"
          alt="扫码支付"
          style="width: 250px; height: 340px"
        />
        <p style="margin-top: 10px">请支付宝扫码支付</p>
      </div>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";
import { ArrowLeft } from "@element-plus/icons-vue";
import { useStore } from "vuex";

// 响应式数据
const cartItems = ref([]);
const selectedItems = ref([]);
const loading = ref(false);
const showQrDialog = ref(false);
const qrCodeUrl = require("@/assets/qrcode.png");

// 工具函数与方法
const router = useRouter();
const store = useStore();

const totalSelectedPrice = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + item.price * item.num, 0)
);

const fetchCart = async () => {
  const userId = store.state.userId;
  if (!userId) return;

  loading.value = true;
  try {
    const res = await api.get(`/list/${userId}`);
    if (res.data.code === "200") {
      cartItems.value = res.data.data.reverse();
      store.commit("setCartCount", cartItems.value.length);
    } else {
      ElMessage.error("获取购物车失败：" + res.data.msg);
    }
  } catch (err) {
    console.error("获取购物车失败：", err);
    ElMessage.error("网络错误，无法获取购物车");
  } finally {
    loading.value = false;
  }
};

const deleteItem = async (item) => {
  try {
    const res = await api.delete(`/cart/delete`, {
      data: {
        userId: store.state.userId,
        goodId: item.goodId,
      },
    });

    if (res.data.code === "200") {
      ElMessage.success("删除成功");
      fetchCart();
    } else {
      ElMessage.error("删除失败：" + res.data.msg);
    }
  } catch (err) {
    console.error("删除失败：", err);
    ElMessage.error("网络错误，删除失败");
  }
};

const handleSelectionChange = (val) => {
  selectedItems.value = val;
};

const checkout = () => {
  const goods = selectedItems.value.map((item) => ({
    goodId: item.goodId,
    num: item.num,
  }));
  console.log("结算商品：", goods);
  ElMessage.success(
    `共 ${goods.length} 件商品，总价 ¥${totalSelectedPrice.value}`
  );
  showQrDialog.value = true;
};

const goBack = () => {
  router.back();
};

onMounted(fetchCart);
</script>



<style scoped>
.ack-button-wrapper {
  padding: 25px;
  padding-top: 20px;
}
.cart-container {
  padding: 20px;
}
.cart-container {
  padding: 20px;
}
.back-button-wrapper {
  text-align: left; /* 确保按钮靠左 */
}
.checkout-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 10px 15px;
  background-color: #f5f5f5;
  border-top: 1px solid #ddd;
  font-size: 16px;
}
</style>
