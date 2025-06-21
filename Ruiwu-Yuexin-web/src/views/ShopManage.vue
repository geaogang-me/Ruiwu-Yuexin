<!-- ShopManage.vue -->
<template>
  <div class="shop-manage-container">
    <!-- 顶部标题栏 -->
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-store"></i>
        <h3>商店管理</h3>
      </div>
      <div class="button-box">
        <el-button
          type="danger"
          :disabled="!selectedIds.length"
          @click="deleteBatch"
          class="custom-button"
        >
          <i class="fas fa-trash"></i> 批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-button type="primary" class="add-button" @click="goToAddGood">
          <i class="fas fa-plus"></i> 新增商品
        </el-button>
        <el-button plain class="back-button" @click="navigateTo('/home')">
          <i class="fas fa-arrow-left"></i> 返回
        </el-button>
      </div>
    </div>

    <!-- 商品网格 -->
    <div class="content-area">
      <div class="loader-animation" v-if="loading">
        <div class="spinner"></div>
      </div>

      <div v-show="!loading">
        <div v-if="goodList.length" class="good-grid">
          <div
            v-for="good in goodList"
            :key="good.id"
            class="good-card"
            :style="{ '--delay': Math.random() * 0.3 + 's' }"
          >
            <el-checkbox
              class="select-checkbox"
              v-model="selectedIds"
              :label="good.id"
              @click.stop
            />
            <div class="card-header">
              <span
                :class="['status-badge', good.status ? 'active' : 'inactive']"
              >
                {{ good.status ? "上架中" : "已下架" }}
              </span>
            </div>
            <div class="image-container">
              <img :src="imageUrl(good.goodImage)" class="good-image" />
            </div>
            <div class="good-info">
              <div class="good-name">{{ good.goodName }}</div>
              <div class="price">
                <span class="currency">¥</span>
                <span class="amount">{{ good.price }}</span>
              </div>
              <div class="stock">库存: {{ good.stock }}</div>
            </div>
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click.stop="openGoodDialog(good)"
                class="edit-button"
              >
                编辑
              </el-button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <i class="fas fa-box-open"></i>
          <h3>暂无商品</h3>
          <p>请点击"新增商品"按钮开始添加</p>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <button @click="prev" :disabled="page === 1">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button @click="next" :disabled="page === totalPages">下一页</button>
    </div>

    <!-- 商品操作对话框 -->
    <el-dialog v-model="dialogVisible" title="商品管理" width="500px">
      <div class="good-dialog">
        <div class="good-image-container">
          <img :src="imageUrl(currentGood.goodImage)" class="dialog-image" />
        </div>

        <div class="good-form">
          <el-form :model="currentGood" label-width="80px">
            <el-form-item label="商品名称">
              <el-input
                type="textarea"
                v-model="currentGood.goodName"
                :autosize="{ minRows: 3, maxRows: 3 }"
              />
            </el-form-item>

            <el-form-item label="价格">
              <el-input-number
                v-model="currentGood.price"
                :min="0.01"
                :precision="2"
                :step="0.1"
                controls-position="right"
              />
            </el-form-item>

            <el-form-item label="库存">
              <el-input-number
                v-model="currentGood.stock"
                :min="0"
                :step="1"
                controls-position="right"
              />
            </el-form-item>

            <el-form-item label="状态">
              <el-switch
                v-model="currentGood.status"
                active-text="上架"
                inactive-text="下架"
                :active-value="1"
                :inactive-value="0"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateGood">保存更改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";

const router = useRouter();
const userInfo = computed(() => {
  const u = localStorage.getItem("userInfo");
  return u ? JSON.parse(u) : null;
});

// 分页数据
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const goodList = ref([]);
const loading = ref(false);
const totalPages = computed(() => Math.ceil(total.value / pageSize.value));
const selectedIds = ref([]);
// 当前操作商品
const dialogVisible = ref(false);
const currentGood = ref({
  id: null,
  goodName: "",
  price: 0,
  stock: 0,
  status: 0,
  goodImage: "",
});
function navigateTo(path) {
  router.push("/home");
}

