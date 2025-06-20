<template>
  <div class="payment-container">
    <div class="header">
      <h2>支付订单</h2>
      <el-button type="primary" plain class="back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i>
      </el-button>
    </div>

    <!-- 支付方式选择 -->
    <div v-if="step === 'select'" class="method-list">
      <p>请选择支付方式：</p>
      <button class="wechat-btn" @click="pay('wechat')">微信支付</button>
      <button class="alipay-btn" @click="pay('alipay')">支付宝支付</button>
    </div>

    <!-- 模拟支付进行中 -->
    <div v-if="step === 'processing'" class="processing">
      <p>正在跳转到{{ currentLabel }}...</p>
      <img :src="processingImage" class="processing-img" alt="加载中" />
    </div>

    <!-- 支付完成 -->
    <div v-if="step === 'done'" class="done">
      <p>支付成功！感谢您的购买。</p>
      <button @click="goBack">返回首页</button>
      <button @click="viewOrders">去订单看看</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import api from "@/plugins/axios";
import processingImg from "@/assets/qrcode.png";

const router = useRouter();
const route = useRoute();
const orderIds = ref([]);
const step = ref("select");
const currentLabel = ref("");
const processingImage = ref(processingImg);
onMounted(() => {
  // 兼容处理单订单和多订单
  if (route.query.orderIds) {
    // 多订单模式：逗号分隔的字符串
    orderIds.value = route.query.orderIds.split(",").map((id) => parseInt(id));
  } else if (route.query.orderId) {
    // 单订单模式
    orderIds.value = [parseInt(route.query.orderId)];
  }
});
async function pay(method) {
  if (orderIds.value.length === 0) {
    alert("缺少订单 ID，无法支付");
    return;
  }
  currentLabel.value = method === "wechat" ? "微信支付" : "支付宝支付";
  step.value = "processing";

  setTimeout(async () => {
    try {
      // 使用批处理接口更新所有订单状态
      await api.post("/order/batchUpdateStatus", null, {
        params: {
          orderIds: orderIds.value.join(","),
        },
      });
      step.value = "done";
    } catch (err) {
      console.error("支付状态更新失败", err);
      alert("支付失败，请稍后再试");
      step.value = "select";
    }
  }, 2000);
}
function goBack() {
  router.push({ path: "/" });
}

function viewOrders() {
  router.push({ path: "/order" });
}
</script>

<style scoped>
/* 样式保持不变 */
.payment-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #fff;
}
.header {
  position: relative;
  height: 40px;
  margin-bottom: 20px;
}
.header h2 {
  position: absolute;
  left: 50%;
  top: 0;
  transform: translateX(-50%);
  margin: 0;
  font-size: 20px;
  line-height: 40px;
}
.back-button {
  position: absolute;
  right: 0;
  top: 0;
  background: #fff;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.07);
  border: 1px solid #eaeef5;
  transition: all 0.3s ease;
}
.back-button:hover {
  box-shadow: 0 8px 25px rgba(39, 102, 245, 0.2);
  transform: translateY(-2px);
  color: #2766f5;
}
.method-list p {
  margin-bottom: 16px;
  font-size: 16px;
  text-align: center;
}
.method-list button {
  width: 150px;
  margin: 10px;
  padding: 10px 0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.wechat-btn {
  background-color: #07c160;
  color: white;
}
.alipay-btn {
  background-color: #00a0e9;
  color: white;
}
.processing {
  text-align: center;
}
.processing p {
  margin-bottom: 10px;
}
.processing-img {
  width: 250px;
  height: 350px;
}
.done p {
  font-size: 18px;
  margin-bottom: 20px;
  text-align: center;
}
.done button {
  margin: 0 10px;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background: #2766f5;
  color: #fff;
}
</style>
