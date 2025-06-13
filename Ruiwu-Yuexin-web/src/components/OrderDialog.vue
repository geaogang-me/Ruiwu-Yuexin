<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="(v) => emit('update:visible', v)"
    :close-on-click-modal="false"
    class="order-dialog"
  >
    <!-- 对话框头部 - 匹配主界面风格 -->
    <div class="dialog-header">
      <i class="fas fa-file-invoice-dollar"></i>
      <h3>确认订单</h3>
    </div>

    <!-- 上半部分：地址选择 & 管理 -->
    <div class="address-section">
      <el-select
        v-if="addresses.length"
        v-model="selectedAddressId"
        placeholder="请选择收货地址"
        class="address-select"
      >
        <el-option
          v-for="addr in addresses"
          :key="addr.id"
          :label="`${addr.receiver} ${addr.telephone} — ${addr.fullAddress}`"
          :value="addr.id"
        />
      </el-select>
      <div v-else class="no-address">暂无收货地址，请先添加</div>
      <div class="address-buttons">
        <el-button
          size="small"
          type="primary"
          plain
          class="address-button"
          @click="openAddAddress"
        >
          <i class="fas fa-plus"></i> 添加地址
        </el-button>
        <el-button
          size="small"
          class="address-button"
          @click="openManageAddresses"
        >
          <i class="fas fa-cog"></i> 管理地址
        </el-button>
      </div>
    </div>

    <el-divider class="custom-divider" />

    <!-- 下半部分：商品信息 - 使用卡片样式 -->
    <div class="good-section" v-if="displayGoods">
      <!-- 多商品模式 -->
      <div v-if="multipleGoods" class="multiple-goods-container">
        <div class="cart-items-list">
          <div
            v-for="(item, index) in cartItems"
            :key="index"
            class="cart-order-item"
          >
            <!-- 商品图片 -->
            <div class="image-wrapper">
              <img
                v-if="item.goodImage"
                :src="'data:image/jpeg;base64,' + item.goodImage"
                class="cart-order-item-image"
                alt="商品图"
              />
              <div v-else class="image-placeholder">
                <i class="fas fa-image"></i>
              </div>
            </div>

            <!-- 商品信息 -->
            <div class="cart-order-item-info">
              <h4>{{ item.goodName }}</h4>
              <div class="cart-order-item-details">
                <div>
                  <span class="label">单价：</span>
                  <span class="value">¥{{ item.price }}</span>
                </div>
                <div>
                  <span class="label">数量：</span>
                  <span class="value">{{ item.num }}</span>
                </div>
              </div>
            </div>

            <!-- 小计 -->
            <div class="cart-order-item-total">
              ¥{{ (item.price * item.num).toFixed(2) }}
            </div>
          </div>
        </div>

        <!-- 总价 -->
        <div class="summary-card">
          <div class="total-amount">
            <span class="label">订单总额：</span>
            <span class="value">¥{{ cartTotalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 单商品模式 -->
      <div v-else>
        <!-- 安全处理单商品信息 -->
        <div class="good-card" v-if="good">
          <div class="image-wrapper">
            <img :src="good.image" class="good-image" alt="商品图" />
          </div>
          <div class="good-info">
            <h4>{{ good.name }}</h4>
            <div class="good-details">
              <div class="price-info">
                <span class="label">单价：</span>
                <span class="value">¥{{ good.price }}</span>
              </div>
              <div class="quantity-selector">
                <span class="label">数量：</span>
                <el-input-number
                  v-model="quantity"
                  :min="1"
                  :max="100"
                  @change="onQtyChange"
                  controls-position="right"
                  class="quantity-input"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 单商品模式总价 -->
        <div class="summary-card">
          <div class="total-amount">
            <span class="label">订单总额：</span>
            <span class="value"
              >¥{{ (good ? good.price * quantity : 0).toFixed(2) }}</span
            >
          </div>
        </div>
      </div>
    </div>

    <!-- 无商品提示 -->
    <div v-else class="no-goods-message">
      <i class="fas fa-exclamation-circle"></i>
      <p>无法获取商品信息</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="cancel-button" @click="emit('update:visible', false)">
          <i class="fas fa-times"></i> 取消
        </el-button>
        <el-button type="primary" class="submit-button" @click="submitOrder">
          <i class="fas fa-check"></i> 提交订单
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 添加地址对话框 -->
  <el-dialog
    v-model="showAddDialog"
    :close-on-click-modal="false"
    class="address-dialog"
  >
    <div class="dialog-header">
      <i class="fas fa-map-marker-alt"></i>
      <h3>添加收货地址</h3>
    </div>
    <el-form
      :model="newAddr"
      ref="addrForm"
      :rules="rules"
      label-width="100px"
      label-position="top"
    >
      <div class="form-row">
        <el-form-item label="收件人" prop="receiver">
          <el-input v-model="newAddr.receiver" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="telephone">
          <el-input v-model="newAddr.telephone" placeholder="请输入联系电话" />
        </el-form-item>
      </div>

      <el-form-item label="省市区" prop="city">
        <el-input
          v-model="newAddr.city"
          placeholder="例如：江苏省南京市玄武区"
        />
      </el-form-item>

      <el-form-item label="详细地址" prop="address">
        <el-input
          type="textarea"
          :rows="3"
          v-model="newAddr.address"
          placeholder="请输入详细街道、门牌号等"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="cancel-button" @click="showAddDialog = false">
          <i class="fas fa-times"></i> 取消
        </el-button>
        <el-button
          type="primary"
          class="submit-button"
          @click="submitNewAddress"
        >
          <i class="fas fa-save"></i> 保存地址
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 管理地址对话框 -->
  <el-dialog
    v-if="showManageDialog"
    :model-value="showManageDialog"
    @update:model-value="(val) => (showManageDialog = val)"
    @opened="loadAddresses"
    class="manage-address-dialog"
  >
    <div class="dialog-header">
      <i class="fas fa-address-book"></i>
      <h3>管理收货地址</h3>
    </div>

    <el-table :data="addresses" style="width: 100%" class="address-table">
      <el-table-column prop="receiver" label="收件人" width="120" />
      <el-table-column prop="telephone" label="电话" width="130" />
      <el-table-column prop="city" label="省市区" />
      <el-table-column prop="address" label="详细地址" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" class="action-button" @click="startEdit(row)">
            <i class="fas fa-edit"></i> 编辑
          </el-button>
          <el-button
            size="small"
            type="danger"
            class="action-button"
            @click="deleteAddress(row.id)"
          >
            <i class="fas fa-trash"></i> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="cancel-button" @click="showManageDialog = false">
          <i class="fas fa-times"></i> 关闭
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 编辑地址对话框 -->
  <el-dialog
    v-model="showEditDialog"
    title="编辑地址"
    width="700px"
    class="edit-address-dialog"
  >
    <div class="dialog-header">
      <i class="fas fa-edit"></i>
      <h3>编辑收货地址</h3>
    </div>
    <el-form
      :model="editAddr"
      ref="editAddrForm"
      :rules="rules"
      label-width="100px"
      label-position="top"
    >
      <div class="form-row">
        <el-form-item label="收件人" prop="receiver">
          <el-input v-model="editAddr.receiver" />
        </el-form-item>
        <el-form-item label="电话" prop="telephone">
          <el-input v-model="editAddr.telephone" />
        </el-form-item>
      </div>
      <el-form-item label="省市区" prop="city">
        <el-input v-model="editAddr.city" />
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input v-model="editAddr.address" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="cancel-button" @click="showEditDialog = false">
          <i class="fas fa-times"></i> 取消
        </el-button>
        <el-button
          type="primary"
          class="submit-button"
          @click="submitEditAddress"
        >
          <i class="fas fa-save"></i> 保存修改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from "vue";
import api from "@/plugins/axios";
import Swal from "sweetalert2";
import { nextTick } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();
const props = defineProps({
  visible: Boolean,
  good: {
    type: Object,
    required: false, // 改为非必需
    default: null,
  },
  cartItems: {
    // 新添加的多商品属性
    type: Array,
    default: () => [],
  },
});
const displayGoods = computed(() => {
  return props.cartItems.length > 0 || (props.good && props.good.id);
});

const multipleGoods = computed(() => {
  return props.cartItems.length > 0;
});

const cartTotalAmount = computed(() => {
  return props.cartItems.reduce((total, item) => {
    return total + item.price * item.num;
  }, 0);
});
const quantity = ref(1);

const showAddDialog = ref(false);
const addrForm = ref(null);
const newAddr = ref({
  receiver: "",
  telephone: "",
  city: "",
  address: "",
});
const rules = {
  receiver: [{ required: true, message: "请输入收件人", trigger: "blur" }],
  telephone: [
    { required: true, message: "请输入电话", trigger: "blur" },
    { pattern: /^[\d\- ]+$/, message: "电话格式不正确", trigger: "blur" },
  ],
  city: [{ required: true, message: "请输入省市区", trigger: "blur" }],
  address: [{ required: true, message: "请输入详细地址", trigger: "blur" }],
};

const emit = defineEmits(["update:visible", "order-submitted"]);
const showManageDialog = ref(false);
const showEditDialog = ref(false);
const editAddr = ref({});
const editAddrForm = ref(null);
const addresses = ref([]);
const selectedAddressId = ref(null);

async function loadAddresses() {
  try {
    const res = await api.get("/address/list", {
      params: { userId: JSON.parse(localStorage.getItem("userInfo"))?.id },
    });
    if (res.data.code === "200") {
      // 拼合 fullAddress，并保留姓名 & 电话
      addresses.value = res.data.data.map((addr) => ({
        ...addr,
        fullAddress: `${addr.city} ${addr.address}`,
      }));
      if (addresses.value.length) {
        selectedAddressId.value = addresses.value[0].id;
      }
    } else {
      Swal.fire({
        icon: "error",
        title: res.data.msg || "加载地址失败",
        timer: 1000,
        showConfirmButton: false,
      });
    }
  } catch {
    Swal.fire({
      icon: "error",
      title: "加载地址失败",
      timer: 1000,
      showConfirmButton: false,
    });
  }
}
// 当数量变化时，可以在这里做额外逻辑（例如校验库存）
function onQtyChange(val) {
  if (val < 1) quantity.value = 1;
}

// 计算总价
const totalAmount = computed(() => {
  return props.good.price * quantity.value;
});
function openAddAddress() {
  showAddDialog.value = true;
}
function openManageAddresses() {
  showManageDialog.value = true;
}
requestAnimationFrame(() => {
  // DOM 操作或 UI 调整
});
function startEdit(row) {
  editAddr.value = { ...row };
  showEditDialog.value = true;
}

async function submitOrder() {
  if (!selectedAddressId.value) {
    Swal.fire({
      icon: "warning",
      title: "请选择收货地址",
      timer: 1000,
      showConfirmButton: false,
    });
    return;
  }

  const userId = JSON.parse(localStorage.getItem("userInfo"))?.id;

  try {
    // 多个商品处理
    if (props.cartItems.length > 0) {
      const promises = props.cartItems.map((item) => {
        const payload = {
          userId,
          goodId: item.goodId,
          addressId: selectedAddressId.value,
          quantity: item.num,
        };
        return api.post("/order/create", payload);
      });

      const results = await Promise.all(promises);
      const allSuccess = results.every((res) => res.data.code === "200");

      if (allSuccess) {
        Swal.fire({
          icon: "success",
          title: "订单提交成功",
          timer: 1000,
          showConfirmButton: false,
        });
        emit("order-submitted");
        emit("update:visible", false);
        router.push("/payment");
      } else {
        const errorMessages = results
          .filter((res) => res.data.code !== "200")
          .map((res) => res.data.msg)
          .join(", ");

        Swal.fire({
          icon: "error",
          title: "部分订单提交失败",
          text: errorMessages || "未知错误",
          timer: 2000,
        });
      }
    }
    // 单个商品处理
    else if (props.good) {
      const payload = {
        userId,
        goodId: props.good.id,
        addressId: selectedAddressId.value,
        quantity: quantity.value,
      };

      const res = await api.post("/order/create", payload);
      if (res.data.code === "200") {
        Swal.fire({
          icon: "success",
          title: "订单提交成功",
          timer: 1000,
          showConfirmButton: false,
        });
        emit("order-submitted");
        emit("update:visible", false);
        router.push("/payment");
      } else {
        Swal.fire({
          icon: "error",
          title: res.data.msg || "提交失败",
          timer: 1000,
          showConfirmButton: false,
        });
      }
    }
  } catch {
    Swal.fire({
      icon: "error",
      title: "提交订单出错",
      timer: 1000,
      showConfirmButton: false,
    });
  }
}
async function submitNewAddress() {
  await addrForm.value.validate(async (valid) => {
    if (!valid) return;
    try {
      const userId = JSON.parse(localStorage.getItem("userInfo"))?.id;
      const payload = { userId, ...newAddr.value };
      const res = await api.post("/address/add", payload);
      if (res.data.code === "200") {
        Swal.fire({
          icon: "success",
          title: "地址已添加",
          timer: 1000,
          showConfirmButton: false,
        });
        showAddDialog.value = false;
        // 清空表单
        Object.assign(newAddr.value, {
          receiver: "",
          telephone: "",
          city: "",
          address: "",
        });
        // 重新加载列表
        await loadAddresses();
      } else {
        Swal.fire({
          icon: "error",
          title: res.data.msg || "添加失败",
          timer: 1000,
          showConfirmButton: false,
        });
      }
    } catch (e) {
      console.error(e);
      Swal.fire({
        icon: "error",
        title: "添加地址出错",
        timer: 1000,
        showConfirmButton: false,
      });
    }
  });
}
async function submitEditAddress() {
  await editAddrForm.value.validate(async (valid) => {
    if (!valid) return;
    try {
      const res = await api.put("/address/update", editAddr.value);
      if (res.data.code === "200") {
        Swal.fire({
          icon: "success",
          title: "修改成功",
          timer: 1000,
          showConfirmButton: false,
        });
        showEditDialog.value = false;
        loadAddresses();
      } else {
        Swal.fire({
          icon: "error",
          title: res.data.msg || "修改失败",
          timer: 1000,
          showConfirmButton: false,
        });
      }
    } catch {
      Swal.fire({
        icon: "error",
        title: "修改出错",
        timer: 1000,
        showConfirmButton: false,
      });
    }
  });
}

async function deleteAddress(id) {
  try {
    const result = await Swal.fire({
      title: "删除地址？",
      text: "删除地址后将无法恢复！",
      icon: "question",
      iconColor: "#ff6b6b",
      showCancelButton: true,
      confirmButtonColor: "#2575fc",
      cancelButtonColor: "#909399",
      confirmButtonText: "确定删除",
      cancelButtonText: "取消",
      reverseButtons: true,
      background: "#fff",
      customClass: {
        container: "swal-delete-container",
        popup: "swal-delete-popup",
        title: "swal-delete-title",
        htmlContainer: "swal-delete-content",
        confirmButton: "swal-delete-confirm",
        cancelButton: "swal-delete-cancel",
      },
      buttonsStyling: true,
    });

    if (result.isConfirmed) {
      await api.delete(`/address/delete/${id}`);
      Swal.fire({
        title: "删除成功",
        icon: "success",
        timer: 1000,
        showConfirmButton: false,
        background: "#fff",
      });
      loadAddresses();
    }
  } catch {
    Swal.fire({
      title: "删除失败",
      icon: "error",
      timer: 1000,
      showConfirmButton: false,
      background: "#fff",
    });
  }
}

watch(
  () => props.visible,
  (v) => {
    if (v) loadAddresses();
  }
);
</script>

<style scoped>
/* 对话框通用样式 */
.order-dialog,
.address-dialog,
.manage-address-dialog,
.edit-address-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.2);
}
.multiple-goods-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart-items-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
}

