import Vue from "vue";
import App from "./App.vue";

import Antd from "ant-design-vue";
import "ant-design-vue/dist/antd.css";
import "./assets/reset.css";
import { Tree, Progress, Loading } from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
const introJs = require("intro.js");
import "intro.js/introjs.css";
// import 'intro.js/themes/introjs-flattener.css';
import "@/assets/intro-custom-themes.css";

import router from "./router";
import store from "./store";
import "./router/auth";

// debug routerBase
window.routerBase = window.routerBase === "<routerBase>" ? "" : window.routerBase;

// introJs 按钮中文
const intro = introJs();
intro.setOptions({
  prevLabel: "上一步",
  nextLabel: "下一步",
  // skipLabel: "跳过",
  doneLabel: "结束引导",
});

Vue.config.productionTip = false;
Vue.prototype.$loading = Loading;
Vue.prototype.$introJs = intro;
Vue.use(Antd);
Vue.use(Tree);
Vue.use(Progress);

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
