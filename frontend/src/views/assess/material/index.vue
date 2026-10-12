<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="文件名" prop="materialName">
        <el-input
          v-model="queryParams.materialName"
          placeholder="请输入文件名"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OCR状态" prop="ocrStatus">
        <el-select v-model="queryParams.ocrStatus" placeholder="OCR状态" clearable style="width: 160px">
          <el-option label="未处理" value="0" />
          <el-option label="处理中" value="1" />
          <el-option label="成功" value="2" />
          <el-option label="失败" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="材料类型" prop="materialType">
        <el-select v-model="queryParams.materialType" placeholder="材料类型" clearable style="width: 160px">
          <el-option label="奖学金" value="1" />
          <el-option label="荣誉证书" value="2" />
          <el-option label="竞赛获奖" value="3" />
          <el-option label="社会实践" value="4" />
          <el-option label="科研论文" value="5" />
          <el-option label="其他" value="9" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['assess:material:upload']"
        >上传材料</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['assess:material:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="materialList" border>
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="材料ID" prop="materialId" width="90" align="center" />
      <el-table-column label="文件名" prop="materialName" :show-overflow-tooltip="true" />
      <el-table-column label="材料类型" prop="materialType" width="110" align="center">
        <template slot-scope="scope">
          <span>{{ materialTypeText(scope.row.materialType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="OCR状态" prop="ocrStatus" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="ocrStatusTagType(scope.row.ocrStatus)">{{ ocrStatusText(scope.row.ocrStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" prop="fileSize" width="110" align="center">
        <template slot-scope="scope">
          <span>{{ formatFileSize(scope.row.fileSize) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上传时间" prop="createTime" width="170" align="center">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewOcr(scope.row)"
            v-hasPermi="['assess:material:query']"
          >OCR全文</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-refresh"
            @click="handleReprocess(scope.row)"
            v-hasPermi="['assess:material:reprocess']"
          >重新识别</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['assess:material:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['assess:material:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 上传材料对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="材料类型" prop="materialType">
          <el-select v-model="form.materialType" placeholder="请选择材料类型" style="width: 100%">
            <el-option label="奖学金" value="1" />
            <el-option label="荣誉证书" value="2" />
            <el-option label="竞赛获奖" value="3" />
            <el-option label="社会实践" value="4" />
            <el-option label="科研论文" value="5" />
            <el-option label="其他" value="9" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="upload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            name="file"
            :data="{ materialType: form.materialType }"
            :auto-upload="false"
            :limit="1"
            :on-exceed="handleExceed"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :file-list="fileList"
          >
            <el-button size="small" type="primary" icon="el-icon-upload">点击选择文件</el-button>
            <div slot="tip" class="el-upload__tip">支持上传图片/PDF等证明材料文件</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="uploading" @click="submitUpload">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 修改材料对话框 -->
    <el-dialog :title="title" :visible.sync="editOpen" width="500px" append-to-body>
      <el-form ref="editForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="文件名" prop="materialName">
          <el-input v-model="form.materialName" placeholder="请输入文件名" />
        </el-form-item>
        <el-form-item label="材料类型" prop="materialType">
          <el-select v-model="form.materialType" placeholder="请选择材料类型" style="width: 100%">
            <el-option label="奖学金" value="1" />
            <el-option label="荣誉证书" value="2" />
            <el-option label="竞赛获奖" value="3" />
            <el-option label="社会实践" value="4" />
            <el-option label="科研论文" value="5" />
            <el-option label="其他" value="9" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEditForm">确 定</el-button>
        <el-button @click="editCancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- OCR全文抽屉 -->
    <el-drawer
      title="OCR识别全文"
      :visible.sync="ocrDrawer"
      direction="rtl"
      size="40%"
    >
      <div style="padding: 20px;">
        <el-descriptions :column="1" border size="small" style="margin-bottom: 16px;">
          <el-descriptions-item label="文件名">{{ currentMaterial.materialName }}</el-descriptions-item>
          <el-descriptions-item label="OCR状态">
            <el-tag :type="ocrStatusTagType(currentMaterial.ocrStatus)">{{ ocrStatusText(currentMaterial.ocrStatus) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">OCR文本内容</el-divider>
        <div v-if="currentMaterial.ocrText" class="ocr-text-content">
          <pre>{{ currentMaterial.ocrText }}</pre>
        </div>
        <el-empty v-else description="暂无OCR识别结果" />
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { listMaterial, getMaterial, updateMaterial, delMaterial, reprocessMaterial } from "@/api/assess/material";
import { getToken } from "@/utils/auth";

export default {
  name: "Material",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 显示搜索条件
      showSearch: true,
      // 材料表格数据
      materialList: [],
      // 弹出层标题
      title: "",
      // 上传弹窗
      open: false,
      // 修改弹窗
      editOpen: false,
      // 上传中
      uploading: false,
      // 文件列表
      fileList: [],
      // OCR抽屉
      ocrDrawer: false,
      // 当前查看的材料
      currentMaterial: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        materialName: undefined,
        ocrStatus: undefined,
        materialType: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        materialType: [
          { required: true, message: "材料类型不能为空", trigger: "change" }
        ],
        materialName: [
          { required: true, message: "文件名不能为空", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    uploadUrl() {
      return process.env.VUE_APP_BASE_API + "/assess/material";
    },
    uploadHeaders() {
      return { Authorization: "Bearer " + getToken() };
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询材料列表 */
    getList() {
      this.loading = true;
      listMaterial(this.queryParams).then(response => {
        this.materialList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // OCR状态文本映射
    ocrStatusText(status) {
      const map = { "0": "未处理", "1": "处理中", "2": "成功", "3": "失败" };
      return map[status] || "未知";
    },
    // OCR状态标签类型映射
    ocrStatusTagType(status) {
      const map = { "0": "info", "1": "warning", "2": "success", "3": "danger" };
      return map[status] || "info";
    },
    // 材料类型文本映射
    materialTypeText(type) {
      const map = { "1": "奖学金", "2": "荣誉证书", "3": "竞赛获奖", "4": "社会实践", "5": "科研论文", "9": "其他" };
      return map[type] || type || "-";
    },
    // 文件大小格式化
    formatFileSize(size) {
      if (!size) return "-";
      if (size < 1024) return size + " B";
      if (size < 1024 * 1024) return (size / 1024).toFixed(1) + " KB";
      return (size / 1024 / 1024).toFixed(1) + " MB";
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.fileList = [];
      this.reset();
    },
    editCancel() {
      this.editOpen = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        materialId: undefined,
        materialName: undefined,
        materialType: "1"
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 上传材料按钮 */
    handleAdd() {
      this.reset();
      this.fileList = [];
      this.open = true;
      this.title = "上传材料";
    },
    // 文件超出限制
    handleExceed() {
      this.$modal.msgWarning("只能上传一个文件，请先移除已有文件");
    },
    // 上传成功回调
    handleUploadSuccess(response) {
      this.uploading = false;
      this.open = false;
      this.fileList = [];
      if (response.code === 200) {
        this.$modal.msgSuccess("上传成功");
        this.getList();
      } else {
        this.$modal.msgError(response.msg || "上传失败");
      }
    },
    // 上传失败回调
    handleUploadError() {
      this.uploading = false;
      this.$modal.msgError("上传失败，请稍后重试");
    },
    // 提交上传
    submitUpload() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.fileList.length === 0 && this.$refs.upload.uploadFiles.length === 0) {
            this.$modal.msgWarning("请先选择要上传的文件");
            return;
          }
          this.uploading = true;
          this.$refs.upload.submit();
        }
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      getMaterial(row.materialId).then(response => {
        this.form = response.data;
        this.editOpen = true;
        this.title = "修改材料";
      });
    },
    // 提交修改
    submitEditForm() {
      this.$refs["editForm"].validate(valid => {
        if (valid) {
          updateMaterial(this.form).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.editOpen = false;
            this.getList();
          });
        }
      });
    },
    /** 查看OCR全文 */
    handleViewOcr(row) {
      getMaterial(row.materialId).then(response => {
        this.currentMaterial = response.data;
        this.ocrDrawer = true;
      });
    },
    /** 重新识别 */
    handleReprocess(row) {
      this.$modal.confirm('是否确认对材料"' + row.materialName + '"重新进行OCR识别？').then(function() {
        return reprocessMaterial(row.materialId);
      }).then(() => {
        this.$modal.msgSuccess("已提交重新识别，请稍后刷新查看结果");
        this.getList();
      }).catch(() => {});
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除材料"' + row.materialName + '"？').then(function() {
        return delMaterial(row.materialId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('assess/material/export', {
        ...this.queryParams
      }, `material_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style scoped>
.ocr-text-content {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}
.ocr-text-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
}
</style>
