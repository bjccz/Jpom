<template>
  <div class="full-content">
    <div ref="filter" class="filter">
      <a-select show-search option-filter-prop="children" v-model="listQuery.buildDataId" allowClear placeholder="请选择构建名称" class="filter-item">
        <a-select-option v-for="build in buildList" :key="build.id">{{ build.name }}</a-select-option>
      </a-select>
      <a-select show-search option-filter-prop="children" v-model="listQuery.status" allowClear placeholder="请选择状态" class="filter-item">
        <a-select-option v-for="(val, key) in statusMap" :key="key">{{ val }}</a-select-option>
      </a-select>
      <a-select show-search option-filter-prop="children" v-model="listQuery.triggerBuildType" allowClear placeholder="请选择触发类型" class="filter-item">
        <a-select-option v-for="(val, key) in triggerBuildTypeMap" :key="key">{{ val }}</a-select-option>
      </a-select>
      <a-range-picker class="filter-item" :show-time="{ format: 'HH:mm:ss' }" format="YYYY-MM-DD HH:mm:ss" @change="onchangeTime" />
      <a-tooltip title="按住 Ctr 或者 Alt 键点击按钮快速回到第一页">
        <a-button type="primary" @click="loadData">搜索</a-button>
      </a-tooltip>
      <a-tooltip>
        <template slot="title">
          <div>构建历史是用于记录每次构建的信息,可以保留构建产物信息,构建日志。同时还可以快速回滚发布</div>
          <div>如果不需要保留较多构建历史信息可以到服务端修改构建相关配置参数</div>
          <div>构建历史可能占有较多硬盘空间,建议根据实际情况配置保留个数</div>
        </template>
        <a-icon type="question-circle" theme="filled" />
      </a-tooltip>
      <!-- <a-button type="primary" @click="handleFilter">刷新</a-button> -->
    </div>
    <!-- 数据表格 -->
    <a-table :data-source="list" :loading="loading" :columns="columns" :pagination="pagination" bordered :rowKey="(record, index) => index" @change="change">
      <a-tooltip slot="buildName" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <a-tooltip slot="buildNumberId" slot-scope="text, record" placement="topLeft" :title="text + ' ( 点击查看日志 ) '">
        <a-tag color="#108ee9" @click="handleBuildLog(record)">#{{ text }}</a-tag>
      </a-tooltip>
      <template slot="status" slot-scope="text" placement="topleft" :title="text">
        <span>{{ statusMap[text] }}</span>
      </template>
      <template slot="releaseMethod" slot-scope="text" placement="topleft" :title="text">
        <span>{{ releaseMethodMap[text] }}</span>
      </template>
      <template slot="triggerBuildType" slot-scope="text" placement="topleft" :title="text">
        <span>{{ triggerBuildTypeMap[text] }}</span>
      </template>

      <template slot="startTime" slot-scope="text, record" placement="topLeft">
        <a-tooltip :title="`开始时间：${parseTime(record.startTime)}，${record.endTime ? '结束时间：' + parseTime(record.endTime) : ''}`">
          <span>{{ parseTime(record.startTime) }}</span>
        </a-tooltip>
      </template>
      <template slot="operation" slot-scope="text, record">
        <a-button type="primary" :disabled="!record.hasLog" @click="handleDownload(record)"><a-icon type="download" />日志</a-button>
        <a-button type="primary" :disabled="!record.hashFile" @click="handleFile(record)"><a-icon type="download" />产物</a-button>
        <a-dropdown>
          <a class="ant-dropdown-link" @click="(e) => e.preventDefault()">
            更多
            <a-icon type="down" />
          </a>
          <a-menu slot="overlay">
            <a-menu-item>
              <a-button :disabled="!record.hashFile || record.releaseMethod === 0" type="danger" @click="handleRollback(record)">回滚 </a-button>
            </a-menu-item>
            <a-menu-item>
              <a-button type="danger" @click="handleDelete(record)">删除</a-button>
            </a-menu-item>
          </a-menu>
        </a-dropdown>
      </template>
    </a-table>
    <!-- 构建日志 -->
    <a-modal :width="'80vw'" v-model="buildLogVisible" title="构建日志" :footer="null" :maskClosable="false" @cancel="closeBuildLogModel">
      <build-log v-if="buildLogVisible" :temp="temp" />
    </a-modal>
  </div>
</template>
<script>
import BuildLog from "./log";
import { geteBuildHistory, getBuildListAll, downloadBuildLog, rollback, deleteBuildHistory, releaseMethodMap, statusMap, downloadBuildFile, triggerBuildTypeMap } from "@/api/build-info";
import { parseTime } from "@/utils/time";

