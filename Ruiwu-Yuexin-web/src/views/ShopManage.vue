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
            @click="() => openGoodDialog(good)"
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
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { useStore } from "vuex";
import { ElMessage } from "element-plus";

const router = useRouter();
const store = useStore();
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
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.button-box {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 30px;
}
.back-button {
  background: #736cf8;
  border: 1px solid #dcdfe6;
  color: #ebeef3;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s;
  margin-right: 10px;
}
.back-button:hover {
  background: #f0f0f0;
  color: #409eff;
}
.app-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  color: #333;
}

.app-title i {
  color: #5d9bff;
}

.add-button {
  padding: 10px 20px;
  font-weight: 500;
  background: linear-gradient(to right, #5d9bff, #409eff);
  border: none;
  box-shadow: 0 2px 6px rgba(93, 155, 255, 0.3);
  transition: all 0.3s ease;
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(93, 155, 255, 0.4);
}

/* 商品网格样式 */
.good-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 25px;
  margin-top: 20px;
}

.good-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.good-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 10px;
  text-align: right;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #e8f4ff;
  color: #409eff;
}

.status-badge.inactive {
  background: #fef0f0;
  color: #f56c6c;
}

.image-container {
  height: 180px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
}

.good-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.good-info {
  padding: 15px;
}

.good-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price {
  color: #e53935;
  font-weight: bold;
  font-size: 18px;
}

.currency {
  font-size: 14px;
}

.stock {
  color: #666;
  font-size: 14px;
  margin-top: 5px;
}

/* 操作对话框样式 */
.good-dialog {
  display: flex;
  gap: 20px;
}

.good-image-container {
  flex: 0 0 150px;
  height: 150px;
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

.good-form {
  flex: 1;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.empty-state i {
  font-size: 60px;
  margin-bottom: 20px;
  opacity: 0.4;
}

.empty-state h3 {
  font-size: 18px;
  margin-bottom: 10px;
}

.empty-state p {
  font-size: 14px;
}

/* 分页样式 */
.pagination {
  margin-top: 40px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.pagination button {
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination span {
  color: #606266;
}
</style>