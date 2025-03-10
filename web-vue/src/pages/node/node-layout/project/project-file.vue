<template>
  <!-- 布局 -->
  <a-layout class="file-layout">
    <!-- 目录树 -->
    <a-layout-sider theme="light" class="sider" width="25%">
      <div class="dir-container">
        <a-button type="primary" @click="loadData">刷新目录</a-button>
        <a-button type="primary" v-if="showConsole" @click="goConsole" v-show="noFileModes.includes(runMode)">控制台</a-button>
      </div>
      <a-empty v-if="treeList.length === 0" />
      <el-tree
        ref="tree"
        :data="treeList"
        :props="defaultProps"
        :load="loadNode"
        :default-expanded-keys="expandKeys"
        :expand-on-click-node="false"
        node-key="$treeNodeId"
        highlight-current
        lazy
        @node-click="nodeClick"
      ></el-tree>
    </a-layout-sider>
    <!-- 表格 -->
    <a-layout-content class="file-content">
      <div ref="filter" class="filter">
        <!-- <a-tag color="#2db7f5">项目目录: {{ absPath }}</a-tag>-->
        <a-button type="primary" @click="handleUpload">批量上传文件</a-button>
        <a-button type="primary" @click="handleZipUpload">上传压缩文件（自动解压）</a-button>
        <a-button type="primary" @click="openRemoteUpload">远程上传</a-button>
        <a-button type="primary" @click="loadFileList">刷新表格</a-button>
        <a-button type="danger" @click="clearFile">清空项目目录</a-button>
        <a-tag color="#2db7f5" v-if="uploadPath">当前目录: {{ uploadPath }}</a-tag>
      </div>
      <a-table
        :data-source="fileList"
        :loading="loading"
        :columns="columns"
        :scroll="{ x: 730, y: tableHeight - 60 }"
        :style="{ 'max-height': tableHeight + 'px' }"
        :pagination="false"
        bordered
        :rowKey="(record, index) => index"
      >
        <a-tooltip slot="filename" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text }}</span>
        </a-tooltip>
        <a-tooltip slot="isDirectory" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text ? "目录" : "文件" }}</span>
        </a-tooltip>
        <a-tooltip slot="fileSize" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text }}</span>
        </a-tooltip>
        <template slot="operation" v-if="!record.isDirectory" slot-scope="text, record">
          <a-tooltip title="需要到 节点管理中的系统管理的白名单配置中配置允许编辑的文件后缀">
            <a-button type="primary" :disabled="!record.textFileEdit" @click="handleEditFile(record)">编辑</a-button>
          </a-tooltip>
          <a-button type="primary" @click="handleDownload(record)">下载</a-button>
          <a-button type="danger" @click="handleDelete(record)">删除</a-button>
        </template>
      </a-table>
      <!-- 批量上传文件 -->
      <a-modal v-model="uploadFileVisible" width="300px" title="上传项目文件" :footer="null" :maskClosable="true">
        <a-upload :file-list="uploadFileList" :remove="handleRemove" :before-upload="beforeUpload" multiple>
          <a-button><a-icon type="upload" />选择文件</a-button>
        </a-upload>
        <br />
        <el-progress :text-inside="true" :stroke-width="18" :percentage="percentage" status="success"></el-progress>
        <br />
        <a-button type="primary" :disabled="fileUploadDisabled" @click="startUpload">开始上传</a-button>
        <a-tag color="green" :visible="successSize !== 0" :closable="true" class="successTag"> 上传成功: {{ successSize }} 个文件! </a-tag>
      </a-modal>
      <!-- 上传压缩文件 -->
      <a-modal v-model="uploadZipFileVisible" width="300px" title="上传压缩文件" :footer="null" :maskClosable="true">
        <a-upload :file-list="uploadFileList" :remove="handleZipRemove" :before-upload="beforeZipUpload" :accept="ZIP_ACCEPT">
          <a-button><a-icon type="upload" />选择压缩文件</a-button>
        </a-upload>
        <br />
        <a-switch v-model="checkBox" checked-children="清空覆盖" un-checked-children="不清空" style="margin-bottom: 10px" />
        <br />
        <el-progress :text-inside="true" :stroke-width="18" :percentage="percentage" status="success"></el-progress>
        <br />
        <a-button type="primary" :disabled="fileUploadDisabled" @click="startZipUpload">开始上传</a-button>
        <a-tag color="green" :visible="successSize !== 0" :closable="true" class="successTag"> 上传成功: {{ successSize }} 个文件! </a-tag>
      </a-modal>

      <a-modal v-model="editFileVisible" width="80vw" title="编辑文件" cancelText="关闭" :maskClosable="true" @ok="updateFileData" @cancel="handleCloseModal">
        <div style="height: 60vh">
          <code-editor showTool v-model="fileContent" :fileSuffix="filename"></code-editor>
        </div>
      </a-modal>
      <!--远程下载  -->
      <a-modal v-model="uploadRemoteFileVisible" title="远程下载文件" @ok="handleRemoteUpload" @cancel="openRemoteUpload" :maskClosable="false">
        <a-form-model :model="remoteDownloadData" :label-col="{ span: 5 }" :wrapper-col="{ span: 24 }" :rules="rules" ref="ruleForm">
          <a-form-model-item label="远程下载URL" prop="url">
            <a-input v-model="remoteDownloadData.url" placeholder="远程下载地址" />
          </a-form-model-item>
          <a-form-model-item label="是否为压缩包">
            <a-switch v-model="remoteDownloadData.unzip" checked-children="是" un-checked-children="否" v-decorator="['unzip', { valuePropName: 'checked' }]" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </a-layout-content>
  </a-layout>