import { PAGE_DEFAULT_LIMIT, PAGE_DEFAULT_SIZW_OPTIONS, PAGE_DEFAULT_SHOW_TOTAL, PAGE_DEFAULT_LIST_QUERY } from "@/utils/const";
export default {
  components: {
    BuildLog,
  },
  data() {
    return {
      releaseMethodMap: releaseMethodMap,
      triggerBuildTypeMap: triggerBuildTypeMap,
      loading: false,
      list: [],
      buildList: [],
      total: 0,
      listQuery: Object.assign({}, PAGE_DEFAULT_LIST_QUERY),
      statusMap: statusMap,
      temp: {},
      buildLogVisible: false,
      columns: [
        { title: "构建名称", dataIndex: "buildName", /*width: 120,*/ ellipsis: true, scopedSlots: { customRender: "buildName" } },
        { title: "构建 ID", dataIndex: "buildNumberId", width: 100, ellipsis: true, scopedSlots: { customRender: "buildNumberId" } },
        { title: "状态", dataIndex: "status", width: 120, ellipsis: true, scopedSlots: { customRender: "status" } },
        { title: "触发类型", dataIndex: "triggerBuildType", width: 100, ellipsis: true, scopedSlots: { customRender: "triggerBuildType" } },
        {
          title: "开始时间",
          dataIndex: "startTime",
          sorter: true,
          scopedSlots: { customRender: "startTime" },
          width: 170,
        },
        // {
        //   title: "结束时间",
        //   dataIndex: "endTime",
        //   sorter: true,
        //   customRender: (text) => {
        //     return parseTime(text);
        //   },
        //   width: 170,
        // },
        { title: "发布方式", dataIndex: "releaseMethod", width: 100, ellipsis: true, scopedSlots: { customRender: "releaseMethod" } },
        { title: "构建人", dataIndex: "modifyUser", width: 150, ellipsis: true, scopedSlots: { customRender: "modifyUser" } },
        { title: "操作", dataIndex: "operation", scopedSlots: { customRender: "operation" }, width: 280, fixed: "right" },
      ],
    };
  },
  computed: {
    pagination() {
      return {
        total: this.listQuery.total,
        current: this.listQuery.page || 1,
        pageSize: this.listQuery.limit || PAGE_DEFAULT_LIMIT,
        pageSizeOptions: PAGE_DEFAULT_SIZW_OPTIONS,
        showSizeChanger: true,
        showTotal: (total) => {
          return PAGE_DEFAULT_SHOW_TOTAL(total, this.listQuery);
        },
      };
    },
  },
  created() {
    this.loadBuildList();
    this.loadData();
  },
  methods: {
    parseTime: parseTime,
    // 加载构建列表
    loadBuildList() {
      getBuildListAll().then((res) => {
        if (res.code === 200) {
          this.buildList = res.data;
        }
      });
    },
    // 加载数据
    loadData(pointerEvent) {
      this.listQuery.page = pointerEvent?.altKey || pointerEvent?.ctrlKey ? 1 : this.listQuery.page;

      this.loading = true;

      geteBuildHistory(this.listQuery).then((res) => {
        if (res.code === 200) {
          this.list = res.data.result;
          this.listQuery.total = res.data.total;
        }
        this.loading = false;
      });
    },
    // 分页、排序、筛选变化时触发
    change(pagination, f, sorter) {
      this.listQuery.page = pagination.current;
      this.listQuery.limit = pagination.pageSize;
      if (sorter) {
        this.listQuery.order = sorter.order;
        this.listQuery.order_field = sorter.field;
      }
      this.loadData();
    },
    // 选择时间
    onchangeTime(value, dateString) {
      if (!dateString[0] || !dateString[1]) {
        this.listQuery.startTime = "";
      } else {
        this.listQuery.startTime = `${dateString[0]} ~ ${dateString[1]}`;
      }
    },

    // 下载构建日志
    handleDownload(record) {
      window.open(downloadBuildLog(record.id), "_self");
    },

    // 下载构建产物
    handleFile(record) {
      window.open(downloadBuildFile(record.id), "_self");
    },

    // 回滚
    handleRollback(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的要回滚该构建历史记录么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 重新发布
          rollback(record.id).then((res) => {
            if (res.code === 200) {
              this.$notification.success({
                message: res.msg,
              });
              this.loadData();
            }
          });
        },
      });
    },
    // 删除
    handleDelete(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的要删除构建历史记录么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 删除
          deleteBuildHistory(record.id).then((res) => {
            if (res.code === 200) {
              this.$notification.success({
                message: res.msg,
              });
              this.loadData();
            }
          });
        },
      });
    },
    // 查看构建日志
    handleBuildLog(record) {
      this.temp = {
        id: record.buildDataId,
        buildId: record.buildNumberId,
      };
      this.buildLogVisible = true;
    },
    // 关闭日志对话框
    closeBuildLogModel() {
      this.loadData();
    },
  },
};
</script>
<style scoped>
.filter {
  margin-bottom: 10px;
}
.ant-btn {
  margin-right: 10px;
}
.filter-item {
  width: 150px;
  margin-right: 10px;
}
</style>