.cart-order-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.cart-order-item:last-child {
  border-bottom: none;
}

.image-wrapper {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.cart-order-item-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  background: #f1f1f1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
}

.image-placeholder i {
  font-size: 24px;
}

.cart-order-item-info {
  flex: 1;
}

.cart-order-item-info h4 {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 16px;
}

.cart-order-item-details {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.cart-order-item-total {
  font-weight: 700;
  color: #e74c3c;
  min-width: 80px;
  text-align: right;
}

/* 无商品提示 */
.no-goods-message {
  text-align: center;
  padding: 40px 0;
  color: #ff6b6b;
}

.no-goods-message i {
  font-size: 50px;
  margin-bottom: 15px;
}

.no-goods-message p {
  font-size: 18px;
  font-weight: 500;
}
.dialog-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 5px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dialog-header h3 {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.dialog-header i {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 25px;
}

.custom-divider {
  margin: 15px 0;
}

/* 地址选择区域 */
.address-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.address-select {
  width: 100%;
  margin-bottom: 20px;
}
.el-button--small {
  margin-left: 12px;
}
.no-address {
  color: #909399;
  padding: 10px 0;
  text-align: center;
  font-size: 15px;
}

.address-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.address-button {
  border-radius: 10px;
  padding: 10px 20px;
  box-shadow: 0 4px 6px rgba(50, 50, 93, 0.08), 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  font-weight: 500;
}

.address-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 14px rgba(50, 50, 93, 0.1), 0 3px 6px rgba(0, 0, 0, 0.08);
}

/* 商品信息区域 */
.good-section {
  margin-top: 20px;
}

.good-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.good-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.good-image {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  margin-right: 20px;
}

.good-info {
  flex: 1;
}

.good-info h4 {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 15px;
}

.good-details {
  display: flex;
  flex-wrap: wrap;
  gap: 25px;
}

.price-info,
.quantity-selector {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.value {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
}

.quantity-input {
  width: 120px;
}

.summary-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: flex-end;
}

.total-amount {
  text-align: right;
}

.total-amount .label {
  font-size: 16px;
}

.total-amount .value {
  font-size: 24px;
  color: #f56c6c;
}

/* 对话框底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding-top: 15px;
}

.cancel-button {
  border-radius: 10px;
  padding: 12px 25px;
  font-weight: 700;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.submit-button {
  border-radius: 10px;
  padding: 12px 30px;
  font-weight: 700;
  transition: all 0.3s ease;
  background: linear-gradient(to right, #2766f5, #3a8dff);
  box-shadow: 0 4px 15px rgba(39, 102, 245, 0.3);
  border: none;
  color: white;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 20px rgba(39, 102, 245, 0.4);
}

.cancel-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 15px rgba(0, 0, 0, 0.1);
}

/* 管理地址对话框 */
.manage-address-dialog {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.address-table {
  margin-top: 20px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.action-button {
  padding: 6px 12px;
  font-size: 13px;
}

/* 表单布局调整 */
.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.form-row .el-form-item {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-dialog,
  .address-dialog,
  .manage-address-dialog,
  .edit-address-dialog {
    width: 90% !important;
  }

  .good-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .good-image {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .good-details {
    flex-direction: column;
    gap: 15px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>