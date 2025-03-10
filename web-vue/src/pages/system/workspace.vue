<template>
  <div class="full-content">
    <div ref="filter" class="filter">
      <a-input v-model="listQuery['%name%']" placeholder="工作空间名称" allowClear class="search-input-item" />

      <a-tooltip title="按住 Ctr 或者 Alt 键点击按钮快速回到第一页">
        <a-button type="primary" @click="loadData">搜索</a-button>
      </a-tooltip>
      <a-button type="primary" @click="handleAdd">新增</a-button>
    </div>
    <!-- 数据表格 -->
    <a-table :data-source="list" :loading="loading" :columns="columns" :pagination="this.pagination" bordered @change="changePage" :rowKey="(record, index) => index">
      <a-tooltip slot="description" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <a-tooltip slot="name" slot-scope="text" placement="topLeft" :title="text">
        <span>{{ text }}</span>
      </a-tooltip>
      <template slot="operation" slot-scope="text, record">
        <a-button type="primary" @click="handleEdit(record)">编辑</a-button>
        <a-button type="primary" @click="viewEnvVar(record)">变量</a-button>
        <a-button type="danger" @click="handleDelete(record)">删除</a-button>
      </template>
    </a-table>
    <!-- 编辑区 -->
    <a-modal v-model="editVisible" title="编辑工作空间" @ok="handleEditOk" :maskClosable="false">
      <a-form-model ref="editForm" :rules="rules" :model="temp" :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
        <a-form-model-item label="名称" prop="name">
          <a-input v-model="temp.name" placeholder="工作空间名称" />
        </a-form-model-item>

        <a-form-model-item label="描述" prop="description">
          <a-input v-model="temp.description" type="textarea" :rows="5" placeholder="工作空间描述" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <!-- 环境变量 -->
    <a-modal v-model="envVarListVisible" title="环境变量" width="80vw" :footer="null" :maskClosable="false">
      <div ref="filter" class="filter">
        <a-input v-model="envVarListQuery['%name%']" placeholder="名称" allowClear class="search-input-item" />
        <a-input v-model="envVarListQuery['%value%']" placeholder="值" allowClear class="search-input-item" />
        <a-input v-model="envVarListQuery['%description%']" placeholder="描述" allowClear class="search-input-item" />
        <a-button type="primary" @click="loadDataEnvVar">搜索</a-button>
        <a-button type="primary" @click="addEnvVar">新增</a-button>
      </div>
      <!-- 数据表格 -->
      <a-table :data-source="envVarList" :loading="envVarLoading" :columns="envVarColumns" :pagination="envVarPagination" @change="changeListeEnvVar" bordered :rowKey="(record, index) => index">
        <a-tooltip slot="value" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text }}</span>
        </a-tooltip>
        <a-tooltip slot="name" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text }}</span>
        </a-tooltip>
        <a-tooltip slot="description" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text }}</span>
        </a-tooltip>
        <template slot="operation" slot-scope="text, record">
          <a-button type="primary" @click="handleEnvEdit(record)">编辑</a-button>

          <a-button type="danger" @click="handleEnvDelete(record)">删除</a-button>
        </template>
      </a-table>
    </a-modal>
    <!-- 环境变量编辑区 -->
    <a-modal v-model="editEnvVisible" title="编辑环境变量" @ok="handleEnvEditOk" :maskClosable="false">
      <a-form-model ref="editEnvForm" :rules="rulesEnv" :model="temp" :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
        <a-form-model-item label="名称" prop="name">
          <a-input v-model="temp.name" placeholder="变量名称" />
        </a-form-model-item>
        <a-form-model-item label="值" prop="value">
          <a-input v-model="temp.value" type="textarea" :rows="5" placeholder="变量值" />
        </a-form-model-item>
        <a-form-model-item label="描述" prop="description">
          <a-input v-model="temp.description" type="textarea" :rows="5" placeholder="变量描述" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>
<script>
import { getWorkSpaceList, editWorkSpace, deleteWorkspace, getWorkspaceEnvList, editWorkspaceEnv, deleteWorkspaceEnv } from "@/api/workspace";
import { parseTime } from "@/utils/time";
import { PAGE_DEFAULT_LIMIT, PAGE_DEFAULT_SIZW_OPTIONS, PAGE_DEFAULT_SHOW_TOTAL, PAGE_DEFAULT_LIST_QUERY } from "@/utils/const";

