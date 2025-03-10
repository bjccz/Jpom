<template>
  <a-layout class="node-layout">
    <!-- 侧边栏 节点管理菜单 -->
    <a-layout-sider theme="light" class="node-sider jpom-node-sider">
      <a-menu theme="light" mode="inline" :default-selected-keys="selectedKeys" :default-open-keys="defaultOpenKey">
        <template v-for="menu in nodeMenuList">
          <a-sub-menu v-if="menu.childs" :key="menu.id" :class="menu.id">
            <span slot="title">
              <a-icon :type="menu.icon_v3" />
              <span>{{ menu.title }}</span>
            </span>
            <a-menu-item v-for="subMenu in menu.childs" :key="subMenu.id" @click="handleMenuClick(subMenu.id, subMenu.pId)" :class="subMenu.id">
              <span>{{ subMenu.title }}</span>
            </a-menu-item>
          </a-sub-menu>
          <a-menu-item v-else :key="menu.id" @click="handleMenuClick(menu.id)">
            <a-icon :type="menu.icon_v3" />
            <span>{{ menu.title }}</span>
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>
    <!-- 节点管理的各个组件 -->
    <a-layout-content class="layout-content jpom-node-content">
      <welcome v-if="currentId === 'welcome'" :node="node" />
      <project-list v-if="currentId === 'manageList'" :node="node" />
      <jdk-list v-if="currentId === 'jdkList'" :node="node" />
      <recover v-if="currentId === 'projectRecover'" :node="node" />
      <tomcat v-if="currentId === 'tomcatManage'" :node="node" />
      <script-template v-if="currentId === 'script'" :node="node" />
      <script-log v-if="currentId === 'script-log'" :node="node" />
      <nginx-list v-if="currentId === 'nginxList'" :node="node" />
      <cert v-if="currentId === 'certificate'" :node="node" />
      <white-list v-if="currentId === 'whitelistDirectory'" :node="node" />
      <cache v-if="currentId === 'cacheManage'" :node="node" />
      <log v-if="currentId === 'logManage'" :node="node" />
      <upgrade v-if="currentId === 'update'" :node="node" />
      <config-file v-if="currentId === 'sysConfig'" :node="node" />
    </a-layout-content>
  </a-layout>
</template>
<script>
import { mapGetters } from "vuex";
import { getNodeMenu } from "../../../api/menu";
import Welcome from "./welcome";
import ProjectList from "./project/project-list";
import JdkList from "./project/jdk-list";
import Recover from "./project/recover-list";
import Tomcat from "./other/tomcat-list";
import ScriptTemplate from "@/pages/node/node-layout/other/script-list";
import ScriptLog from "@/pages/node/node-layout/other/script-log";
import NginxList from "./nginx/list";
import Cert from "./nginx/cert";
import WhiteList from "./system/white-list.vue";
import Cache from "./system/cache";
import Log from "./system/log.vue";
import Upgrade from "./system/upgrade.vue";
import ConfigFile from "./system/config-file.vue";
import { GUIDE_NODE_USED_KEY } from "../../../utils/const";
export default {
  components: {
    Welcome,
    ProjectList,
    JdkList,
    Recover,
    Tomcat,
    ScriptTemplate,
    NginxList,
    Cert,
    WhiteList,
    Cache,
    Log,
    Upgrade,
    ConfigFile,
    ScriptLog,
  },
  props: {
    node: {
      type: Object,
    },
  },
  data() {
    return {
      nodeMenuList: [],
      selectedKeys: [this.$route.query.id || "welcome"],
    };
  },
  computed: {
    ...mapGetters(["getGuideFlag"]),
    currentId() {
      return this.selectedKeys[0];
    },
    defaultOpenKey() {
      let keyList = [];
      // 引导开启且没指定打开某一项菜单时打开系统配置
      if (this.getGuideFlag && !this.$route.query.pId) {
        keyList = ["systemConfig"];
      } else if (this.$route.query.pId) {
        // 打开对应的父级菜单
        keyList = [this.$route.query.pId];
      }
      return keyList;
    },
  },
  watch: {
    getGuideFlag() {
      this.introGuide();
    },
  },
  created() {
    this.loadNodeMenu();
    setTimeout(() => {
      this.introGuide();
    }, 1000);
  },
  methods: {
    // 页面引导
    introGuide() {
      const used = localStorage.getItem(GUIDE_NODE_USED_KEY) === "true";
      // 如果要显示引导并且没有使用过
      if (this.getGuideFlag && !used) {
        this.$introJs
          .setOptions({
            hidePrev: true,
            steps: [
              {
                title: "Jpom 导航助手",
                element: document.querySelector(".ant-drawer-title"),
                intro: "这里是这个节点的名称和节点地址",
              },
              {
                title: "Jpom 导航助手",
                element: document.querySelector(".jpom-node-sider"),
                intro: "这里是这个节点的侧边栏菜单",
              },
              {
                title: "Jpom 导航助手",
                element: document.querySelector(".jpom-node-content"),
                intro: "这里是这个节点的主要内容展示区",
              },
              {
                title: "Jpom 导航助手",
                element: document.querySelector(".whitelistDirectory"),
                intro: "白名单目录是一个配置型菜单，里面配置的内容将会在</p><p><b>项目列表</b></br><b>Nginx 列表</b></br><b>证书管理</b></p>菜单里面作为选择项出现。",
              },
            ],
          })
          .start()
          .onexit(() => {
            localStorage.setItem(GUIDE_NODE_USED_KEY, "true");
          });
        return false;
      }
      this.$introJs.exit();
    },
    // 加载菜单
    loadNodeMenu() {
      getNodeMenu(this.node.id).then((res) => {
        if (res.code === 200) {
          const data = res.data.map((item) => {
            const childs =
              item.childs &&
              item.childs.map((sub) => {
                return { ...sub, pId: item.id };
              });
            return { ...item, childs };
          });
          this.nodeMenuList = data;
        }
      });
    },
    // 点击菜单
    handleMenuClick(id, pId) {
      this.selectedKeys = [id];
      this.$router.push({
        query: {
          ...this.$route.query,
          pId: pId,
          id: id,
        },
      });
    },
  },
};
</script>
<style scoped>
.sider {
  /* height: calc(100vh - 75px); */
  /* overflow-y: auto; */
}
.layout-content {
  /* min-height: calc(100vh); */
  /* overflow-y: auto; */
}
/* .node-layout {
  padding: 10px;
} */
</style>