</template>
<script>
import { getFileList, downloadProjectFile, noFileModes, deleteProjectFile, uploadProjectFile, readFile, updateFile, remoteDownload } from "@/api/node-project";
import { ZIP_ACCEPT } from "@/utils/const";
import codeEditor from "@/components/codeEditor";

export default {
  components: {
    codeEditor,
  },
  props: {
    nodeId: {
      type: String,
    },
    projectId: {
      type: String,
    },
    runMode: {
      type: String,
    },
    absPath: {
      type: String,
    },
    showConsole: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      ZIP_ACCEPT: ZIP_ACCEPT,
      noFileModes: noFileModes,
      loading: false,
      treeList: [],
      fileList: [],
      uploadFileList: [],
      expandKeys: [],
      tempNode: {},
      temp: {},
      filename: "",
      uploadFileVisible: false,
      uploadZipFileVisible: false,
      uploadRemoteFileVisible: false,
      editFileVisible: false,
      successSize: 0,
      fileContent: "",

      cmOptions: {
        mode: "application/json",
      },
      // 是否是上传状态
      uploading: false,
      percentage: 0,
      checkBox: false,
      tableHeight: "80vh",
      defaultProps: {
        children: "children",
        label: "filename",
        isLeaf: "isLeaf",
      },
      remoteDownloadData: {
        id: "",
        url: "",
        unzip: false,
      },
      columns: [
        { title: "文件名称", dataIndex: "filename", ellipsis: true, scopedSlots: { customRender: "filename" } },
        { title: "文件类型", dataIndex: "isDirectory", width: 100, ellipsis: true, scopedSlots: { customRender: "isDirectory" } },
        { title: "文件大小", dataIndex: "fileSize", width: 120, ellipsis: true, scopedSlots: { customRender: "fileSize" } },
        { title: "修改时间", dataIndex: "modifyTime", width: 180, ellipsis: true },
        { title: "操作", dataIndex: "operation", width: 260, scopedSlots: { customRender: "operation" } },
      ],
      rules: {
        url: [{ required: true, message: "远程下载Url不为空", trigger: "change" }],
      },
    };
  },
  computed: {
    fileUploadDisabled() {
      return this.uploadFileList.length === 0 || this.uploading;
    },
    uploadPath() {
      if (!this.tempNode.data) {
        return "";
      }
      if (this.tempNode.level === 1) {
        return "";
      } else {
        return (this.tempNode.data.levelName || "") + "/" + this.tempNode.data.filename;
      }
    },
  },
  mounted() {
    this.calcTableHeight();
    this.loadData();
  },
  methods: {
    // 计算表格高度
    calcTableHeight() {
      this.tableHeight = window.innerHeight - this.$refs["filter"].clientHeight - 135;
    },

    handleEditFile(record) {
      this.editFileVisible = true;
      this.loadFileData(record.filename);
      this.filename = record.filename;
    },

    // 关闭编辑器弹窗
    handleCloseModal() {
      this.fileContent = "";
    },

    // 加载数据
    loadData() {
      this.treeList = [];
      const data = {
        $treeNodeId: 1,
        filename: "目录：" + (this.absPath || ""),
        isDirectory: true,
        isLeaf: false,
      };
      this.treeList.push(data);
      // 设置默认展开第一个
      setTimeout(() => {
        const node = this.$refs["tree"].getNode(1);
        this.tempNode = node;
        this.expandKeys = [1];
        this.loadFileList();
      }, 1000);
    },
    // 加载子节点
    loadNode(node, resolve) {
      const data = node.data;
      this.tempNode = node;
      // 如果是目录
      if (data.isDirectory) {
        setTimeout(() => {
          // 请求参数
          const params = {
            nodeId: this.nodeId,
            id: this.projectId,
            path: this.uploadPath,
          };
          // if (node.level === 1) {
          //   params.path = ''
          // } else {
          //   params.path = data.levelName || '' + '/' + data.filename
          // }
          // 加载文件
          getFileList(params).then((res) => {
            if (res.code === 200) {
              const treeData = res.data;
              treeData.forEach((ele) => {
                ele.isLeaf = !ele.isDirectory;
              });
              resolve(res.data);
            } else {
              resolve([]);
            }
          });
        }, 500);
      } else {
        resolve([]);
      }
    },

    // 读取文件数据
    loadFileData(filename) {
      const params = {
        nodeId: this.nodeId,
        id: this.projectId,
        filePath: this.uploadPath,
        filename,
      };

      readFile(params).then((res) => {
        if (res.code === 200) {
          setTimeout(() => {
            this.fileContent = res.data;
          }, 300);
        }
      });
    },

    updateFileData() {
      const params = {
        nodeId: this.nodeId,
        id: this.projectId,
        filePath: this.uploadPath,
        filename: this.filename,
        fileText: this.fileContent,
      };

      updateFile(params).then((res) => {
        console.log(res, 2312);
      });
    },

    // 点击树节点
    nodeClick(data, node) {
      if (data.isDirectory) {
        this.tempNode = node;
        this.loadFileList();
      }
    },
    // 上传文件
    handleUpload() {
      if (Object.keys(this.tempNode).length === 0) {
        this.$notification.error({
          message: "请选择一个节点",
        });
        return;
      }
      //初始化成功数
      this.successSize = 0;
      this.uploadFileVisible = true;
    },
    handleRemove(file) {
      const index = this.uploadFileList.indexOf(file);
      const newFileList = this.uploadFileList.slice();
      newFileList.splice(index, 1);
      this.uploadFileList = newFileList;
    },
    beforeUpload(file) {
      this.successSize = 0;
      this.uploadFileList = [...this.uploadFileList, file];
      return false;
    },
    // 开始上传文件
    startUpload() {
      this.$notification.info({
        message: "正在上传文件，请稍后...",
      });
      // 设置上传状态
      this.uploading = true;
      const timer = setInterval(() => {
        this.percentage = this.percentage > 99 ? 99 : this.percentage + 1;
      }, 1000);

      // 遍历上传文件
      this.uploadFileList.forEach((file, index) => {
        const formData = new FormData();
        formData.append("file", file);
        formData.append("nodeId", this.nodeId);
        formData.append("id", this.projectId);
        // 计算属性 uploadPath
        formData.append("levelName", this.uploadPath);

        // 上传文件
        uploadProjectFile(formData).then((res) => {
          if (res.code === 200) {
            this.$notification.success({
              message: res.msg,
            });
            this.successSize++;
          }
          // 判断是否全部上传完成
          if (index === this.uploadFileList.length - 1) {
            this.percentage = 100;
            setTimeout(() => {
              this.percentage = 0;
              this.uploading = false;
              clearInterval(timer);
              this.loadFileList();
              this.uploadFileList = [];
            }, 1000);
          }
        });
      });
    },
    // 上传压缩文件
    handleZipUpload() {
      if (Object.keys(this.tempNode).length === 0) {
        this.$notification.error({
          message: "请选择一个节点",
        });
        return;
      }
      this.checkBox = false;
      this.successSize = 0;
      this.uploadZipFileVisible = true;
    },
    handleZipRemove() {
      this.uploadFileList = [];
    },
    beforeZipUpload(file) {
      this.uploadFileList = [file];
      return false;
    },
    // 开始上传压缩文件
    startZipUpload() {
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
      formData.append("nodeId", this.nodeId);
      formData.append("id", this.projectId);
      // 计算属性 uploadPath
      formData.append("levelName", this.uploadPath);
      formData.append("type", "unzip");
      formData.append("clearType", this.checkBox ? "clear" : "noClear");
      // 上传文件
      uploadProjectFile(formData).then((res) => {
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
            this.checkBox = false;
            this.uploadFileList = [];
            this.loadFileList();
          }, 1000);
        }
      });
    },
    //打开远程上传
    openRemoteUpload() {
      // this.$refs.ruleForm.resetFields();
      this.uploadRemoteFileVisible = true;
    },
    //关闭远程上传
    closeRemoteUpload() {
      //   this.$refs.ruleForm.resetFields();
      this.uploadRemoteFileVisible = false;
    },
    //处理上传文件
    handleRemoteUpload() {
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          const params = {
            id: this.projectId,
            nodeId: this.nodeId,
            url: this.remoteDownloadData.url,
            levelName: this.uploadPath,
            unzip: this.remoteDownloadData.unzip,
          };
          remoteDownload(params).then((res) => {
            if (res.code == 200) {
              this.$notification.success({
                message: res.msg,
              });
              this.remoteDownloadData = {};
              this.uploadRemoteFileVisible = false;
              this.loadFileList();
            }
          });
        } else {
          return false;
        }
      });
    },
    // 加载文件列表
    loadFileList() {
      if (Object.keys(this.tempNode).length === 0) {
        this.$notification.warn({
          message: "请选择一个节点",
        });
        return false;
      }
      // 请求参数
      const params = {
        nodeId: this.nodeId,
        id: this.projectId,
        path: this.uploadPath,
      };
      this.fileList = [];
      this.loading = true;
      // 加载文件
      getFileList(params).then((res) => {
        if (res.code === 200) {
          // 区分目录和文件
          res.data.forEach((element) => {
            // if (!element.isDirectory) {
            // 设置文件表格
            this.fileList.push({
              ...element,
            });
            // }
          });
        }
        this.loading = false;
      });
    },
    // 清空文件
    clearFile() {
      this.$confirm({
        title: "系统提示",
        content: "真的要清空项目目录和文件么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 请求参数
          const params = {
            nodeId: this.nodeId,
            id: this.projectId,
            type: "clear",
          };
          // 删除
          deleteProjectFile(params).then((res) => {
            if (res.code === 200) {
              this.$notification.success({
                message: res.msg,
              });
              this.loadFileList();
            }
          });
        },
      });
    },
    // 下载
    handleDownload(record) {
      this.$notification.info({
        message: "正在下载，请稍等...",
      });
      // 请求参数
      const params = {
        nodeId: this.nodeId,
        id: this.projectId,
        levelName: record.levelName,
        filename: record.filename,
      };
      // 请求接口拿到 blob
      downloadProjectFile(params).then((blob) => {
        const url = window.URL.createObjectURL(blob);
        let link = document.createElement("a");
        link.style.display = "none";
        link.href = url;
        link.setAttribute("download", record.filename);
        document.body.appendChild(link);
        link.click();
      });
    },
    // 删除
    handleDelete(record) {
      this.$confirm({
        title: "系统提示",
        content: "真的要删除文件么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 请求参数
          const params = {
            nodeId: this.nodeId,
            id: this.projectId,
            levelName: record.levelName,
            filename: record.filename,
          };
          // 删除
          deleteProjectFile(params).then((res) => {
            if (res.code === 200) {
              this.$notification.success({
                message: res.msg,
              });
              this.loadData();
              this.loadFileList();
            }
          });
        },
      });
    },
    goConsole() {
      this.$emit("goConsole");
    },
  },
};
</script>

<style scoped>
.file-layout {
  padding: 0;
}
.sider {
  border: 1px solid #e2e2e2;
  height: calc(100vh - 80px);
  overflow-y: auto;
}
.dir-container {
  padding: 10px;
  border-bottom: 1px solid #eee;
}
.file-content {
  height: calc(100vh - 100px);
  overflow-y: auto;
  margin: 10px 10px 0;
  padding: 10px;
  background-color: #fff;
}
.filter {
  margin: 0 0 10px;
}
.ant-btn {
  margin-right: 10px;
}
.successTag {
  height: 32px;
  line-height: 30px;
}
</style>
