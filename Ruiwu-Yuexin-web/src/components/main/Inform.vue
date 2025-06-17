<template>
  <div class="form-container">
    <!-- 头部导航栏 -->
    <div class="app-header">
      <div class="app-title">
        <i class="fas fa-user-cog"></i>
        <h1>个人信息管理</h1>
      </div>
      <el-button type="primary" plain class="back-button" @click="goBack">
        <i class="fas fa-arrow-left"></i> 返回首页
      </el-button>
    </div>

    <!-- 主内容区 -->
    <div class="form-content-container">
      <div class="form-card">
        <!-- 用户头像上传区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <div class="avatar-preview">
              <img v-if="form.avatar" :src="form.avatar" class="avatar-image" />
              <div v-else class="avatar-placeholder">
                <i class="fas fa-user"></i>
              </div>
            </div>
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
            >
              <el-button type="primary" plain>
                <i class="fas fa-camera"></i> 更换头像
              </el-button>
            </el-upload>
          </div>
        </div>

        <!-- 表单内容 -->
        <div class="form-sections">
          <el-form
            ref="userFormRef"
            :model="form"
            :rules="baseRules"
            label-width="120px"
            label-position="right"
          >
            <!-- 基础信息卡片 -->
            <div class="form-section-card">
              <div class="section-header">
                <div class="section-icon">
                  <i class="fas fa-user-circle"></i>
                </div>
                <h2>基本资料</h2>
              </div>
              <div class="section-body">
                <div class="form-row">
                  <el-form-item label="用户名" prop="username">
                    <el-input
                      v-model="form.username"
                      clearable
                      placeholder="请输入用户名"
                    />
                  </el-form-item>
                </div>

                <div class="form-row">
                  <el-form-item label="性别" prop="sex">
                    <el-radio-group v-model="form.sex">
                      <el-radio label="男" />
                      <el-radio label="女" />
                      <el-radio label="保密" />
                    </el-radio-group>
                  </el-form-item>
                </div>
              </div>
            </div>

            <!-- 联系信息卡片 -->
            <div class="form-section-card">
              <div class="section-header">
                <div class="section-icon">
                  <i class="fas fa-address-book"></i>
                </div>
                <h2>联系信息</h2>
              </div>
              <div class="section-body">
                <div class="form-row">
                  <el-form-item label="手机号" prop="phone">
                    <el-input
                      v-model="form.phone"
                      clearable
                      placeholder="请输入手机号"
                    />
                  </el-form-item>
                </div>

                <div class="form-row">
                  <el-form-item label="邮箱" prop="email">
                    <el-input
                      v-model="form.email"
                      clearable
                      placeholder="请输入邮箱"
                    />
                  </el-form-item>
                </div>

                <div class="form-row">
                  <el-form-item label="地址" prop="address">
                    <el-input
                      v-model="form.address"
                      type="textarea"
                      :rows="3"
                      clearable
                      placeholder="请输入详细地址"
                    />
                  </el-form-item>
                </div>
              </div>
            </div>
            <div class="form-section-card">
              <el-button
                type="info"
                class="change-password-button"
                @click="passwordDialogVisible = true"
              >
                <i class="fas fa-key"></i> 修改密码
              </el-button>
            </div>

            <!-- 表单操作按钮 -->
            <div class="form-actions">
              <el-button
                type="primary"
                class="submit-button"
                @click="submitForm"
              >
                <i class="fas fa-save"></i> 保存修改
              </el-button>

              <el-button class="cancel-button" @click="goBack">
                <i class="fas fa-times"></i> 关闭
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>

  <!-- 修改密码对话框 -->
  <el-dialog
    v-model="passwordDialogVisible"
    title="修改密码"
    width="450px"
    :close-on-click-modal="false"
    @closed="resetPasswordForm"
  >
    <el-form
      ref="passwordFormRef"
      :model="passwordForm"
      :rules="passwordRules"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          type="password"
          show-password
          placeholder="请输入原密码"
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码"
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>

      <div style="display: flex; justify-content: flex-end; margin-top: 20px">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPasswordChange">确定</el-button>
      </div>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import api from "@/plugins/axios";
