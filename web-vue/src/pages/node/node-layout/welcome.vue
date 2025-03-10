<template>
  <div class="node-full-content">
    <!-- 历史监控数据 -->
    <a-button v-show="node.cycle && node.cycle !== 0" type="primary" @click="handleHistory">历史监控图表</a-button>
    <a-divider>图表</a-divider>
    <!-- top 图表 -->
    <div id="top-chart">loading...</div>
    <a-divider>进程监控表格</a-divider>
    <!-- 进程表格数据 -->
    <div ref="filter" class="filter">
      <a-select @change="loadNodeProcess" v-model="processName" allowClear placeholder="进程类型" class="filter-item">
        <a-select-option value="java">java</a-select-option>
        <a-select-option value="python">python</a-select-option>
        <a-select-option value="mysql">mysql</a-select-option>
        <a-select-option value="php">php</a-select-option>
        <a-select-option value="docker">docker</a-select-option>
      </a-select>
      <!-- <a-button type="primary" @click="loadData">切换</a-button> -->
    </div>
    <a-table :loading="loading" :columns="columns" :data-source="processList" bordered rowKey="pid" class="node-table" :pagination="false">
      <a-tooltip slot="port" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <a-tooltip slot="user" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <a-tooltip slot="jpomName" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <template slot="operation" slot-scope="text, record">
        <a-button type="primary" @click="kill(record)">Kill</a-button>
      </template>
    </a-table>
    <!-- 历史监控 -->
    <a-modal v-model="monitorVisible" width="75%" title="历史监控图表" :footer="null" :maskClosable="false">
      <div ref="filter" class="filter">
        <a-range-picker class="filter-item" :show-time="{ format: 'HH:mm:ss' }" format="YYYY-MM-DD HH:mm:ss" @change="onchange" />
        <a-button type="primary" @click="handleFilter">搜索</a-button>
      </div>
      <div id="history-chart">loading...</div>
    </a-modal>
  </div>
</template>
<script>
import { getNodeTop, getProcessList, killPid, nodeMonitorData } from "../../../api/node";
import echarts from "echarts";

