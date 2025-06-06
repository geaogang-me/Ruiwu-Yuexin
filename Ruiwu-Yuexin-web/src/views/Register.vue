<template>
  <div class="register-container">
    <div class="register-form">
      <h2 class="register-title">注册</h2>
      <el-form
        :model="form"
        label-width="80px"
        class="custom-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            autocomplete="off"
            :rules="[{ required: true, message: '用户名不能为空' }]"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            type="password"
            v-model="form.password"
            placeholder="请输入密码"
            autocomplete="off"
            :rules="[{ required: true, message: '密码不能为空' }]"
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            type="password"
            v-model="form.confirmPassword"
            placeholder="请确认密码"
            autocomplete="off"
            :rules="[{ required: true, message: '确认密码不能为空' }]"
          ></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
            <el-icon v-else><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleRegister">注册</el-button>
          <el-button type="default" @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import api from "@/plugins/axios";

export default {
  components: {
    Plus,
  },
  setup() {
    const form = reactive({
      username: "",
      password: "",
      confirmPassword: "",
      avatar: null,
    });
    const avatarUrl = ref("");

    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith("image/");
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        ElMessage.error("上传头像图片只能是图片格式!");
        return false;
      }
      if (!isLt2M) {
        ElMessage.error("上传头像图片大小不能超过 2MB!");
        return false;
      }

      const reader = new FileReader();
      reader.onload = () => {
        form.avatar = reader.result; // base64 字符串
        avatarUrl.value = reader.result;
      };
      reader.readAsDataURL(file);
      return false; // 阻止默认上传
    };
    const handleAvatarUpload = () => {}; // 使用 beforeUpload 已读取

    const router = useRouter();

    const handleRegister = async () => {
      if (form.username === "") {
        ElMessage.error("用户名不能为空");
        return;
      }

      if (form.password === "") {
        ElMessage.error("密码不能为空");
        return;
      }

      if (form.confirmPassword === "") {
        ElMessage.error("确认密码不能为空");
        return;
      }

      if (form.password !== form.confirmPassword) {
        ElMessage.error("两次输入的密码不一致");
        return;
      }

      try {
        const response = await api.post("/register", {
          username: form.username,
          password: form.password,
          avatar: form.avatar,
        });

        if (response.data.code === "200") {
          ElMessage.success("注册成功！");
          await router.push("/login");
        } else {
          ElMessage.error(response.data.msg);
        }
      } catch (error) {
        ElMessage.error("注册请求失败，请稍后重试");
      }
    };

    const handleCancel = () => {
      form.username = "";
      form.password = "";
      form.confirmPassword = "";
      form.avatar = null;
      avatarUrl.value = "";
      router.push("/login");
    };

    return {
      form,
      avatarUrl,
      beforeAvatarUpload,
      handleAvatarUpload,
      handleRegister,
      handleCancel,
    };
  },
};
</script>
<style scoped>
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
  margin-bottom: 10px;
  object-fit: cover;
}
.register-title {
  text-align: center;
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 3rem;
}

.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.register-form {
  width: 300px;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.custom-form {
  width: 100%;
}
</style>