import Swal from "sweetalert2";
import { useAuth } from "@/composables/useAuth"; // <-- 新增

const { checkTokenValidity, refreshToken } = useAuth();
const router = useRouter();
const store = useStore();
// --- 引用 & 状态 ---
const userFormRef = ref();
const passwordFormRef = ref();
// 密码对话框状态
const passwordDialogVisible = ref(false);

const form = reactive({
  username: "",
  sex: "保密",
  phone: "",
  email: "",
  address: "",
  avatar: "",
});
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const baseRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 16, message: "长度在 2 到 16 个字符", trigger: "blur" },
  ],
};

const passwordRules = {
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
        if (!passwordForm.newPassword && !value) {
          return callback();
        }
        if (!value) {
          return callback(new Error("请再次输入新密码"));
        }
        if (value !== passwordForm.newPassword) {
          return callback(new Error("两次输入密码不一致"));
        }
        callback();
      },
      trigger: ["blur", "change", "input"],
    },
  ],
};

// --- 路由 & Store ---

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
    Swal.fire({
      icon: "error",
      title: "只能上传图片文件",
      timer: 1000,
      showConfirmButton: false,
    });
    return false;
  }
  if (!isLt2M) {
    Swal.fire({
      icon: "error",
      title: "头像图片不能超过2MB！",
      timer: 1000,
      showConfirmButton: false,
    });
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
      sex: user.sex || "保密",
      phone: user.phone || "",
      email: user.email || "",
      address: user.address || "",
      avatar: processAvatar(user.avatar),
    });
  } catch (err) {
    Swal.fire({
      icon: "error",
      title: "获取用户信息失败",
      timer: 1000,
      showConfirmButton: false,
    });
  }
};
const submitting = ref(false);
// --- 提交修改 ---
const submitForm = async () => {
  if (!checkTokenValidity()) return;

  submitting.value = true;
  try {
    // 校验表单
    await userFormRef.value.validate();

    // 构造参数
    const params = {
      username: form.username,
      sex: form.sex,
      phone: form.phone,
      email: form.email,
      address: form.address,
      avatar: form.avatar,
    };

    // 发起更新请求
    const response = await api.put("/user/update", params);
    const { code, msg, data: updatedUser } = response.data;

    // 如果后端业务码不是 200，则抛出错误进入 catch
    if (String(code) !== "200") {
      throw new Error(msg || "修改异常");
    }

    // 成功提示
    Swal.fire({
      icon: "success",
      title: "个人信息修改成功",
      timer: 1000,
      showConfirmButton: false,
    });

    // 本地 & Vuex 同步
    const old = JSON.parse(localStorage.getItem("userInfo")) || {};
    const merged = {
      ...old,
      ...params,
      ...(updatedUser?.avatar ? { avatar: updatedUser.avatar } : {}),
    };
    localStorage.setItem("userInfo", JSON.stringify(merged));
    store.commit("updateUserInfo", merged);
  } catch (err) {
    console.error(err);
    // 优先展示自定义错误信息，否则兜底展示 err.message
    Swal.fire({
      icon: "error",
      title: err.response?.data?.message || err.message || "修改异常",
      timer: 1000,
      showConfirmButton: false,
    });
  } finally {
    submitting.value = false;
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  passwordFormRef.value?.resetFields();
};

// 提交密码修改
const submitPasswordChange = async () => {
  try {
    // 验证密码表单
    await passwordFormRef.value.validate();
    const params = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    };
    // 验证原密码
    const verifyRes = await api.post("/user/verify-password", {
      oldPassword: passwordForm.oldPassword,
    });

    if (verifyRes.data.code !== "200") {
      return Swal.fire({
        icon: "error",
        title: verifyRes.data.msg || "原密码错误",
        timer: 1000,
        showConfirmButton: false,
      });
    }

    // 修改密码
    const changeRes = await api.post("/user/change-password", params);

    if (changeRes.data.code === "200") {
      Swal.fire({
        icon: "success",
        title: "密码修改成功",
        timer: 1000,
        showConfirmButton: false,
      });
      passwordDialogVisible.value = false;
    } else {
      Swal.fire({
        icon: "error",
        title: changeRes.data.msg || "密码修改失败",
        timer: 1000,
        showConfirmButton: false,
      });
    }
  } catch (err) {
    console.error(err);
    Swal.fire({
      icon: "error",
      title: err.response?.data?.message || "密码修改失败",
      timer: 1000,
      showConfirmButton: false,
    });
  }
};
const goBack = () => {
  router.push({ path: "/home" });
};
onMounted(() => {
  refreshToken(); // 如果 token 可能更新时用
  loadUserInfo();
});
</script>

