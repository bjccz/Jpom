<template>
  <div class="user-header">
    <a-tooltip placement="left" title="切换工作空间">
      <a-select v-model="selectWorkspace" class="workspace jpom-workspace" option-filter-prop="children" show-search placeholder="工作空间" @change="handleChange">
        <a-select-option v-for="item in myWorkspaceList" :key="item.id">
          <a-tooltip placement="left" :title="item.name">
            {{ item.name }}
          </a-tooltip>
        </a-select-option>
      </a-select>
    </a-tooltip>
    <a-dropdown>
      <!--      <a-avatar-->
      <!--        shape="square"-->
      <!--        size="large"-->
      <!--        :style="{ backgroundColor: '#f56a00', verticalAlign: 'middle' ,fontSize:'40px'}">-->
      <!--        -->
      <!--      </a-avatar>-->
      <a-tooltip placement="left" :title="this.getUserInfo.name">
        <a-button
          class="ant-dropdown-link jpom-user-operation"
          :style="{ backgroundColor: '#1890ff', color: '#fff', verticalAlign: 'middle', marginRight: 0 }"
          @click="(e) => e.preventDefault()"
          :title="getUserInfo.name"
        >
          {{ avatarName }} <a-icon type="down" />
        </a-button>
      </a-tooltip>
      <a-menu slot="overlay">
        <a-menu-item>
          <a href="javascript:;" @click="handleUpdatePwd">修改密码</a>
        </a-menu-item>
        <!-- <a-menu-item>
          <a href="javascript:;" @click="handleUpdateName">修改昵称</a>
        </a-menu-item> -->
        <a-menu-item>
          <a href="javascript:;" @click="handleUpdateUser">用户资料</a>
        </a-menu-item>
        <a-menu-item>
          <a href="javascript:;" @click="logOut">退出登录</a>
        </a-menu-item>
      </a-menu>
    </a-dropdown>
    <!-- 修改密码区 -->
    <a-modal v-model="updateNameVisible" title="修改密码" @ok="handleUpdatePwdOk" :maskClosable="false">
      <a-form-model ref="pwdForm" :rules="rules" :model="temp" :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
        <a-form-model-item label="原密码" prop="oldPwd">
          <a-input-password v-model="temp.oldPwd" placeholder="Old password" />
        </a-form-model-item>
        <a-form-model-item label="新密码" prop="newPwd">
          <a-input-password v-model="temp.newPwd" placeholder="New password" />
        </a-form-model-item>
        <a-form-model-item label="确认密码" prop="confirmPwd">
          <a-input-password v-model="temp.confirmPwd" placeholder="Confirm password" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <!-- 修改用户资料区 -->
    <a-modal v-model="updateUserVisible" title="修改用户资料" @ok="handleUpdateUserOk" :maskClosable="false">
      <a-form-model ref="userForm" :rules="rules" :model="temp" :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
        <a-form-model-item label="临时token" prop="token">
          <a-input v-model="temp.token" placeholder="Token" />
        </a-form-model-item>
        <a-form-model-item label="长期token" prop="md5Token">
          <a-input v-model="temp.md5Token" placeholder="Token" />
        </a-form-model-item>
        <a-form-model-item label="邮箱地址" prop="email">
          <a-input v-model="temp.email" placeholder="邮箱地址" />
        </a-form-model-item>
        <a-form-model-item v-show="showCode" label="邮箱验证码" prop="code">
          <a-row :gutter="8">
            <a-col :span="15">
              <a-input v-model="temp.code" placeholder="邮箱验证码" />
            </a-col>
            <a-col :span="4">
              <a-button type="primary" :disabled="!temp.email" @click="sendEmailCode">发送验证码</a-button>
            </a-col>
          </a-row>
        </a-form-model-item>
        <a-form-model-item label="钉钉通知地址" prop="dingDing">
          <a-input v-model="temp.dingDing" placeholder="钉钉通知地址" />
        </a-form-model-item>
        <a-form-model-item label="企业微信通知地址" prop="workWx">
          <a-input v-model="temp.workWx" placeholder="企业微信通知地址" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>
