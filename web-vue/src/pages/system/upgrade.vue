<template>
  <div class="node-update full-content">
    <a-tabs type="card" default-active-key="1">
      <a-tab-pane key="1" tab="服务端"> <upgrade></upgrade></a-tab-pane>
      <a-tab-pane key="2" tab="所有节点(插件端)" force-render>
        <div class="header">
          <div class="left">
            <a-input class="search-input-item" v-model="listQuery['%name%']" placeholder="节点名称" />
            <a-input class="search-input-item" v-model="listQuery['%url%']" placeholder="节点地址" />
            <a-button type="primary" @click="refresh">搜索</a-button>
            |
            <a-select v-model="temp.protocol" placeholder="升级协议" class="search-input-item">
              <a-select-option value="WebSocket">WebSocket</a-select-option>
              <a-select-option value="Http">Http</a-select-option>
            </a-select>
            <a-button type="primary" @click="batchUpdate">批量更新</a-button>
          </div>
          <div class="right">
            <div class="title">
              Agent版本：{{ agentVersion | version }}
              <a-tag v-if="temp.upgrade" color="pink" @click="downloadRemoteEvent">新版本：{{ temp.newVersion }} </a-tag>
            </div>
            <div class="version">打包时间：{{ agentTimeStamp | version }}</div>
            <div class="action">
              <a-upload name="file" accept=".jar,.zip" action="" :showUploadList="false" :multiple="false" :before-upload="beforeUpload">
                <a-button type="primary">
                  <a-icon type="upload" />
                  上传新版本
                </a-button>
              </a-upload>
            </div>
          </div>
        </div>
        <div class="table-div">
          <a-table
            :loading="loading"
            :columns="columns"
            :data-source="listComputed"
            bordered
            rowKey="id"
            class="table"
            :pagination="this.pagination"
            @change="changePage"
            :row-selection="rowSelection"
          >
            <template slot="version" slot-scope="text">
              {{ text | version }}
            </template>
            <template slot="status" slot-scope="text">
              <div class="restarting" v-if="text && text.type === 'restarting'">
                <a-tooltip :title="text.data"> {{ text.data }} </a-tooltip>
              </div>
              <div class="uploading" v-if="text && text.type === 'uploading'">
                <div class="text">{{ text.percent === 100 ? "上传成功" : "正在上传文件" }}</div>
                <a-progress :percent="text.percent" />
              </div>
            </template>
            <template slot="operation" slot-scope="text, record">
              <a-button type="primary" @click="updateNodeHandler(record)">更新</a-button>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
<script>
import upgrade from "@/components/upgrade";
import { uploadAgentFile, downloadRemote, checkVersion, getNodeList } from "@/api/node";
import { mapGetters } from "vuex";
import { PAGE_DEFAULT_LIMIT, PAGE_DEFAULT_SIZW_OPTIONS, PAGE_DEFAULT_SHOW_TOTAL, PAGE_DEFAULT_LIST_QUERY } from "@/utils/const";

