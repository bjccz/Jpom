<template>
  <!-- 布局 -->
  <a-layout class="ssh-file-layout">
    <!-- 目录树 -->
    <a-layout-sider theme="light" class="sider" width="25%">
      <a-empty v-if="treeList.length === 0" />
      <a-directory-tree :treeData="treeList" :replaceFields="replaceFields" @select="onSelect"> </a-directory-tree>
    </a-layout-sider>
    <!-- 表格 -->
    <a-layout-content class="file-content">
      <div ref="filter" class="filter">
        <a-button :disabled="!this.tempNode.parentDir" type="primary" @click="handleUpload">上传文件</a-button>
        <a-button :disabled="!this.tempNode.parentDir" type="primary" @click="handleUploadZip">上传压缩文件（自动解压）</a-button>
        <a-button :disabled="!this.tempNode.parentDir" type="primary" @click="loadFileList()">刷新</a-button>
        <a-button :disabled="!this.tempNode.parentDir" type="danger" @click="handleDeletePath()">删除</a-button>
        <span v-if="this.tempNode.parentDir">当前目录:{{ this.tempNode.path }}</span
        ><span v-if="this.tempNode.parentDir">{{ this.tempNode.parentDir }}</span>
      </div>
      <a-table :data-source="fileList" :loading="loading" :columns="columns" :pagination="false" bordered :rowKey="(record, index) => index">
        <a-tooltip slot="name" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text }}</span>
        </a-tooltip>
        <a-tooltip slot="dir" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text ? "目录" : "文件" }}</span>
        </a-tooltip>
        <a-tooltip slot="size" slot-scope="text" placement="topLeft" :title="text">
          <span>{{ text }}</span>
        </a-tooltip>
        <template slot="operation" slot-scope="text, record">
          <a-tooltip title="需要到 ssh 信息中配置允许编辑的文件后缀">
            <a-button type="primary" :disabled="!record.textFileEdit" @click="handleEdit(record)">编辑 </a-button>
          </a-tooltip>
          <!-- <a-button type="primary" :disabled="!record.textFileEdit" @click="handlePreview(record)">跟踪</a-button> -->
          <a-button type="primary" @click="handleDownload(record)">下载</a-button>
          <a-button type="danger" @click="handleDelete(record)">删除</a-button>
        </template>
      </a-table>
      <!-- 上传文件 -->
      <a-modal v-model="uploadFileVisible" width="300px" title="上传文件" :footer="null" :maskClosable="true">
        <a-upload :file-list="uploadFileList" :remove="handleRemove" :before-upload="beforeUpload" :accept="`${uploadFileZip ? ZIP_ACCEPT : ''}`" :multiple="!uploadFileZip">
          <a-button>
            <a-icon type="upload" />选择文件
            {{ uploadFileZip ? "压缩包" : "" }}
          </a-button>
        </a-upload>
        <br />
        <a-button type="primary" :disabled="uploadFileList.length === 0" @click="startUpload">开始上传</a-button>
      </a-modal>
      <!-- Terminal -->
      <a-modal v-model="terminalVisible" width="50%" title="Terminal" :footer="null" :maskClosable="false">
        <terminal v-if="terminalVisible" :sshId="ssh.id" :nodeId="ssh.nodeModel.id" :tail="temp.path + temp.parentDir" />
      </a-modal>
      <a-modal v-model="editFileVisible" width="80vw" title="编辑文件" cancelText="关闭" :maskClosable="true" @ok="updateFileData">
        <div style="height: 60vh">
          <code-editor showTool v-model="temp.fileContent" :fileSuffix="temp.name"></code-editor>
        </div>
      </a-modal>
    </a-layout-content>
  </a-layout>
