<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="(v) => emit('update:visible', v)"
    title="确认订单"
    width="600px"
    :close-on-click-modal="false"
  >
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
        <el-button size="small" @click="openAddAddress">添加地址</el-button>
        <el-button size="small" @click="openManageAddresses"
          >管理地址</el-button
        >
      </div>
    </div>

    <el-divider />

    <!-- 下半部分：商品信息 -->
    <div class="good-section">
      <img :src="good.image" class="good-image" alt="商品图" />
      <div class="good-info">
        <h4>{{ good.name }}</h4>
        <p>单价：¥{{ good.price }}</p>
        <p>
          数量：
          <el-input-number
            v-model="quantity"
            :min="1"
            :max="100"
            @change="onQtyChange"
          />
        </p>
        <p class="total">总计：¥{{ totalAmount.toFixed(2) }}</p>
      </div>
    </div>
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submitOrder">提交订单</el-button>
    </template>
  </el-dialog>
  <el-dialog
    v-model="showAddDialog"
    title="添加收货地址"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="newAddr" ref="addrForm" :rules="rules" label-width="100px">
      <el-form-item label="收件人" prop="receiver">
        <el-input v-model="newAddr.receiver" placeholder="请输入收件人姓名" />
      </el-form-item>
      <el-form-item label="电话" prop="telephone">
        <el-input v-model="newAddr.telephone" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="省市区" prop="city">
        <el-input v-model="newAddr.city" placeholder="例如：江苏南京" />
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input
          type="textarea"
          v-model="newAddr.address"
          placeholder="请输入详细街道、门牌号等"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddDialog = false">取消</el-button>
      <el-button type="primary" @click="submitNewAddress">保存</el-button>
    </template>
  </el-dialog>
  <el-dialog
    v-if="showManageDialog"
    :model-value="showManageDialog"
    @update:model-value="(val) => (showManageDialog = val)"
    title="管理地址"
    width="600px"
    @opened="loadAddresses"
  >
    <el-table :data="addresses" style="width: 100%">
      <el-table-column prop="receiver" label="收件人" />
      <el-table-column prop="telephone" label="电话" />
      <el-table-column prop="city" label="省市区" />
      <el-table-column prop="address" label="详细地址" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="startEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAddress(row.id)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
  <el-dialog v-model="showEditDialog" title="编辑地址" width="500px">
    <el-form
      :model="editAddr"
      ref="editAddrForm"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="收件人" prop="receiver">
        <el-input v-model="editAddr.receiver" />
      </el-form-item>
      <el-form-item label="电话" prop="telephone">
        <el-input v-model="editAddr.telephone" />
      </el-form-item>
      <el-form-item label="省市区" prop="city">
        <el-input v-model="editAddr.city" />
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input v-model="editAddr.address" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showEditDialog = false">取消</el-button>
      <el-button type="primary" @click="submitEditAddress">保存</el-button>
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
    required: true,
  },
});

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
const quantity = ref(1);

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
  const payload = {
    userId: JSON.parse(localStorage.getItem("userInfo"))?.id,
    goodId: props.good.id,
    addressId: selectedAddressId.value,
    quantity: quantity.value,
  };
  try {
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
      router.push("/order");
    } else {
      Swal.fire({
        icon: "error",
        title: res.data.msg || "提交失败",
        timer: 1000,
        showConfirmButton: false,
      });
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
    await api.delete(`/address/delete/${id}`);
    Swal.fire({
      icon: "success",
      title: "删除成功",
      timer: 1000,
      showConfirmButton: false,
    });
    loadAddresses();
  } catch {
    Swal.fire({
      icon: "error",
      title: "删除失败",
      timer: 1000,
      showConfirmButton: false,
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
.address-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.address-radio {
  display: block;
  margin-bottom: 12px;
}
.total {
  font-weight: bold;
  color: #f56c6c;
  margin-top: 8px;
}
.el-button + .el-button {
  margin-left: 0px;
}
.addr-detail {
  line-height: 1.4;
}
.address-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.no-address {
  flex: 1;
  color: #999;
}
.good-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}
.good-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border: 1px solid #eee;
}
.good-info h4 {
  margin: 0 0 8px;
}
</style>