watch(page, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    fetchShopGoods();
  }
});
// 获取当前商家的商品
const fetchShopGoods = async () => {
  if (!userInfo.value?.shopId) {
    ElMessage.error("未找到店铺信息");
    return;
  }

  loading.value = true;
  try {
    const response = await api.get("/shop/goods", {
      params: {
        shopId: userInfo.value.shopId,
        page: page.value,
        size: pageSize.value,
      },
    });

    if (response.data?.code === "200" && response.data.data) {
      const payload = response.data.data;
      goodList.value = payload.list?.length
        ? payload.list
        : payload.records?.length
        ? payload.records
        : [];
      total.value = payload.total ?? payload.count ?? 0;
    } else {
      ElMessage.error(response.data?.msg || "获取商品失败");
    }
  } catch (error) {
    console.error("获取店铺商品失败:", error);
    ElMessage.error("获取商品数据失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 打开商品操作对话框
const openGoodDialog = (good) => {
  currentGood.value = { ...good };
  dialogVisible.value = true;
};

// 更新商品信息
const updateGood = async () => {
  try {
    const response = await api.put("/shop/good/update", currentGood.value);

    if (response.data?.code === "200") {
      ElMessage.success("商品信息更新成功");
      // 更新本地列表数据
      const index = goodList.value.findIndex(
        (g) => g.id === currentGood.value.id
      );
      if (index !== -1) {
        goodList.value[index] = { ...currentGood.value };
      }
      dialogVisible.value = false;
    } else {
      ElMessage.error(response.data?.msg || "更新失败");
    }
  } catch (error) {
    console.error("更新商品失败:", error);
    ElMessage.error("更新商品信息失败");
  }
};
// 单个删除
async function deleteSingle(id) {
  try {
    await api.delete(`/shop/good/delete/${id}`, {
      params: { shopId: userInfo.value.shopId },
    });
    ElMessage.success("删除成功");

    // 判断是否需要回到上一页
    if (goodList.value.length === 1 && page.value > 1) {
      page.value--;
    }
    fetchShopGoods();
    // 在本地移除
    goodList.value = goodList.value.filter((g) => g.id !== id);
    // 如果批量模式下也要移除
    selectedIds.value = selectedIds.value.filter((x) => x !== id);
    total.value--;
  } catch (e) {
    ElMessage.error("删除失败");
  }
}

// 批量删除
async function deleteBatch() {
  if (!selectedIds.value.length) return;
  try {
    await api.post(`/shop/good/deleteBatch`, selectedIds.value, {
      params: { shopId: userInfo.value.shopId },
    });
    ElMessage.success("批量删除成功");
    const deletedCount = selectedIds.value.length;
    if (goodList.value.length === deletedCount && page.value > 1) {
      page.value--;
    }
    selectedIds.value = [];
    fetchShopGoods();
    // 在本地移除
    goodList.value = goodList.value.filter(
      (g) => !selectedIds.value.includes(g.id)
    );
    total.value -= selectedIds.value.length;
    selectedIds.value = [];
  } catch (e) {
    ElMessage.error("批量删除失败");
  }
}
// 分页控制
const prev = () => {
  if (page.value > 1) {
    page.value--;
    fetchShopGoods();
  }
};

const next = () => {
  if (page.value < totalPages.value) {
    page.value++;
    fetchShopGoods();
  }
};

const imageUrl = (b64) => {
  return `data:image/png;base64,${b64}`;
};
const goToAddGood = () => {
  router.push("/addGood");
};

onMounted(fetchShopGoods);
</script>

<style scoped>
.shop-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 10px 0;
  margin-bottom: 20px;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.app-title {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 22px;
  color: #2c3e50;
}

.app-title i {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 28px;
}

.button-box {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
}

.add-button {
  padding: 12px 24px;
  font-weight: 500;
  background: linear-gradient(to right, #5d9bff, #409eff);
  border: none;
  box-shadow: 0 4px 12px rgba(93, 155, 255, 0.3);
  transition: all 0.3s ease;
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(93, 155, 255, 0.4);
  background: linear-gradient(to right, #4d8bfa, #2e8bff);
}

.back-button {
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid #dcdfe6;
}

.back-button:hover {
  background: #f5f7fa;
  color: #409eff;
  border-color: #c0c4cc;
}

.custom-button {
  padding: 12px 24px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  transition: all 0.3s ease;
}

.custom-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
}

/* 商品网格样式 */
.content-area {
  position: relative;
  min-height: 500px;
}

.good-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 25px;
  margin-top: 10px;
}

.good-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
  padding: 1px;
  transition: all 0.3s ease;
  cursor: pointer;
  opacity: 0;
  transform: translateY(20px);
  animation: cardAppear 0.5s ease forwards;
  animation-delay: var(--delay);
  display: flex;
  flex-direction: column;
  height: 100%;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.good-card:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.select-checkbox {
  height: 18px;
  top: 1px;
  left: auto;
}

.card-header {
  position: absolute;
  top: 240px;
  right: 10px;
  z-index: 10;
  text-align: right;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.status-badge.active {
  background: rgba(64, 158, 255, 0.15);
  color: #409eff;
}

.status-badge.inactive {
  background: rgba(245, 108, 108, 0.15);
  color: #f56c6c;
}

.image-container {
  height: 220px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
  background: #f8fafc;
}

.good-image {
  max-width: 100%;
  max-height: 100%;
  border-radius: 12px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.good-card:hover .good-image {
  transform: scale(1.05);
}

.good-info {
  padding: 16px;
  flex: 1;
}

.good-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 44px;
}

.price {
  color: #ff5000;
  font-weight: 700;
  font-size: 20px;
  margin-bottom: 5px;
}

.currency {
  font-size: 16px;
}

.stock {
  color: #666;
  font-size: 14px;
}

.action-buttons {
  padding: 0 16px 16px;
  display: flex;
  justify-content: center;
}

.edit-button {
  width: 100%;
  background: linear-gradient(to right, #2766f5, #3a8dff);
  border: none;
  box-shadow: 0 2px 8px rgba(39, 102, 245, 0.3);
  color: white;
  transition: all 0.3s ease;
}

.edit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(39, 102, 245, 0.4);
  background: linear-gradient(to right, #1a5be9, #2979ff);
}

/* 空状态样式 */
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
  opacity: 0.6;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #2c3e50;
}

.empty-state p {
  max-width: 500px;
  margin: 0 auto;
  color: #6c757d;
  font-size: 16px;
  line-height: 1.6;
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
  border: 4px solid rgba(39, 102, 245, 0.1);
  border-radius: 50%;
  border-top: 4px solid #2766f5;
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

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 40px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
}

.pagination button {
  padding: 10px 25px;
  border: none;
  background: linear-gradient(to right, #2766f5, #3a8dff);
  color: #fff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(39, 102, 245, 0.25);
  font-weight: 500;
}

.pagination button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(39, 102, 245, 0.35);
}

.pagination button:disabled {
  background: #c5d8ff;
  cursor: not-allowed;
  opacity: 0.7;
}

.pagination span {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .good-grid {
    grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  }

  .app-title h3 {
    font-size: 20px;
  }

  .button-box {
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .button-box {
    width: 100%;
    justify-content: space-between;
  }

  .good-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .image-container {
    height: 180px;
  }
}

@media (max-width: 480px) {
  .good-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    flex-direction: column;
    gap: 10px;
  }
}

/* 对话框样式调整 */
.good-dialog {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.good-image-container {
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
</style>