</template>
<script>
import { getRootFileList, getFileList, downloadFile, deleteFile, uploadFile, readFile, updateFileData } from "@/api/ssh";
import Terminal from "./terminal";
import codeEditor from "@/components/codeEditor";
import { ZIP_ACCEPT } from "@/utils/const";
export default {
  props: {
    ssh: {
      type: Object,
    },
  },
  components: {
    Terminal,
    codeEditor,
  },
  data() {
    return {
      loading: false,
      treeList: [],
      fileList: [],
      uploadFileList: [],
      tempNode: {},
      temp: {},
      uploadFileVisible: false,
      uploadFileZip: false,
      ZIP_ACCEPT: ZIP_ACCEPT,
      terminalVisible: false,
      tableHeight: "80vh",
      replaceFields: {
        children: "children",
        title: "title",
        key: "key",
      },
      columns: [
        { title: "文件名称", dataIndex: "title", ellipsis: true, scopedSlots: { customRender: "name" } },
        { title: "文件类型", dataIndex: "dir", width: 100, ellipsis: true, scopedSlots: { customRender: "dir" } },
        { title: "文件大小", dataIndex: "size", width: 120, ellipsis: true, scopedSlots: { customRender: "size" } },
        { title: "修改时间", dataIndex: "modifyTime", width: 180, ellipsis: true },
        { title: "操作", dataIndex: "operation", scopedSlots: { customRender: "operation" }, width: 270 },
      ],
      editFileVisible: false,
    };
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
    // 加载数据
    loadData() {
      this.loading = true;
      getRootFileList(this.ssh.id).then((res) => {
        if (res.code === 200) {
          let tempList = [];
          res.data.forEach((element) => {
            tempList.push({
              key: element.path,
              title: element.path,
              path: element.path,
              parentDir: "/",
              isLeaf: false,
              disabled: element.error ? true : false,
            });
          });
          this.treeList = tempList;
        }
        this.loading = false;
      });
    },
    // 上传文件
    handleUpload() {
      if (Object.keys(this.tempNode).length === 0) {
        this.$notification.error({
          message: "请选择一个节点",
        });
        return;
      }
      this.uploadFileVisible = true;
      this.uploadFileZip = false;
    },
    handleUploadZip() {
      this.handleUpload();
      this.uploadFileZip = true;
    },
    handleRemove(file) {
      const index = this.uploadFileList.indexOf(file);
      const newFileList = this.uploadFileList.slice();
      newFileList.splice(index, 1);
      this.uploadFileList = newFileList;
    },
    beforeUpload(file) {
      this.uploadFileList = [...this.uploadFileList, file];
      return false;
    },
    // 开始上传文件
    startUpload() {
      this.uploadFileList.forEach((file) => {
        const formData = new FormData();
        formData.append("file", file);
        formData.append("id", this.ssh.id);
        formData.append("name", this.tempNode.parentDir);
        formData.append("unzip", this.uploadFileZip);
        formData.append("path", this.tempNode.path);
        // 上传文件
        uploadFile(formData).then((res) => {
          if (res.code === 200) {
            this.$notification.success({
              message: res.msg,
            });
            this.loadFileList();
            this.uploadFileList = [];
            this.uploadFileVisible = false;
          }
        });
      });
    },
    // 选中目录
    onSelect(selectedKeys, { node }) {
      return new Promise((resolve) => {
        this.tempNode = node.dataRef;
        if (node.dataRef.disabled) {
          resolve();
          return;
        }
        // 请求参数
        const params = {
          id: this.ssh.id,
          path: node.dataRef.path,
          children: node.dataRef.parentDir,
        };
        this.fileList = [];
        this.loading = true;
        // 加载文件
        getFileList(params).then((res) => {
          if (res.code === 200) {
            let children = [];
            // 区分目录和文件
            res.data.forEach((element) => {
              if (element.dir) {
                children.push({
                  key: element.id,
                  title: element.title,
                  name: element.name,
                  path: node.dataRef.path,
                  parentDir: element.parentDir,
                  isLeaf: element.dir ? false : true,
                  disabled: element.error ? true : false,
                });
              } else {
                // 设置文件表格
                this.fileList.push({
                  path: node.dataRef.path,
                  ...element,
                });
              }
            });
            // 设置目录树
            node.dataRef.children = children;
            this.treeList = [...this.treeList];
          }
          this.loading = false;
        });
        resolve();
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
        id: this.ssh.id,
        path: this.tempNode.path,
        children: this.tempNode.parentDir,
      };
      this.fileList = [];
      this.loading = true;
      // 加载文件
      getFileList(params).then((res) => {
        if (res.code === 200) {
          // 区分目录和文件
          this.fileList = res.data
            .filter((element) => {
              return !element.dir;
            })
            .map((element) => {
              // 设置文件表格
              return {
                path: this.tempNode.path,
                ...element,
              };
            });
        }
        this.loading = false;
      });
    },
    // 编辑
    handleEdit(record) {
      this.temp = Object.assign(record);
      const params = {
        id: this.ssh.id,
        path: record.path,
        children: record.parentDir,
      };
      readFile(params).then((res) => {
        if (res.code == 200) {
          this.temp.fileContent = res.data;
          this.editFileVisible = true;
        }
      });
      //
    },
    updateFileData() {
      const params = {
        id: this.ssh.id,
        path: this.temp.path,
        children: this.temp.parentDir,
        content: this.temp.fileContent,
      };
      updateFileData(params).then((res) => {
        this.$notification.success({
          message: res.msg,
        });
        if (res.code == 200) {
          this.editFileVisible = false;
        }
      });
    },
    // 查看
    handlePreview(record) {
      this.temp = Object.assign(record);
      this.terminalVisible = true;
    },
    // 下载
    handleDownload(record) {
      // 请求参数
      const params = {
        id: this.ssh.id,
        path: record.path,
        name: record.parentDir,
      };
      // 请求接口拿到 blob
      downloadFile(params).then((blob) => {
        const url = window.URL.createObjectURL(blob);
        let link = document.createElement("a");
        link.style.display = "none";
        link.href = url;
        link.setAttribute("download", record.name);
        document.body.appendChild(link);
        link.click();
      });
    },
    // 删除文件夹
    handleDeletePath() {
      this.$confirm({
        title: "系统提示",
        content: "真的要删除当前文件夹么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 请求参数
          const params = {
            id: this.ssh.id,
            path: this.tempNode.path,
            name: this.tempNode.parentDir,
          };
          // 删除
          deleteFile(params).then((res) => {
            if (res.code === 200) {
              this.$notification.success({
                message: res.msg,
              });
              // 刷新树
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
        content: "真的要删除文件么？",
        okText: "确认",
        cancelText: "取消",
        onOk: () => {
          // 请求参数
          const params = {
            id: this.ssh.id,
            path: record.path,
            name: record.parentDir,
          };
          // 删除
          deleteFile(params).then((res) => {
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
  },
};
</script>
<style scoped lang="stylus">
.ssh-file-layout {
  padding: 0;
  min-height calc(100vh - 75px);
}
.sider {
  border: 1px solid #e2e2e2;
  /* height: calc(100vh - 80px); */
  /* overflow-y: auto; */
}
.file-content {
  /* height: calc(100vh - 100px); */
  /* overflow-y: auto; */
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
</style>
