<template>
  <div>
    <a-input id="build-log-textarea" v-model="logText" type="textarea" class="console" readOnly style="resize: none; height: 70vh" />
  </div>
</template>
<script>
// import { loadBuildLog } from '../../api/build';
import { loadBuildLog } from "../../api/build-info";
export default {
  props: {
    temp: {
      type: Object,
    },
  },
  data() {
    return {
      logTimer: null,
      logText: "loading...",
      line: 1,
    };
  },
  beforeDestroy() {
    if (this.logTimer) {
      clearInterval(this.logTimer);
    }
  },
  mounted() {
    this.loadData();
  },
  methods: {
    // 加载日志内容
    loadData() {
      // 加载构建日志
      this.logTimer = setInterval(() => {
        const params = {
          id: this.temp.id,
          buildId: this.temp.buildId,
          line: this.line,
        };
        loadBuildLog(params).then((res) => {
          if (res.code === 200) {
            // 停止请求
            if (res.data.run === false) {
              clearInterval(this.logTimer);
            }
            // 更新日志
            if (this.logText === "loading...") {
              this.logText = "";
            }
            let lines = res.data.dataLines;
            lines.forEach((element) => {
              this.logText += `${element}\r\n`;
            });
            this.line = res.data.line;
            if (lines.length) {
              // 自动滚动到底部
              this.$nextTick(() => {
                setTimeout(() => {
                  const textarea = document.getElementById("build-log-textarea");
                  textarea.scrollTop = textarea.scrollHeight;
                }, 100);
              });
            }
          }
        });
      }, 2000);
    },
  },
};
</script>
<style scoped>
.console {
  padding: 5px;
  color: #fff;
  font-size: 14px;
  background-color: black;
  width: 100%;
  overflow-y: auto;
  border: 1px solid #e2e2e2;
  border-radius: 5px 5px;
}
</style>
