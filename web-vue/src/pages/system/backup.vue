<template>
  <div class="full-content">
    <div ref="filter" class="filter">
      <a-input v-model="listQuery['%name%']" placeholder="请输入备份名称" class="filter-item" />
      <a-select v-model="listQuery.backupType" allowClear placeholder="请选择备份类型" class="filter-item">
        <a-select-option v-for="backupType in backupTypeList" :key="backupType.key">{{ backupType.value }}</a-select-option>
      </a-select>
      <a-tooltip title="按住 Ctr 或者 Alt 键点击按钮快速回到第一页">
        <a-button type="primary" @click="loadData">搜索</a-button>
      </a-tooltip>
      <a-button type="primary" @click="handleAdd">创建备份</a-button>
      <a-button type="primary" @click="handleSqlUpload">导入备份</a-button>
    </div>
    <!-- 表格 -->
    <a-table :loading="loading" :columns="columns" :data-source="list" bordered rowKey="id" @change="changePage" :pagination="pagination">
      <a-tooltip slot="name" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <template slot="backupType" slot-scope="text" placement="topleft" :title="text">
        <span>{{ backupTypeMap[text] }}</span>
      </template>
      <a-tooltip slot="fileSize" slot-scope="text" placement="topLeft" :title="`${renderSizeFormat(text)}`">
        <a-tag color="#108ee9">{{ renderSizeFormat(text) }}</a-tag>
      </a-tooltip>
      <a-tooltip slot="status" slot-scope="text" placement="topLeft" :title="backupStatusMap[text]">
        <span>{{ backupStatusMap[text] }}</span>
      </a-tooltip>
      <a-tooltip slot="sha1Sum" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <a-tooltip slot="filePath" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <template slot="operation" slot-scope="text, record">
        <a-button type="primary" @click="handleDownload(record)">下载</a-button>
        <a-button type="danger" @click="handleDelete(record)">删除</a-button>
        <a-button type="danger" :disabled="record.status !== 1" @click="handleRestore(record)">还原备份</a-button>
      </template>
    </a-table>
    <!-- 创建备份信息区 -->
    <a-modal v-model="createBackupVisible" title="创建备份信息" @ok="handleCreateBackupOk" width="600px" :maskClosable="false">
      <a-form-model ref="editBackupForm" :rules="rules" :model="temp" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-model-item label="备份类型" prop="backupType">
          <a-radio-group v-model="temp.backupType" name="backupType">
            <a-radio v-for="item in backupTypeList" :disabled="item.disabled" :key="item.key" :value="item.key">{{ item.value }}</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <!-- 部分备份 -->
        <a-form-model-item v-if="temp.backupType === 1" label="勾选数据表" prop="tableNameList" class="feature jpom-role">
          <a-transfer :data-source="tableNameList" show-search :filter-option="filterOption" :target-keys="targetKeys" :render="(item) => item.title" @change="handleChange" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <!-- 上传 SQL 备份文件 -->
    <a-modal v-model="uploadSqlFileVisible" width="300px" title="上传 SQL 文件" :footer="null" :maskClosable="true">
      <a-upload :file-list="uploadFileList" :remove="handleSqlRemove" :before-upload="beforeSqlUpload" accept=".sql">
        <a-button><a-icon type="upload" />选择 SQL 文件</a-button>
      </a-upload>
      <!-- <br />
      <a-radio-group v-model="backupType" name="backupType">
        <a-radio :value="0">全量备份</a-radio>
        <a-radio :value="1">部分备份</a-radio>
      </a-radio-group>
      <br /> -->
      <br />
      <el-progress :text-inside="true" :stroke-width="18" :percentage="percentage" status="success"></el-progress>
      <br />
      <a-button type="primary" :disabled="fileUploadDisabled" @click="startSqlUpload">开始上传</a-button>
      <a-tag color="green" :visible="successSize !== 0" :closable="true" class="successTag"> 上传成功: {{ successSize }} 个文件! </a-tag>
    </a-modal>
  </div>
</template>
<script>
import { mapGetters } from "vuex";
import { getBackupList, getTableNameList, createBackup, downloadBackupFile, deleteBackup, restoreBackup, uploadBackupFile, backupTypeMap, backupTypeArray, backupStatusMap } from "@/api/backup-info";
import { parseTime, renderSize } from "@/utils/time";
import { PAGE_DEFAULT_LIMIT, PAGE_DEFAULT_SIZW_OPTIONS, PAGE_DEFAULT_SHOW_TOTAL, PAGE_DEFAULT_LIST_QUERY } from "@/utils/const";

