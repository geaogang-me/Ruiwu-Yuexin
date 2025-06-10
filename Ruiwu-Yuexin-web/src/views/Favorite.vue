<template>
  <div class="favorite-container">
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-heart"></i>
        <h1>我的收藏</h1>
      </div>
      <el-button type="primary" plain class="back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i> 返回
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div class="loader-animation" v-if="loading">
      <div class="spinner"></div>
    </div>

    <!-- 收藏商品列表 -->
    <div v-if="!loading && favorites.length > 0" class="favorites-grid">
      <div
        v-for="item in favorites"
        :key="item.id"
        class="favorite-card"
        @click="toDetail(item.id)"
      >
        <el-button
          type="danger"
          class="remove-button"
          @click.stop="removeFavorite(item.id)"
        >
          <i class="fas fa-times"></i>
        </el-button>

        <img
          :src="'data:image/jpeg;base64,' + item.goodImage"
          alt="商品图片"
          class="favorite-image"
        />

        <div class="card-content">
          <div class="item-name">{{ item.goodName }}</div>
          <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
        </div>
      </div>
    </div>

    <!-- 空状态提示 -->
    <div v-if="!loading && favorites.length === 0" class="empty-favorites">
      <i class="fas fa-heart-broken"></i>
      <h3>您还没有收藏的商品</h3>
      <p>去发现心仪的商品并收藏吧</p>
      <el-button type="primary" class="explore-button" @click="goToGoodsList">
        浏览商品
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import Swal from "sweetalert2";
import { ElMessage } from "element-plus";

const userId = JSON.parse(localStorage.getItem("userInfo"))?.id || null;
const router = useRouter();
const favorites = ref([]);
const loading = ref(false);

const fetchFavorites = async () => {
  loading.value = true;
  try {
    const res = await api.get("/favorite/list", {
      params: { userId },
    });
    if (res.data.code === "200") {
      favorites.value = res.data.data;

      // 设置卡片入场动画
      setTimeout(() => {
        document.querySelectorAll(".favorite-card").forEach((card) => {
          card.style.opacity = 1;
          card.style.transform = "translateY(0)";
        });
      }, 50);
    } else {
      ElMessage.error("加载收藏失败");
    }
  } catch (err) {
    ElMessage.error("请求失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

const toDetail = (goodId) => {
  router.push({ path: "/detail", query: { goodId } });
};

const removeFavorite = async (goodId) => {
  try {
    const res = await api.delete("/favorite/remove", {
      params: { userId, goodId },
    });
    if (res.data.code === "200") {
      ElMessage.success("已取消收藏");

      // 添加删除动画
      const index = favorites.value.findIndex((item) => item.id === goodId);
      if (index !== -1) {
        const card = document.querySelectorAll(".favorite-card")[index];
        card.style.transform = "scale(0.9)";
        card.style.opacity = 0;

        setTimeout(() => {
          favorites.value.splice(index, 1);
        }, 300);
      }
    } else {
      ElMessage.error(res.data.msg || "取消收藏失败");
    }
  } catch (err) {
    ElMessage.error("请求失败");
  }
};

const goBack = () => {
  router.back();
};

const goToGoodsList = () => {
  router.push({ path: "/home" });
};

onMounted(fetchFavorites);
</script>

<style scoped>
.favorite-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
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
  background: linear-gradient(135deg, #ff4d94 0%, #ff7a00 100%);
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
  box-shadow: 0 8px 25px rgba(255, 77, 148, 0.2);
  transform: translateY(-2px);
  color: #ff4d94;
}

/* 收藏商品网格布局 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 25px;
  margin-top: 10px;
}

.favorite-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);

  /* 入场动画 */
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.4s ease, transform 0.4s ease, box-shadow 0.3s;
}

.favorite-card:hover {
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.15);
  transform: translateY(-5px) scale(1.02);
}

.remove-button {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 77, 148, 0.15);
  color: #ff4d94;
  border: none;
  transition: all 0.3s ease;
}

.remove-button:hover {
  background: #ff4d94;
  color: white;
  transform: scale(1.1);
}

.favorite-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-bottom: 1px solid #f1f5f9;
  transition: transform 0.5s ease;
}

.favorite-card:hover .favorite-image {
  transform: scale(1.05);
}

.card-content {
  padding: 20px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  transition: color 0.3s;
}

.favorite-card:hover .item-name {
  color: #ff4d94;
}

.item-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d94;
}

/* 空状态提示 */
.empty-favorites {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  margin-top: 30px;
  animation: fadeIn 0.8s ease;
}

.empty-favorites i {
  font-size: 80px;
  color: #ffccdd;
  margin-bottom: 25px;
}

.empty-favorites h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #2c3e50;
}

.empty-favorites p {
  max-width: 500px;
  margin: 0 auto 30px;
  color: #6c757d;
  font-size: 16px;
  line-height: 1.6;
}

.explore-button {
  padding: 12px 40px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(to right, #ff4d94, #ff7a00);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 77, 148, 0.25);
  transition: all 0.3s ease;
}

.explore-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 20px rgba(255, 77, 148, 0.35);
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
  border: 3px solid rgba(255, 77, 148, 0.3);
  border-radius: 50%;
  border-top: 3px solid #ff4d94;
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

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 15px;
  }

  .favorite-image {
    height: 150px;
  }
}

@media (max-width: 480px) {
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }

  .favorite-image {
    height: 120px;
  }

  .card-content {
    padding: 15px;
  }
}
</style>