<script>
import { mapGetters } from "vuex";
import { updatePwd, sendEmailCode, editUserInfo, getUserInfo, myWorkspace } from "@/api/user";
import sha1 from "sha1";
export default {
  data() {
    return {
      collapsed: false,
      // 修改密码框
      updateNameVisible: false,
      updateUserVisible: false,
      temp: {},
      myWorkspaceList: [],
      selectWorkspace: "",
      // 表单校验规则
      rules: {
        oldPwd: [
          { required: true, message: "Please input old password", trigger: "blur" },
          { max: 20, message: "密码长度为6-20", trigger: "blur" },
          { min: 6, message: "密码长度为6-20", trigger: "blur" },
        ],
        newPwd: [
          { required: true, message: "Please input new password", trigger: "blur" },
          { max: 20, message: "密码长度为6-20", trigger: "blur" },
          { min: 6, message: "密码长度为6-20", trigger: "blur" },
        ],
        confirmPwd: [
          { required: true, message: "Please input confirmPwd password", trigger: "blur" },
          { max: 20, message: "密码长度为6-20", trigger: "blur" },
          { min: 6, message: "密码长度为6-20", trigger: "blur" },
        ],
        email: [{ required: true, message: "Please input email", trigger: "blur" }],
      },
    };
  },
  computed: {
    ...mapGetters(["getToken", "getUserInfo", "getWorkspaceId"]),
    // 处理展示的名称 中文 3 个字 其他 4 个字符
    avatarName() {
      const reg = new RegExp("[\u4E00-\u9FA5]+");
      if (reg.test(this.getUserInfo.name)) {
        return this.getUserInfo.name.substring(0, 3);
      } else {
        return this.getUserInfo.name.substring(0, 4);
      }
    },
    showCode() {
      return this.getUserInfo.email !== this.temp.email;
    },
  },
  inject: ["reload"],
  created() {
    this.init();
  },
  methods: {
    init() {
      myWorkspace().then((res) => {
        this.myWorkspaceList = res.data;
        let wid = this.$route.query.wid;
        this.selectWorkspace = wid ? wid : this.getWorkspaceId;
        if (!this.selectWorkspace) {
          this.handleChange(res.data[0]?.id);
        } else {
          this.$router.push({
            query: { ...this.$route.query, wid: this.selectWorkspace },
          });
        }
      });
    },
    // 退出登录
    logOut() {
      this.$confirm({
        title: "系统提示",
        content: "真的要退出系统么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          return new Promise((resolve) => {
            // 退出登录
            this.$store.dispatch("logOut").then(() => {
              this.$notification.success({
                message: "退出登录成功",
              });
              this.$router.push("/login");
              resolve();
            });
          });
        },
      });
    },
    // 加载修改密码对话框
    handleUpdatePwd() {
      this.temp = {};
      this.updateNameVisible = true;
    },
    // 修改密码
    handleUpdatePwdOk() {
      // 检验表单
      this.$refs["pwdForm"].validate((valid) => {
        if (!valid) {
          return false;
        }
        // 判断两次新密码是否一致
        if (this.temp.newPwd !== this.temp.confirmPwd) {
          this.$notification.error({
            message: "两次密码不一致...",
          });
          return;
        }
        // 提交修改
        const params = {
          oldPwd: sha1(this.temp.oldPwd),
          newPwd: sha1(this.temp.newPwd),
        };
        updatePwd(params).then((res) => {
          // 修改成功
          if (res.code === 200) {
            // 退出登录
            this.$store.dispatch("logOut").then(() => {
              this.$notification.success({
                message: res.msg,
              });
              this.$refs["pwdForm"].resetFields();
              this.updateNameVisible = false;
              this.$router.push("/login");
            });
          }
        });
      });
    },
    // 加载修改用户资料对话框
    handleUpdateUser() {
      getUserInfo().then((res) => {
        if (res.code === 200) {
          this.temp = res.data;
          this.temp.token = this.getToken;
          //this.temp.md5Token = res.data.md5Token;
          this.updateUserVisible = true;
        }
      });
    },
    // 发送邮箱验证码
    sendEmailCode() {
      if (!this.temp.email) {
        this.$notification.error({
          message: "请输入邮箱地址",
        });
        return;
      }
      sendEmailCode(this.temp.email).then((res) => {
        if (res.code === 200) {
          this.$notification.success({
            message: res.msg,
          });
        }
      });
    },
    // 修改用户资料
    handleUpdateUserOk() {
      // 检验表单
      this.$refs["userForm"].validate((valid) => {
        if (!valid) {
          return false;
        }
        editUserInfo(this.temp).then((res) => {
          // 修改成功
          if (res.code === 200) {
            this.$notification.success({
              message: res.msg,
            });
            // 清空表单校验
            this.$refs["userForm"].resetFields();
            this.updateUserVisible = false;
          }
        });
      });
    },
    handleChange(value) {
      this.$store.dispatch("changeWorkspace", value);
      this.$router
        .push({
          query: { ...this.$route.query, wid: value },
        })
        .then(() => {
          this.reload();
        });
    },
    // toOldIndex() {
    //   window.location.href = '/old.html'
    // }
  },
};
</script>
<style scoped>
.workspace {
  width: 100px;
  margin-right: 10px;
}
.user-header {
  display: inline-table;
  width: 220px;
  text-align: right;
  margin-right: 20px;
  cursor: pointer;
}
.close-all {
  margin-right: 10px;
}
</style>