<style scoped>
.form-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

/* 头部导航栏样式 */
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

.app-title i {
  font-size: 32px;
  color: #2766f5;
}

.app-title h1 {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
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
  box-shadow: 0 8px 25px rgba(39, 102, 245, 0.2);
  transform: translateY(-2px);
  color: #2766f5;
}

/* 表单容器 */
.form-content-container {
  display: flex;
  justify-content: center;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
  width: 100%;
  max-width: 900px;
  padding: 30px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.form-card:hover {
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
}

/* 头像区域 */
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar-preview {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  width: 150px;
  height: 150px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.avatar-preview:hover {
  transform: scale(1.05);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, #c9d6ff, #e2e2e2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  color: #fff;
}

/* 表单区域 */
.form-sections {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.form-section-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid #edf2f9;
}

.form-section-card:hover {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #edf2f9;
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.section-body {
  padding: 10px 5px;
}

.form-row {
  margin-bottom: 20px;
}

/* 按钮区域 */
.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}
/* 修改密码按钮样式 */
.change-password-button {
  background: linear-gradient(to right, #4f8bf3, #54d0e3);
  border: none;
  border-radius: 10px;
  padding: 15px 25px;
  color: white;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 8px 20px rgba(251, 93, 219, 0.35);
  transition: all 0.3s ease;
}

.change-password-button:hover {
  background: linear-gradient(to right, #ff4d8b, #ff5cf7);
  transform: translateY(-3px);
  box-shadow: 0 12px 25px rgba(255, 92, 165, 0.45);
}
.submit-button {
  background: linear-gradient(to right, #2766f5, #3a8dff);
  border: none;
  border-radius: 10px;
  padding: 15px 35px;
  color: white;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 8px 20px rgba(39, 102, 245, 0.35);
  transition: all 0.3s ease;
}

.submit-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 25px rgba(39, 102, 245, 0.45);
}

.cancel-button {
  border: 1px solid #eaeef5;
  background: #fff;
  border-radius: 10px;
  padding: 15px 35px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  color: #5a5e66;
}

.cancel-button:hover {
  background: #f5f7fa;
  color: #2766f5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 表单元素样式调整 */
.el-input,
.el-textarea {
  border-radius: 10px;
}

.el-input :deep(.el-input__inner) {
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  padding: 12px 15px;
  height: 44px;
}

.el-input :deep(.el-input__inner):focus {
  border-color: #2766f5;
  box-shadow: 0 0 0 2px rgba(39, 102, 245, 0.2);
}

.el-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  padding: 15px;
}

.el-textarea :deep(.el-textarea__inner):focus {
  border-color: #2766f5;
  box-shadow: 0 0 0 2px rgba(39, 102, 245, 0.2);
}

.el-radio-group {
  display: flex;
  gap: 15px;
}

/* 响应式设计 */
@media (max-width: 900px) {
  .form-card {
    flex-direction: column;
    padding: 20px;
  }

  .avatar-section {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    margin-top: 30px;
    gap: 15px;
    flex-wrap: wrap;
  }

  .submit-button,
  .cancel-button {
    width: 100%;
    padding: 15px 20px;
  }

  .el-form :deep(.el-form-item__label) {
    text-align: left;
    margin-bottom: 5px;
    width: 100% !important;
  }

  .el-form :deep(.el-form-item__content) {
    width: 100%;
  }

  .el-form-item {
    margin-bottom: 25px;
  }
}

@media (max-width: 480px) {
  .app-title h1 {
    font-size: 24px;
  }

  .form-section-card {
    padding: 20px 15px;
  }
}
</style>