export default {
  components: {
    upgrade,
  },
  filters: {
    version(value) {
      return (value && value) || "未知";
    },
    status(value) {
      return (value && value) || "未知";
    },
  },
  data() {
    return {
      agentVersion: "",
      agentTimeStamp: "",
      websocket: null,
      groupFilter: undefined,
      loading: false,
      listQuery: Object.assign({}, PAGE_DEFAULT_LIST_QUERY),
      list: [],
      columns: [
        // { title: "节点 ID", dataIndex: "id", ellipsis: true, scopedSlots: { customRender: "id" } },
        { title: "节点名称", dataIndex: "name", ellipsis: true, scopedSlots: { customRender: "name" } },
        { title: "节点地址", dataIndex: "url", sorter: true, key: "url", ellipsis: true, scopedSlots: { customRender: "url" } },
        { title: "Agent版本号", dataIndex: "version", width: "100", ellipsis: true, scopedSlots: { customRender: "version" } },
        { title: "打包时间", dataIndex: "timeStamp", ellipsis: true, scopedSlots: { customRender: "timeStamp" } },
        { title: "状态", dataIndex: "status", ellipsis: true, scopedSlots: { customRender: "status" } },
        // {title: '自动更新', dataIndex: 'autoUpdate', ellipsis: true, scopedSlots: {customRender: 'autoUpdate'}},
        { title: "操作", dataIndex: "operation", width: 120, scopedSlots: { customRender: "operation" }, align: "left" },
      ],
      nodeVersion: {},
      nodeStatus: {},
      tableSelections: [],
      temp: {},
    };
  },
  computed: {
    ...mapGetters(["getLongTermToken"]),
    listComputed() {
      const data = [];
      this.list.forEach((item) => {
        if (item.group === this.groupFilter || this.groupFilter === undefined) {
          let itemData = this.nodeVersion[item.id];
          if (itemData) {
            try {
              itemData = JSON.parse(itemData);
              item.version = itemData.version;
              item.timeStamp = itemData.timeStamp;
            } catch (e) {
              item.version = itemData;
            }
          }

          item.status = this.nodeStatus[item.id];
          data.push(item);
        }
      });
      return data;
    },
    rowSelection() {
      return {
        onChange: this.tableSelectionChange,
        selectedRowKeys: this.tableSelections,
      };
    },
    socketUrl() {
      const protocol = location.protocol === "https:" ? "wss://" : "ws://";
      const domain = window.routerBase;
      const url = (domain + "/node_update").replace(new RegExp("//", "gm"), "/");
      return `${protocol}${location.host}${url}?userId=${this.getLongTermToken}&nodeId=system&type=nodeUpdate`;
    },
    pagination() {
      return {
        total: this.listQuery.total || 0,
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
  mounted() {
    this.getNodeList();
  },
  beforeDestroy() {
    this.socket && this.socket.close();
    clearInterval(this.heart);
  },
  methods: {
    initWebsocket(ids) {
      if (!this.socket || this.socket.readyState !== this.socket.OPEN || this.socket.readyState !== this.socket.CONNECTING) {
        this.socket = new WebSocket(this.socketUrl);
      }

      this.socket.onopen = () => {
        this.init(ids);
      };
      this.socket.onmessage = ({ data: socketData }) => {
        const msgObj = JSON.parse(socketData);
        if (msgObj) {
          const { command, data, nodeId } = msgObj;
          this[`${command}Result`] && this[`${command}Result`](data, nodeId);
        }
      };
      this.socket.onerror = (err) => {
        console.error(err);
        this.$notification.error({
          message: "web socket 错误,请检查是否开启 ws 代理,或者没有对应的权限",
        });
      };
    },
    checkAgentFileVersion() {
      // 获取是否有新版本
      checkVersion().then((res) => {
        if (res.code === 200) {
          let upgrade = res.data?.upgrade;
          if (upgrade) {
            //
            this.temp = { ...this.temp, upgrade: upgrade, newVersion: res.data.tagName };
            // this.temp.upgrade = upgrade;
            // this.temp.newVersion = ;
          }
        }
      });
    },
    init(ids) {
      this.sendMsg("getNodeList:" + ids.join(","));
      this.getAgentVersion();
      // 创建心跳，防止掉线
      this.heart && clearInterval(this.heart);
      this.heart = setInterval(() => {
        this.sendMsg("heart", {});
      }, 2000);
    },
    refresh() {
      if (this.socket) {
        this.socket.close();
      }
      this.nodeStatus = {};
      this.nodeVersion = {};
      this.getNodeList();
    },
    batchUpdate() {
      if (this.tableSelections.length === 0) {
        this.$notification.warning({
          message: "请选择要升级的节点",
        });
        return;
      }
      this.updateNode();
    },
    updateNodeHandler(record) {
      this.tableSelections = [record.id];
      this.updateNode();
    },
    tableSelectionChange(selectedRowKeys) {
      this.tableSelections = selectedRowKeys;
    },
    sendMsg(command, params) {
      this.socket.send(
        JSON.stringify({
          command,
          params,
        })
      );
    },
    getNodeList() {
      this.loading = true;
      getNodeList(this.listQuery).then((res) => {
        if (res.code === 200) {
          this.list = res.data.result;
          this.listQuery.total = res.data.total;
          let ids = this.list.map((item) => {
            return item.id;
          });
          if (ids.length > 0) {
            this.initWebsocket(ids);
          }
        }
        this.loading = false;
      });
    },
    getNodeListResult(data) {
      this.list = data;
    },
    getAgentVersion() {
      this.sendMsg("getAgentVersion");
    },
    getAgentVersionResult(data) {
      try {
        let newData = JSON.parse(data);
        this.agentVersion = newData.version;
        this.agentTimeStamp = newData.timeStamp;
      } catch (e) {
        this.agentVersion = data;
      }
      this.checkAgentFileVersion();
    },
    getVersionResult(data, nodeId) {
      this.nodeVersion = Object.assign({}, this.nodeVersion, {
        [nodeId]: data,
      });
    },
    onErrorResult(data) {
      this.$notification.warning({
        message: data,
      });
    },
    updateNode() {
      this.sendMsg("updateNode", {
        ids: this.tableSelections,
        protocol: this.temp.protocol,
      });
      this.tableSelections = [];
    },
    updateNodeResult(data, nodeId) {
      const { completeSize, size } = data;
      this.nodeStatus = Object.assign({}, this.nodeStatus, {
        [nodeId]: {
          ...data,
          type: "uploading",
          percent: Math.floor((completeSize / size) * 100),
        },
      });
    },
    restartResult(data, nodeId) {
      this.nodeStatus = Object.assign({}, this.nodeStatus, {
        [nodeId]: {
          type: "restarting",
          data: data,
        },
      });
    },
    beforeUpload(file) {
      const formData = new FormData();
      formData.append("file", file);
      uploadAgentFile(formData).then(({ code, msg }) => {
        if (code === 200) {
          this.$notification.success({ message: msg });
          this.getAgentVersion();
        } else {
          //this.$notification.error({ message: msg });
        }
      });
      return false;
    },
    // 下载远程最新文件
    downloadRemoteEvent() {
      downloadRemote().then((res) => {
        if (res.code === 200) {
          this.$notification.success({ message: res.msg });
          this.getAgentVersion();
        } else {
          //this.$notification.error({ message: res.msg });
        }
      });
    },
    // 分页、排序、筛选变化时触发
    changePage(pagination, filters, sorter) {
      this.listQuery.page = pagination.current;
      this.listQuery.limit = pagination.pageSize;
      if (sorter) {
        this.listQuery.order = sorter.order;
        this.listQuery.order_field = sorter.field;
      }
      this.getNodeList();
    },
  },
};
</script>

<style scoped lang="stylus">
.node-update {
  width 100%
  height 100%
  // display flex
  flex-direction column
  justify-content space-between


  .header {
    // height 60px;
    display flex;
    align-items center;
    margin-bottom: 15px;

    > div {
      //  width 50%;
    }

    .left {

      .group {
        width 200px
      }
    }

    .right {
      display flex
      justify-content flex-end
      align-items center;
      flex: 1;

      > div {
        padding: 0 10px
      }
    }
  }

  .table-div {
    // height calc(100% - 60px)
    // overflow auto

    .table {
      // height 100%

      .uploading {
        display flex
        flex-direction column
        align-items center
      }
    }
  }
}

.ant-select, .ant-btn {
  margin-right 20px
}
</style>