export default {
  props: {
    node: {
      type: Object,
    },
  },
  data() {
    return {
      topData: {},
      topChartTimer: null,
      loading: false,
      processList: [],
      monitorVisible: false,
      timeRange: "",
      historyData: [],
      processName: "java",
      columns: [
        { title: "进程 ID", dataIndex: "pid", width: 100, ellipsis: true, scopedSlots: { customRender: "pid" } },
        { title: "进程名称", dataIndex: "command", width: 150, ellipsis: true, scopedSlots: { customRender: "command" } },
        { title: "端口", dataIndex: "port", width: 100, ellipsis: true, scopedSlots: { customRender: "port" } },
        { title: "所有者", dataIndex: "user", width: 100, ellipsis: true, scopedSlots: { customRender: "user" } },
        { title: "项目名称", dataIndex: "jpomName", width: 150, ellipsis: true, scopedSlots: { customRender: "jpomName" } },
        { title: "物理内存", dataIndex: "res", width: 100, ellipsis: true },
        { title: "进程状态", dataIndex: "status", width: 100, ellipsis: true },
        { title: "占用CPU", dataIndex: "cpu", width: 100, ellipsis: true },
        { title: "物理内存百分比", dataIndex: "mem", width: 140, ellipsis: true },
        { title: "虚拟内存", dataIndex: "virt", width: 100, ellipsis: true },
        { title: "共享内存", dataIndex: "shr", width: 100, ellipsis: true },
        { title: "操作", dataIndex: "operation", scopedSlots: { customRender: "operation" }, width: 100, fixed: "right" },
      ],
    };
  },
  mounted() {
    this.initData();
  },
  destroyed() {
    clearInterval(this.topChartTimer);
  },
  methods: {
    // 初始化页面
    initData() {
      if (this.topChartTimer == null) {
        this.loadNodeTop();
        this.loadNodeProcess();
        // 计算多久时间绘制图表
        // const millis = this.node.cycle < 30000 ? 30000 : this.node.cycle;
        // console.log(millis);
        this.topChartTimer = setInterval(() => {
          this.loadNodeTop();
          this.loadNodeProcess();
        }, 20000);
      }
    },
    // 请求 top 命令绘制图表
    loadNodeTop() {
      getNodeTop(this.node.id).then((res) => {
        if (res.code === 200) {
          this.topData = res.data;
          this.drawTopChart();
        }
      });
    },
    generateChart(data) {
      let cpuItem = {
        name: "cpu占用",
        type: "line",
        data: [],
        showSymbol: false,
        // 设置折线为曲线
        smooth: true,
      };
      let diskItem = {
        name: "磁盘占用",
        type: "line",
        data: [],
        showSymbol: false,
        smooth: true,
      };
      let memoryItem = {
        name: "内存占用(累计)",
        type: "line",
        data: [],
        showSymbol: false,
        smooth: true,
      };
      let memoryUsedItem = {
        name: "内存占用",
        type: "line",
        data: [],
        showSymbol: false,
        smooth: true,
      };
      data.series.forEach((item) => {
        cpuItem.data.push(parseFloat(item.cpu));
        diskItem.data.push(parseFloat(item.disk));
        memoryItem.data.push(parseFloat(item.memory));
        if (item.memoryUsed) {
          memoryUsedItem.data.push(parseFloat(item.memoryUsed));
        }
      });
      let series = [cpuItem, memoryItem, diskItem];
      if (memoryUsedItem.data.length > 0) {
        series.push(memoryUsedItem);
      }
      let legends = series.map((data) => {
        return data.name;
      });
      // 指定图表的配置项和数据
      return {
        title: {
          text: "系统 Top 监控",
        },
        tooltip: {
          trigger: "axis",
        },
        legend: {
          data: legends,
        },
        grid: {
          left: "1%",
          right: "2%",
          bottom: "1%",
          containLabel: true,
        },
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: data.scales,
        },
        calculable: true,
        yAxis: {
          type: "value",
          axisLabel: {
            // 设置y轴数值为%
            formatter: "{value} %",
          },
          max: 100,
        },
        dataZoom: [{ type: "inside" }, { type: "slider" }],
        series: series,
      };
    },
    // 绘制 top 图表
    drawTopChart() {
      let topChartDom = document.getElementById("top-chart");
      if (!topChartDom) {
        return;
      }
      let option = this.generateChart(this.topData);
      // 绘制图表
      const topChart = echarts.init(topChartDom);
      topChart.setOption(option);
    },
    // 加载节点进程列表
    loadNodeProcess() {
      this.loading = true;
      getProcessList({
        nodeId: this.node.id,
        processName: this.processName,
      }).then((res) => {
        if (res.code === 200) {
          this.processList = res.data;
        } else {
          this.processList = [];
        }
        this.loading = false;
      });
    },
    // kill pid
    kill(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的要 Kill 这个进程么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // kill
          const params = {
            nodeId: this.node.id,
            pid: record.pid,
          };
          killPid(params).then((res) => {
            if (res.code === 200) {
              this.$notification.success({
                message: res.msg,
              });
              this.loadNodeProcess();
            }
          });
        },
      });
    },
    // 历史图表
    handleHistory() {
      this.monitorVisible = true;
      this.$nextTick(() => {
        this.handleFilter();
      });
    },
    // 刷新
    handleFilter() {
      const params = {
        nodeId: this.node.id,
        time: this.timeRange,
      };
      // 加载数据
      nodeMonitorData(params).then((res) => {
        if (res.code === 200) {
          this.historyData = res.data;
          this.drawHistoryChart();
        }
      });
    },
    // 选择时间
    onchange(value, dateString) {
      this.timeRange = `${dateString[0]} ~ ${dateString[1]}`;
    },
    // 画历史图表
    drawHistoryChart() {
      let historyChartDom = document.getElementById("history-chart");
      if (!historyChartDom) {
        return;
      }
      let option = this.generateChart(this.historyData);
      // 绘制图表
      const historyChart = echarts.init(historyChartDom);
      historyChart.setOption(option);
    },
  },
};
</script>
<style scoped>
#top-chart {
  height: calc((100vh - 70px) / 2);
}

.filter {
  margin-bottom: 10px;
}

.filter-item {
  width: 150px;
  margin-right: 10px;
}

#history-chart {
  height: 60vh;
}
</style>
