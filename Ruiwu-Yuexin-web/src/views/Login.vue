<template>
  <div class="login-container">
    <div class="left-container">
      <img
        src="@/assets/images/login.png"
        alt="Login Image"
        class="login-image"
      />
    </div>
    <div class="right-container">
      <h2 class="login-title">瑞物悦心</h2>
      <el-form
        :model="form"
        label-width="80px"
        class="custom-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            autocomplete="off"
            class="input-custom"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            type="password"
            v-model="form.password"
            placeholder="请输入密码"
            autocomplete="off"
            class="input-custom"
          ></el-input>
        </el-form-item>
        <el-form-item label="验证码">
          <el-row type="flex" justify="space-between">
            <el-col :span="10">
              <el-input
                v-model="form.captcha"
                placeholder="请输入验证码"
                autocomplete="off"
                class="captcha-input"
              ></el-input>
            </el-col>
            <el-col :span="10">
              <div class="captcha-code" @click="debouncedRefreshCaptcha">
                {{ randomCaptcha }}
              </div>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button type="default" @click="handleCancel" class="cancel-button"
            >取消</el-button
          >
          <el-button type="text" @click="handleRegister">注册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import Swal from "sweetalert2";
import debounce from "lodash/debounce";
import api from "@/plugins/axios";
import { useStore } from "vuex"; // 引入 useStore

export default {
  setup() {
    const form = reactive({
      username: "",
      password: "",
      captcha: "",
    });

    const randomCaptcha = ref("");
    const router = useRouter();
    const store = useStore(); // 使用 useStore

    const refreshCaptcha = () => {
      const chars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      randomCaptcha.value = Array.from(
        { length: 6 },
        () => chars[Math.floor(Math.random() * chars.length)]
      ).join("");
    };

    const debouncedRefreshCaptcha = debounce(refreshCaptcha, 300);

    const handleLogin = async () => {
      if (!form.username || !form.password || !form.captcha) {
        Swal.fire({
          icon: "error",
          title: "请填写所有字段",
          timer: 1500,
          showConfirmButton: false,
        });
        return;
      }

      if (form.captcha.toLowerCase() !== randomCaptcha.value.toLowerCase()) {
        Swal.fire({
          icon: "error",
          title: "验证码无效",
          timer: 1500,
          showConfirmButton: false,
        });
        return;
      }

      try {
        const response = await api.post("/login", {
          username: form.username,
          password: form.password,
        });

        if (response.data.code === "200") {
          // 保存 JWT Token 到 localStorage
          const userData = response.data.data;

          // 统一通过 Vuex 更新状态
          store.commit("updateToken", userData.token);
          store.commit("updateUserInfo", userData);
          store.commit("setLogin", {
            isLogin: true,
            userId: userData.id,
          });

          Swal.fire({
            icon: "success",
            title: "登录成功！",
            timer: 1200,
            showConfirmButton: false,
          }).then(() => {
            router.push("/home");
          });
        } else {
          Swal.fire({
            icon: "error",
            title: response.data.msg || "登录失败",
          });
        }
      } catch (error) {
        Swal.fire({
          icon: "error",
          title: "登录请求失败，请稍后重试",
        });
      }
    };

    const handleCancel = () => {
      form.username = "";
      form.password = "";
      form.captcha = "";
      router.push("/home");
    };

    const handleRegister = () => {
      Swal.fire({
        icon: "info",
        title: "跳转到注册页面",
        timer: 1000,
        showConfirmButton: false,
      }).then(() => {
        router.push("/register");
      });
    };

    refreshCaptcha();

    return {
      form,
      randomCaptcha,
      handleLogin,
      handleCancel,
      handleRegister,
      debouncedRefreshCaptcha,
    };
  },
};
</script>

<style scoped>
.login-title {
  text-align: center;
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 3rem;
}

.login-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  width: 80%;
  max-width: 800px;
  min-width: 300px;
  height: 500px; /* 必要时可改成 min-height: 500px */

  display: flex;
  justify-content: center;
  align-items: center;

  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 18px;
  overflow: hidden;
}

.right-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 1rem;
}

.left-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 60px;
  margin-left: 30px;
}

.custom-form {
  width: 20rem;
}

.captcha-input {
  width: 105px;
  height: 2rem;
}

.captcha-code {
  cursor: pointer;
  width: 80px;
  height: 30px;
  margin-left: 15px;
  background-color: #4b9497;
  border-radius: 4px;
  font-weight: bold;
  font-size: 18px;
  user-select: none;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>