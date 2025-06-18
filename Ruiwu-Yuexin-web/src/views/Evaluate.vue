<template>
  <div class="evaluate-wrapper">
    <div class="evaluate-container">
      <div class="app-header">
        <img
          src="@/assets/icon/评论.svg"
          alt="评论"
          width="45px"
          height="45px"
        />
        <h1>商品评价</h1>
        <el-button type="primary" plain class="back-button" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回
        </el-button>
      </div>

      <div class="evaluate-content">
        <!-- 商品信息 -->
        <div class="product-section" v-if="good">
          <div class="product-image">
            <img :src="good.image" alt="商品图片" />
          </div>
          <div class="product-info">
            <h3>{{ good.name }}</h3>
            <div class="price">¥{{ good.price }}</div>
          </div>
        </div>

        <!-- 评价表单 -->
        <div class="evaluate-form">
          <div class="stars">
            <h4>请评分：</h4>
            <div class="star-rating">
              <span v-for="star in 5" :key="star" @click="setRating(star)">
                <i
                  :class="[
                    'fas fa-star',
                    star <= rating ? 'active' : 'inactive',
                  ]"
                ></i>
              </span>
            </div>
          </div>

          <div class="comment">
            <h4>评价内容：</h4>
            <el-input
              type="textarea"
              :rows="4"
              placeholder="请输入您的评价"
              v-model="comment"
            ></el-input>
          </div>

          <div class="image-upload">
            <h4>上传图片（最多4张）：</h4>
            <el-upload
              action=""
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleImageChange"
              :on-remove="handleImageRemove"
              :file-list="fileList"
              :limit="4"
              :multiple="true"
            >
              <i class="el-icon-plus"></i>
              <template #tip>
                <div class="upload-tip">支持上传图片文件</div>
              </template>
            </el-upload>
          </div>
          <div class="button-group">
            <el-button type="primary" class="submit-button" @click="goBack">
              暂不评价
            </el-button>
            <el-button
              type="primary"
              class="submit-button"
              @click="submitEvaluate"
              :disabled="rating === 0"
            >
              提交评价
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import api from "@/plugins/axios";
import Swal from "sweetalert2";

const router = useRouter();
const route = useRoute();
const userInfo = JSON.parse(localStorage.getItem("userInfo"));

// 从路由参数中获取数据
const orderId = parseInt(route.query.orderId);
const goodId = parseInt(route.query.goodId);

const rating = ref(0);
const comment = ref("");
const fileList = ref([]);
const files = ref([]); // 存储文件对象
const good = ref(null);

// 获取商品信息
async function fetchGood() {
  try {
    const res = await api.get(`/good/${goodId}`);
    if (res.data.code === "200") {
      good.value = {
        name: res.data.data.goodName,
        price: res.data.data.price,
        image: `data:image/png;base64,${res.data.data.images[0]}`,
      };
    }
  } catch (error) {
    console.error("获取商品信息失败:", error);
  }
}

function setRating(star) {
  rating.value = star;
}

function handleImageChange(file, fileList) {
  files.value = fileList;
}

function handleImageRemove(file, fileList) {
  files.value = fileList;
}

async function submitEvaluate() {
  const star = Number(rating.value);
  if (isNaN(star) || star < 1 || star > 5) {
    return Swal.fire({
      icon: "warning",
      title: "无效的评分",
      text: "请点击星星进行评分",
      timer: 1500,
      showConfirmButton: false,
    });
  }

  try {
    // 准备表单数据
    const formData = new FormData();
    formData.append("userId", userInfo.id);
    formData.append("orderId", orderId);
    formData.append("goodId", goodId);
    formData.append("starLevel", rating.value);
    formData.append("comment", comment.value);

    // 添加图片文件
    files.value.forEach((file) => {
      formData.append("images", file.raw);
    });

    // 提交评价
    const res = await api.post("/evaluate/submit", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    if (res.data.code === "200") {
      const statusRes = await api.post(`/order/evaluate/${orderId}`);
      if (statusRes.data.code === "200") {
        Swal.fire({
          icon: "success",
          title: "评价提交成功",
          timer: 1500,
          showConfirmButton: false,
        }).then(() => {
          router.push({ path: "/order" });
        });
      } else {
        Swal.fire({
          icon: "error",
          title: "评价提交失败",
          text: res.data.msg || "未知错误",
          timer: 2000,
        });
      }
    }
  } catch (error) {
    console.error("提交评价失败:", error);
    Swal.fire({
      icon: "error",
      title: "系统错误，请稍后再试",
      timer: 1500,
      showConfirmButton: false,
    });
  }
}

function goBack() {
  router.back();
}

onMounted(() => {
  fetchGood();
});
</script>

<style scoped>
/* 新增这个外层容器样式 */
.evaluate-wrapper {
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: flex-start; /* 如果你想让它垂直居中，用 center */
  padding: 20px 0;
}

.evaluate-container {
  max-height: 95vh;
  overflow-y: auto;
  box-sizing: border-box;
  min-width: 700px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.app-header h1 {
  flex: 1;
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}
.button-group {
  display: flex;
  gap: 30px;
  justify-content: flex-end;
}
.app-header i {
  margin-right: 10px;
  color: #3498db;
  font-size: 28px;
}

.back-button {
  background: #fff;
  border-radius: 12px;
  padding: 10px 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border: 1px solid #eaeef5;
  transition: all 0.3s ease;
  font-weight: 600;
}

.back-button:hover {
  box-shadow: 0 8px 25px rgba(52, 152, 219, 0.2);
  transform: translateY(-2px);
  color: #3498db;
}

.evaluate-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.product-section {
  display: flex;
  gap: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.product-image img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-info h3 {
  margin-top: 0;
  font-size: 18px;
}

.price {
  font-weight: bold;
  color: #e74c3c;
  font-size: 16px;
}

.evaluate-form {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.stars,
.comment,
.image-upload {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
}

.star-rating {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.star-rating i {
  font-size: 32px;
  cursor: pointer;
  transition: color 0.2s;
}

.star-rating .active {
  color: #f8d568;
}

.star-rating .inactive {
  color: #e0e0e0;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

.submit-button {
  align-self: flex-end;
  padding: 12px 40px;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.submit-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(39, 102, 245, 0.4);
}
</style>