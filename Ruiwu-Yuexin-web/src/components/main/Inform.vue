<template>
  <div class="form-container">
    <el-dialog
      title="修改个人信息"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
      @close="onClose"
      :top="'1vh'"
    >
      <el-form
        ref="userFormRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
      >
        <!-- 头像上传 -->
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" clearable />
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio label="男" />
            <el-radio label="女" />
            <el-radio label="保密" />
          </el-radio-group>
        </el-form-item>

        <!-- 原密码 -->
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 新密码 -->
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" clearable />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" clearable />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="2"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">提交修改</el-button>
          <el-button @click="dialogVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import api from "@/plugins/axios";
import { ElMessage } from "element-plus";
import { useAuth } from "@/composables/useAuth"; // <-- 新增

const { checkTokenValidity, refreshToken } = useAuth();

// --- 引用 & 状态 ---
const userFormRef = ref();
const dialogVisible = ref(false);

function onClose() {
  router.push({ path: "/home" });
}

const form = reactive({
  username: "",
  sex: "保密",
  oldPassword: "", // 原密码字段
  newPassword: "", // 新密码字段
  confirmPassword: "",
  phone: "",
  email: "",
  address: "",
  avatar: "",
});
watch(
  () => form.newPassword,
  () => {
    if (form.confirmPassword) {
      userFormRef.value?.validateField("confirmPassword", (valid, fields) => {
        // valid 是 boolean，fields 是错误信息
        // 如果你不需要做额外处理，可以留空
      });
    }
  }
);

// --- 表单校验规则 ---
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 16, message: "长度在 2 到 16 个字符", trigger: "blur" },
  ],
  oldPassword: [
    {
      required: true,
      message: "请输入原密码",
      trigger: ["blur", "change"],
    },
    {
      min: 6,
      max: 20,
      message: "密码长度在6到20个字符",
      trigger: ["blur", "change"],
    },
  ],
  newPassword: [
    {
      required: true,
      message: "请输入新密码",
      trigger: ["blur", "change"],
    },
    {
      min: 6,
      max: 20,
      message: "密码长度在6到20个字符",
      trigger: ["blur", "change"],
    },
  ],
  confirmPassword: [
    {
      required: true,
      message: "请再次输入新密码",
      trigger: ["blur", "change"],
    },
    {
      validator: (_, value, callback) => {
        if (!form.newPassword && !value) {
          return callback();
        }
        if (!value) {
          return callback(new Error("请再次输入新密码"));
        }
        if (value !== form.newPassword) {
          return callback(new Error("两次输入密码不一致"));
        }
        callback();
      },
      trigger: ["blur", "change", "input"],
    },
  ],
};

// --- 路由 & Store ---
const router = useRouter();
const store = useStore();

// --- 辅助：处理后端返回的 avatar 字符串 ---
const processAvatar = (avatar) => {
  if (!avatar) return "";
  if (avatar.startsWith("data:image")) return avatar;
  if (avatar.includes("base64,")) return avatar;
  return `data:image/png;base64,${avatar}`;
};

// --- 上传前将 file 转 Base64 ---
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("头像大小不能超过 2MB!");
    return false;
  }
  const reader = new FileReader();
  reader.onload = () => (form.avatar = reader.result);
  reader.readAsDataURL(file);
  return false; // 阻止默认上传
};

// --- 加载后端用户数据 填充表单 ---
const loadUserInfo = async () => {
  if (!checkTokenValidity()) return;
  try {
    const res = await api.get("/user/info", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });
    const { code, msg, data: user } = res.data;
    if (code !== "200") {
      throw new Error(msg || "请求失败");
    }
    Object.assign(form, {
      username: user.username || "",
      oldPassword: "", // 原密码字段默认为空
      newPassword: "",
      confirmPassword: "",
      sex: user.sex || "保密",
      phone: user.phone || "",
      email: user.email || "",
      address: user.address || "",
      avatar: processAvatar(user.avatar),
    });
    dialogVisible.value = true;
  } catch (err) {
    console.error(err);
    ElMessage.error("获取用户信息失败");
  }
};

// --- 提交修改 ---
const submitForm = async () => {
  ElMessage.closeAll();
  if (!checkTokenValidity()) return;
  try {
    await userFormRef.value.validate();
    // 构造参数
    const params = { ...form };
    delete params.confirmPassword;

    // 验证原密码是否正确
    const verifyRes = await api.post("/user/verify-password", {
      oldPassword: params.oldPassword,
    });
    const { code, msg } = verifyRes.data;
    if (code !== "200") {
      return ElMessage.error(msg || "原密码错误");
    }

    // 如果原密码正确，提交新密码
    if (params.newPassword) {
      params.password = params.newPassword; // 映射为后端需要的字段名
      delete params.newPassword;
    }

    const res = await api.put("/user/update", params);
    const { data: updatedUser } = res.data;

    ElMessage.success("修改成功");
    dialogVisible.value = false;

    // 本地 & Vuex 同步
    const old = JSON.parse(localStorage.getItem("userInfo")) || {};
    const merged = {
      ...old,
      ...params,
      ...(updatedUser?.avatar ? { avatar: updatedUser.avatar } : {}),
    };
    localStorage.setItem("userInfo", JSON.stringify(merged));

    store.commit("updateUserInfo", merged);

    router.push({ path: "/home" });
  } catch (err) {
    console.error(err);
    ElMessage.error(err.response?.data?.message || err.message || "修改异常");
  }
};

onMounted(() => {
  refreshToken(); // 如果 token 可能更新时用
  loadUserInfo();
});
</script>

<style scoped>
.form-container {
  position: relative;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.el-form {
  padding: 5px 30px;
}
</style>