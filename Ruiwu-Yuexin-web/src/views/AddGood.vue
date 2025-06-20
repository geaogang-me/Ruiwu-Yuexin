<template>
  <div class="add-good-container">
    <div class="app-header">
      <i class="fas fa-plus-circle"></i>
      <h1>新增商品</h1>
      <el-button plain class="back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i> 返回
      </el-button>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="good-form"
      enctype="multipart/form-data"
    >
      <!-- 商品名称 -->
      <el-form-item label="商品名称" prop="goodName">
        <el-input v-model="form.goodName" placeholder="请输入商品名称" />
      </el-form-item>

      <!-- 价格 -->
      <el-form-item label="价格(¥)" prop="price">
        <el-input-number
          v-model="form.price"
          :min="0.01"
          :step="0.1"
          controls-position="right"
        />
      </el-form-item>

      <!-- 库存 -->
      <el-form-item label="库存" prop="stock">
        <el-input-number
          v-model="form.stock"
          :min="0"
          :step="1"
          controls-position="right"
        />
      </el-form-item>

      <!-- 上架状态 -->
      <el-form-item label="上架状态" prop="status">
        <el-switch
          v-model="form.status"
          active-text="上架"
          inactive-text="下架"
          :active-value="1"
          :inactive-value="0"
        />
      </el-form-item>

      <!-- 描述 -->
      <el-form-item label="商品描述" prop="description">
        <el-input
          type="textarea"
          v-model="form.description"
          :rows="4"
          placeholder="请输入商品描述"
        />
      </el-form-item>

      <!-- 主图 -->
      <el-form-item label="* 主图" prop="mainImageFile">
        <el-upload
          class="upload-demo"
          :show-file-list="false"
          :before-upload="beforeMainUpload"
        >
          <el-button size="small" type="primary">选择主图</el-button>
        </el-upload>
        <div v-if="form.mainImageUrl" class="preview">
          <img :src="form.mainImageUrl" alt="主图预览" />
          <i class="el-icon-close" @click="removeMainImage"></i>
        </div>
      </el-form-item>

      <!-- 轮播图 -->
      <el-form-item label="轮播图" prop="images">
        <el-upload
          list-type="picture-card"
          :file-list="fileList"
          :auto-upload="false"
          :limit="5"
          accept="image/*"
          @change="handleImagesChange"
          @remove="handleImageRemove"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
      </el-form-item>

      <!-- 按钮 -->
      <el-form-item class="button-group">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";

const router = useRouter();
const formRef = ref(null);

// 表单数据
const form = ref({
  goodName: "",
  price: null,
  stock: 0,
  status: 1,
  description: "",
  mainImageFile: null,
  mainImageUrl: "",
  images: [], // 真正要上传的 File[]
});

// upload 组件内部维护的 file-list
const fileList = ref([]);

// 校验规则
const rules = {
  goodName: [{ required: true, message: "请输入商品名称", trigger: "blur" }],
  price: [{ required: true, message: "请输入价格", trigger: "change" }],
  mainImageFile: [{ required: true, message: "请上传主图", trigger: "change" }],
};

// 主图选前预览
function beforeMainUpload(file) {
  form.value.mainImageFile = file;
  const reader = new FileReader();
  reader.onload = (e) => (form.value.mainImageUrl = e.target.result);
  reader.readAsDataURL(file);
  return false; // 阻止自动上传
}

// 移除主图
function removeMainImage() {
  form.value.mainImageFile = null;
  form.value.mainImageUrl = "";
}

// 轮播图变化：生成 preview，更新 fileList & form.images
async function handleImagesChange(file, fList) {
  // 为没有 url 的项生成 DataURL
  await Promise.all(
    fList.map((item) => {
      if (!item.url && item.raw) {
        return new Promise((resolve) => {
          const reader = new FileReader();
          reader.onload = (e) => {
            item.url = e.target.result;
            resolve();
          };
          reader.readAsDataURL(item.raw);
        });
      }
      return Promise.resolve();
    })
  );
  fileList.value = fList;
  form.value.images = fList.map((item) => item.raw);
}

// 单张移除时：同步更新
function handleImageRemove(file, fList) {
  fileList.value = fList;
  form.value.images = fList.map((item) => item.raw);
}

// 提交
async function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    const data = new FormData();
    data.append("goodName", form.value.goodName);
    data.append("price", form.value.price);
    data.append("stock", form.value.stock);
    data.append("status", form.value.status);
    data.append("description", form.value.description);
    data.append("mainImage", form.value.mainImageFile);
    form.value.images.forEach((file) => data.append("images", file));

    try {
      const res = await api.post("/good/create", data, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (res.data.code === "200") {
        ElMessage.success("新增商品成功");
        router.back();
      } else {
        ElMessage.error(res.data.msg || "新增失败");
      }
    } catch {
      ElMessage.error("网络错误，新增失败");
    }
  });
}

function goBack() {
  router.back();
}
</script>

<style scoped>
.add-good-container {
  max-width: 700px;
  margin: 20px auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.app-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.app-header h1 {
  flex: 1;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}
.back-button {
  background: #fff;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
.preview {
  margin-top: 10px;
  position: relative;
  display: inline-block;
}
.preview img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
.preview .el-icon-close {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #fff;
  border-radius: 50%;
  color: #f56c6c;
  cursor: pointer;
  font-size: 14px;
  padding: 2px;
}
.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}
</style>
