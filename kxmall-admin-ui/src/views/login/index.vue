<template>
  <div
    class="login-container"
    @keyup.enter.native="handleLogin">
    <div class="login-weaper animated bounceInDown">
      <div class="login-left">
        <div class="login-time">
          欢迎使用
        </div>
        <img
          class="img"
          src="@/assets/avatar.png"
          alt="">
        <p class="title">kxmall 商城后台登录</p>
      </div>
      <div class="login-border">
        <div class="login-main">
          <h4 class="login-title">
            登录 kxmall
          </h4>
          <el-form
            ref="loginForm"
            :rules="loginRules"
            :model="loginForm"
            class="login-form"
            status-icon
            label-width="0">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="small"
                auto-complete="off"
                clearable
                @keyup.enter.native="handleLogin">
                <i
                  slot="prefix"
                  class="icon-yonghu"/>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                :type="passwordType"
                v-model="loginForm.password"
                placeholder="请输入密码"
                size="small"
                auto-complete="off"
                clearable
                @keyup.enter.native="handleLogin">
                <i
                  slot="suffix"
                  class="el-icon-view el-input__icon"
                  @click="showPwd"/>
                <i
                  slot="prefix"
                  class="icon-mima"/>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <el-col :span="16">
                <el-input
                  v-model="loginForm.verifyCode"
                  placeholder="输入验证码"
                  size="small"
                  auto-complete="off"
                  clearable
                  @keyup.enter.native="handleLogin">
                  <i
                    slot="prefix"
                    class="icon-yanzhengma"
                    style="margin-top:6px;"/>
                </el-input>
              </el-col>
              <el-col :span="6">
                <img :src="codeUrl" @click="getCode" >
              </el-col>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="small"
                class="login-submit"
                @click.native.prevent="handleLogin">登录</el-button>
            </el-form-item>
          </el-form>

          <div class="login-menu">
            <a
              href="https://gitee.com/zhengkaixing/kxmall"
              @click.stop="activeName='user'">kxmall官网</a>
            <a
              href="#"
              @click.stop="activeName='code'">客服</a>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from '@/api/login'

export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (validateUsername == null) {
        callback(new Error('请输入正确的管理员用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('管理员密码长度应大于6'))
      } else {
        callback()
      }
    }
    return {
      codeUrl: '',
      loginForm: {
        username: 'guest',
        password: '123456',
        verifyCode: '',
        uuid: ''
      },
      loginRules: {
        username: [
          { required: true, trigger: 'blur', validator: validateUsername }
        ],
        password: [
          { required: true, trigger: 'blur', validator: validatePassword }
        ],
        verifyCode: [
          { required: true, trigger: 'blur', message: '验证码不能为空' }
        ]
      },
      passwordType: 'password',
      loading: false,
      verifyLoading: false,
      show: true,
      count: '',
      timer: null
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
  },
  destroyed() {
    // window.removeEventListener('hashchange', this.afterQRScan)
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.codeUrl = 'data:image/gif;base64,' + res.data.img
        this.loginForm.uuid = res.data.uuid
      })
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid && !this.loading) {
          this.loading = true
          this.$store
            .dispatch('LoginByUsername', this.loginForm)
            .then(() => {
              this.loading = false
              this.$router.push({ path: this.redirect || '/' })
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              })
              this.getCode()
              this.loading = false
            })
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style>
.msg-text {
  display: block;
  width: 60px;
  font-size: 12px;
  text-align: center;
  cursor: pointer;
}
.msg-text.display {
  color: #ccc;
}
  .myInput {
    background-color: #F5F7FA;
    color: #909399;
    vertical-align: middle;
    display: table-cell;
    position: relative;
    border: 1px solid #DCDFE6;
    border-radius: 4px;
    padding: 0 20px;
    width: 1px;
    white-space: nowrap;
  }
</style>

<style lang="scss">
@import "@/styles/login.scss";
</style>