export default {
  components: {},
  data() {
    return {
      backupTypeMap: backupTypeMap,
      backupStatusMap: backupStatusMap,
      loading: false,
      listQuery: Object.assign({}, PAGE_DEFAULT_LIST_QUERY),
      backupTypeList: backupTypeArray,
      list: [],
      total: 0,
      tableNameList: [],
      targetKeys: [],
      uploadFileList: [],
      temp: {},
      createBackupVisible: false,
      uploadSqlFileVisible: false,
      // 是否是上传状态
      uploading: false,
      percentage: 0,
      backupType: 0,
      successSize: 0,
      columns: [
        { title: "备份名称", dataIndex: "name", width: 150, ellipsis: true, scopedSlots: { customRender: "name" } },
        { title: "备份类型", dataIndex: "backupType", width: 100, ellipsis: true, scopedSlots: { customRender: "backupType" } },
        {
          title: "文件大小",
          dataIndex: "fileSize",
          width: 100,
          // ellipsis: true,
          scopedSlots: { customRender: "fileSize" },
        },
        { title: "状态", dataIndex: "status", width: 100, ellipsis: true, scopedSlots: { customRender: "status" } },
        {
          title: "SHA1",
          dataIndex: "sha1Sum",
          // width: 80,
          ellipsis: true,
          scopedSlots: { customRender: "sha1Sum" },
        },
        {
          title: "文件地址",
          dataIndex: "filePath",
          // width: 150,
          ellipsis: true,
          scopedSlots: { customRender: "filePath" },
        },
        {
          title: "备份时间",
          dataIndex: "createTimeMillis",
          customRender: (text) => {
            if (!text) {
              return "";
            }
            return parseTime(text);
          },
          width: 180,
        },
        {
          title: "操作",
          dataIndex: "operation",
          width: 280,
          scopedSlots: { customRender: "operation" },
          align: "left",
          fixed: "right",
        },
      ],
      rules: {
        name: [{ required: true, message: "Please input build name", trigger: "blur" }],
        script: [{ required: true, message: "Please input build script", trigger: "blur" }],
        resultDirFile: [{ required: true, message: "Please input build target path", trigger: "blur" }],
        releasePath: [{ required: true, message: "Please input release path", trigger: "blur" }],
      },
    };
  },
  computed: {
    ...mapGetters(["getGuideFlag"]),
    // 计算上传文件是否禁用
    fileUploadDisabled() {
      return this.uploadFileList.length === 0 || this.uploading;
    },
    // 分页
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
  created() {
    console.log(backupTypeMap);
    this.loadData();
  },
  methods: {
    // 格式化文件大小
    renderSizeFormat(value) {
      return renderSize(value);
    },
    // 加载数据
    loadData(pointerEvent) {
      this.loading = true;
      this.listQuery.page = pointerEvent?.altKey || pointerEvent?.ctrlKey ? 1 : this.listQuery.page;

      getBackupList(this.listQuery).then((res) => {
        if (res.code === 200) {
          this.list = res.data.result;
          this.listQuery.total = res.data.total;
        }
        this.loading = false;
      });
    },
    // 加载数据库表名列表
    loadTableNameList() {
      this.tableNameList = [];
      getTableNameList().then((res) => {
        if (res.code === 200) {
          res.data.forEach((element) => {
            this.tableNameList.push({ key: element, title: element });
          });
        }
      });
    },

    // 穿梭框筛选
    filterOption(inputValue, option) {
      return option.title.indexOf(inputValue) > -1;
    },
    // 穿梭框 change
    handleChange(targetKeys) {
      this.targetKeys = targetKeys;
    },
    // 创建备份
    handleAdd() {
      this.targetKeys = [];
      this.temp = {
        backupType: 0,
      };
      this.loadTableNameList();
      this.createBackupVisible = true;
    },
    // 提交节点数据
    handleCreateBackupOk() {
      // 检验表单
      this.$refs["editBackupForm"].validate((valid) => {
        if (!valid) {
          return false;
        }
        // 提交数据
        createBackup(this.targetKeys).then((res) => {
          if (res.code === 200) {
            // 成功
            this.$notification.success({
              message: res.msg,
            });
            this.$refs["editBackupForm"].resetFields();
            this.createBackupVisible = false;
            this.loadData();
          }
        });
      });
    },
    // 下载
    handleDownload(record) {
      window.open(downloadBackupFile(record.id), "_self");
    },
    // 删除
    handleDelete(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的要删除备份信息么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 删除
          deleteBackup(record.id).then((res) => {
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
    // 还原备份
    handleRestore(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的要还原备份信息么？还原过程中不能操作哦...",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 还原
          restoreBackup(record.id).then((res) => {
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
    // 上传压缩文件
    handleSqlUpload() {
      this.successSize = 0;
      this.uploadSqlFileVisible = true;
    },
    handleSqlRemove() {
      this.uploadFileList = [];
    },
    beforeSqlUpload(file) {
      this.uploadFileList = [file];
      return false;
    },
    // 开始上传 SQL 文件
    startSqlUpload() {
      this.$notification.info({
        message: "正在上传文件，请稍后...",
      });
      // 设置上传状态
      this.uploading = true;
      const timer = setInterval(() => {
        this.percentage = this.percentage > 99 ? 99 : this.percentage + 1;
      }, 1000);

      // 上传文件
      const file = this.uploadFileList[0];
      const formData = new FormData();
      formData.append("file", file);
      formData.append("backupType", this.backupType);
      // 上传文件
      uploadBackupFile(formData).then((res) => {
        if (res.code === 200) {
          this.$notification.success({
            message: res.msg,
          });
          this.successSize++;
          this.percentage = 100;
          setTimeout(() => {
            this.percentage = 0;
            this.uploading = false;
            clearInterval(timer);
            this.uploadFileList = [];
            this.loadData();
          }, 1000);
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

.btn-add {
  margin-left: 10px;
  margin-right: 0;
}

.successTag {
  height: 32px;
  line-height: 30px;
}
</style>