export default {
  data() {
    return {
      loading: false,
      envVarLoading: false,
      list: [],
      envVarList: [],
      envVarListQuery: Object.assign({}, PAGE_DEFAULT_LIST_QUERY),
      listQuery: Object.assign({}, PAGE_DEFAULT_LIST_QUERY),
      editVisible: false,
      envVarListVisible: false,
      editEnvVisible: false,
      temp: {},
      columns: [
        { title: "名称", dataIndex: "name", ellipsis: true, scopedSlots: { customRender: "name" } },
        { title: "描述", dataIndex: "description", ellipsis: true, scopedSlots: { customRender: "description" } },
        {
          title: "修改时间",
          dataIndex: "modifyTimeMillis",

          customRender: (text) => {
            if (!text) {
              return "";
            }
            return parseTime(text);
          },
          sorter: true,
          width: 180,
        },
        { title: "操作", dataIndex: "operation", scopedSlots: { customRender: "operation" }, width: "300px" },
      ],
      envVarColumns: [
        { title: "名称", dataIndex: "name", ellipsis: true, scopedSlots: { customRender: "name" } },
        { title: "值", dataIndex: "value", ellipsis: true, scopedSlots: { customRender: "value" } },
        { title: "描述", dataIndex: "description", ellipsis: true, scopedSlots: { customRender: "description" } },
        { title: "修改人", dataIndex: "modifyUser", ellipsis: true, scopedSlots: { customRender: "modifyUser" }, width: 120 },
        {
          title: "修改时间",
          dataIndex: "modifyTimeMillis",
          customRender: (text) => {
            if (!text) {
              return "";
            }
            return parseTime(text);
          },
          sorter: true,
          width: 180,
        },
        { title: "操作", dataIndex: "operation", scopedSlots: { customRender: "operation" }, width: 200 },
      ],
      // 表单校验规则
      rules: {
        name: [{ required: true, message: "请输入工作空间名称", trigger: "blur" }],
        description: [{ required: true, message: "请输入工作空间描述", trigger: "blur" }],
      },
      // 表单校验规则
      rulesEnv: {
        name: [{ required: true, message: "请输入变量名称", trigger: "blur" }],
        description: [{ required: true, message: "请输入变量描述", trigger: "blur" }],
        value: [{ required: true, message: "请输入变量值", trigger: "blur" }],
      },
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
    envVarPagination() {
      return {
        total: this.envVarListQuery.total,
        current: this.envVarListQuery.page || 1,
        pageSize: this.envVarListQuery.limit || PAGE_DEFAULT_LIMIT,
        pageSizeOptions: PAGE_DEFAULT_SIZW_OPTIONS,
        showSizeChanger: true,
        showTotal: (total) => {
          return PAGE_DEFAULT_SHOW_TOTAL(total, this.envVarListQuery);
        },
      };
    },
  },
  created() {
    this.loadData();
  },
  methods: {
    // 加载数据
    loadData(pointerEvent) {
      this.loading = true;
      this.listQuery.page = pointerEvent?.altKey || pointerEvent?.ctrlKey ? 1 : this.listQuery.page;
      getWorkSpaceList(this.listQuery).then((res) => {
        if (res.code === 200) {
          this.list = res.data.result;
          this.listQuery.total = res.data.total;
        }
        this.loading = false;
      });
    },
    loadDataEnvVar(pointerEvent) {
      this.envVarLoading = true;
      this.envVarListQuery.page = pointerEvent?.altKey || pointerEvent?.ctrlKey ? 1 : this.envVarListQuery.page;
      getWorkspaceEnvList(this.envVarListQuery).then((res) => {
        if (res.code === 200) {
          this.envVarList = res.data.result;
          this.envVarListQuery.total = res.data.total;
        }
        this.envVarLoading = false;
      });
    },
    viewEnvVar(record) {
      this.temp = record;
      this.envVarListVisible = true;
      this.loadDataEnvVar();
    },
    addEnvVar() {
      this.temp = {};
      this.editEnvVisible = true;
      this.$refs["editEnvForm"] && this.$refs["editEnvForm"].resetFields();
    },
    handleAdd() {
      this.temp = {};
      this.editVisible = true;
      this.$refs["editForm"] && this.$refs["editForm"].resetFields();
    },
    handleEdit(record) {
      this.temp = record;
      this.editVisible = true;
    },
    handleEnvEdit(record) {
      this.temp = record;
      this.editEnvVisible = true;
    },
    handleEditOk() {
      this.$refs["editForm"].validate((valid) => {
        if (!valid) {
          return false;
        }
        editWorkSpace(this.temp).then((res) => {
          if (res.code === 200) {
            // 成功
            this.$notification.success({
              message: res.msg,
            });
            this.$refs["editForm"].resetFields();
            this.editVisible = false;
            this.loadData();
          }
        });
      });
    },
    handleEnvEditOk() {
      this.$refs["editEnvForm"].validate((valid) => {
        if (!valid) {
          return false;
        }
        editWorkspaceEnv(this.temp).then((res) => {
          if (res.code === 200) {
            // 成功
            this.$notification.success({
              message: res.msg,
            });
            this.$refs["editEnvForm"].resetFields();
            this.editEnvVisible = false;
            this.loadDataEnvVar();
          }
        });
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
    changeListeEnvVar(pagination, filters, sorter) {
      this.envVarListQuery.page = pagination.current;
      this.envVarListQuery.limit = pagination.pageSize;
      if (sorter) {
        this.envVarListQuery.order = sorter.order;
        this.envVarListQuery.order_field = sorter.field;
      }
      this.loadDataEnvVar();
    },
    handleEnvDelete(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的当前工作空间么,删除前需要将关联数据都删除后才能删除当前工作空间？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 删除
          deleteWorkspaceEnv(record.id).then((res) => {
            if (res.code === 200) {
              this.$notification.success({
                message: res.msg,
              });
              this.loadDataEnvVar();
            }
          });
        },
      });
    },
    // 删除
    handleDelete(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的当前工作空间么,删除前需要将关联数据都删除后才能删除当前工作空间？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 删除
          deleteWorkspace(record.id).then((res) => {
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
  },
};
</script>
<style scoped>
.filter {
  margin-bottom: 10px;
}

.ant-btn {
  margin-left: 10px;
}

.search {
  width: 200px;
}
</style>
