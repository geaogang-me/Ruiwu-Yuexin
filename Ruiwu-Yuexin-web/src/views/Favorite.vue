<template>
  <div class="favorite-page">
    <div class="back-button-wrapper">
      <el-button type="primary" @click="goBack" plain>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    <h2 class="title">我的收藏</h2>
    <el-row :gutter="20">
      <el-col
        v-for="item in favorites"
        :key="item.id"
        :span="6"
        class="favorite-item"
      >
        <el-card shadow="hover" class="card" @click="toDetail(item.id)">
          <img
            :src="'data:image/jpeg;base64,' + item.goodImage"
            alt="商品图片"
            class="favorite-image"
          />
          <div class="info">
            <div class="name">{{ item.goodName }}</div>
            <div class="price">￥{{ item.price.toFixed(2) }}</div>
            <el-button
              type="danger"
              size="small"
              @click="removeFavorite(item.id)"
            >
              取消收藏
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { ElMessage, ElMessageBox } from "element-plus";

const userId = JSON.parse(localStorage.getItem("userInfo"))?.id || null;
// 从登录信息中获取用户 ID，示例写死
const router = useRouter();
const favorites = ref([]);
const goBack = () => {
  router.back();
};
const fetchFavorites = async () => {
  try {
    const res = await api.get("/favorite/list", {
      params: { userId },
    });
    if (res.data.code === "200") {
      favorites.value = res.data.data;
    } else {
      ElMessage.error(res.data.msg || "加载收藏失败");
    }
  } catch (err) {
    ElMessage.error("请求失败");
  }
};
const toDetail = (goodId) => {
  router.push({ path: "/detail", query: { goodId } });
};
const removeFavorite = async (goodId) => {
  try {
    // await ElMessageBox.confirm("确定要取消收藏该商品吗？", "提示", {
    //   type: "warning",
    // });
    const res = await api.delete("/favorite/remove", {
      params: { userId, goodId },
    });
    if (res.data.code === "200") {
      ElMessage.success("取消收藏成功");
      fetchFavorites();
    } else {
      ElMessage.error(res.data.msg || "取消失败");
    }
  } catch (err) {
    // 用户取消弹窗时不提示错误
    if (err !== "cancel") {
      ElMessage.error("请求失败");
    }
  }
};
onMounted(fetchFavorites);
</script>

<style scoped>
.favorite-page {
  padding: 20px;
}
.back-button-wrapper {
  text-align: left; /* 确保按钮靠左 */
}
.title {
  font-size: 22px;
  margin-bottom: 20px;
}
.favorite-item {
  margin-bottom: 20px;
}
.favorite-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  margin-bottom: 10px;
}
.info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.name {
  font-weight: bold;
  font-size: 16px;
}
.price {
  color: #f56c6c;
}
</style>
