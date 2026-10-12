<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="所属细则" prop="regulationId">
        <el-select
          v-model="queryParams.regulationId"
          placeholder="请选择细则"
          clearable
          filterable
          style="width: 240px"
          @change="handleRegulationChange"
        >
          <el-option
            v-for="item in regulationOptions"
            :key="item.regulationId"
            :label="item.regulationNo + ' - ' + item.title"
            :value="item.regulationId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="条款标题" prop="clauseTitle">
        <el-input
          v-model="queryParams.clauseTitle"
          placeholder="请输入条款标题"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
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
          v-hasPermi="['assess:clause:add']"
        >新增条款</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-upload"
          size="mini"
          @click="handleImport"
          v-hasPermi="['assess:clause:add']"
        >导入条款</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['assess:clause:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="clauseList"
      row-key="clauseId"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
      v-if="refreshTable"
    >
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="条款ID" prop="clauseId" width="80" align="center" />
      <el-table-column label="条款编号" prop="clauseNo" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="条款标题" prop="clauseTitle" :show-overflow-tooltip="true" />
      <el-table-column label="分值" prop="maxScore" width="80" align="center" />
      <el-table-column label="类型" prop="clauseType" width="100" align="center">
        <template slot-scope="scope">
          <span>{{ clauseTypeText(scope.row.clauseType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sortOrder" width="80" align="center" />
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-plus"
            @click="handleAddChild(scope.row)"
            v-hasPermi="['assess:clause:add']"
          >新增子条款</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['assess:clause:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['assess:clause:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增或修改条款对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属细则" prop="regulationId">
          <el-select
            v-model="form.regulationId"
            placeholder="请选择细则"
            clearable
            filterable
            style="width: 100%"
            @change="handleFormRegulationChange"
          >
            <el-option
              v-for="item in regulationOptions"
              :key="item.regulationId"
              :label="item.regulationNo + ' - ' + item.title"
              :value="item.regulationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="父条款" prop="parentId">
          <el-cascader
            v-model="form.parentId"
            :options="parentClauseOptions"
            :props="{ checkStrictly: true, emitPath: false, value: 'clauseId', label: 'clauseTitle', children: 'children' }"
            placeholder="不选则为顶级条款"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="条款编号" prop="clauseNo">
              <el-input v-model="form.clauseNo" placeholder="请输入条款编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="条款类型" prop="clauseType">
              <el-select v-model="form.clauseType" placeholder="请选择类型" style="width: 100%">
                <el-option label="加分项" value="1" />
                <el-option label="减分项" value="2" />
                <el-option label="基础项" value="3" />
                <el-option label="一票否决" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="条款标题" prop="clauseTitle">
          <el-input v-model="form.clauseTitle" placeholder="请输入条款标题" />
        </el-form-item>
        <el-form-item label="条款原文" prop="clauseText">
          <el-input v-model="form.clauseText" type="textarea" :rows="6" placeholder="请输入条款原文（AI证据链将从此字段读取）" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="最大分值" prop="maxScore">
              <el-input-number v-model="form.maxScore" controls-position="right" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序号" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" controls-position="right" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入条款对话框 -->
    <el-dialog title="导入条款" :visible.sync="importOpen" width="500px" append-to-body>
      <el-form ref="importForm" :model="importForm" :rules="importRules" label-width="100px">
        <el-form-item label="所属细则" prop="regulationId">
          <el-select
            v-model="importForm.regulationId"
            placeholder="请选择细则"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in regulationOptions"
              :key="item.regulationId"
              :label="item.regulationNo + ' - ' + item.title"
              :value="item.regulationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="upload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="uploadData"
            accept=".pdf,.docx,.txt"
            :auto-upload="false"
            :limit="1"
            :on-exceed="handleExceed"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-change="handleFileChange"
            :file-list="fileList"
            drag
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip" slot="tip" style="color: #909399; font-size: 12px;">
              支持PDF、Word、TXT格式，文件不超过10MB<br/>
              上传综测细则文件，AI将自动识别并提取条款
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="importLoading" @click="submitImport">确 定</el-button>
        <el-button @click="cancelImport">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listClause, getClause, addClause, updateClause, delClause, clauseTree, importClause } from "@/api/assess/clause";
import { listRegulation } from "@/api/assess/regulation";
import { getToken } from "@/utils/auth";

export default {
  name: "Clause",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 重新渲染表格
      refreshTable: true,
      // 总条数
      total: 0,
      // 显示搜索条件
      showSearch: true,
      // 条款树表数据
      clauseList: [],
      // 细则下拉选项
      regulationOptions: [],
      // 父条款树选项（弹窗用）
      parentClauseOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        regulationId: undefined,
        clauseTitle: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        regulationId: [
          { required: true, message: "所属细则不能为空", trigger: "change" }
        ],
        clauseNo: [
          { required: true, message: "条款编号不能为空", trigger: "blur" }
        ],
        clauseTitle: [
          { required: true, message: "条款标题不能为空", trigger: "blur" }
        ]
      },
      // ===== 导入相关 =====
      importOpen: false,
      importLoading: false,
      importForm: {
        regulationId: undefined
      },
      importRules: {
        regulationId: [
          { required: true, message: "所属细则不能为空", trigger: "change" }
        ]
      },
      fileList: [],
      selectedFile: null
    };
  },
  computed: {
    uploadUrl() {
      return process.env.VUE_APP_BASE_API + "/assess/clause/import";
    },
    uploadHeaders() {
      return { Authorization: "Bearer " + getToken() };
    },
    uploadData() {
      return { regulationId: this.importForm.regulationId };
    }
  },
  created() {
    this.getRegulationOptions();
    this.getList();
  },
  methods: {
    /** 查询细则下拉列表 */
    getRegulationOptions() {
      listRegulation().then(response => {
        this.regulationOptions = response.rows;
      });
    },
    /** 查询条款列表 */
    getList() {
      this.loading = true;
      if (this.queryParams.regulationId) {
        clauseTree(this.queryParams.regulationId).then(response => {
          this.clauseList = response.data;
          this.loading = false;
        });
      } else {
        listClause(this.queryParams).then(response => {
          this.clauseList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      }
    },
    clauseTypeText(type) {
      const map = { "1": "加分项", "2": "减分项", "3": "基础项", "4": "一票否决" };
      return map[type] || type || "-";
    },
    handleRegulationChange() {
      this.getList();
    },
    handleFormRegulationChange(regulationId) {
      if (regulationId) {
        clauseTree(regulationId).then(response => {
          this.parentClauseOptions = response.data || [];
        });
      } else {
        this.parentClauseOptions = [];
      }
      this.form.parentId = undefined;
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        clauseId: undefined,
        regulationId: undefined,
        parentId: 0,
        clauseNo: undefined,
        clauseTitle: undefined,
        clauseText: undefined,
        clauseType: "1",
        maxScore: 0,
        sortOrder: 0,
        remark: undefined
      };
      this.parentClauseOptions = [];
      this.resetForm("form");
    },
    handleQuery() {
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.getList();
    },
    handleAdd() {
      this.reset();
      if (this.queryParams.regulationId) {
        this.form.regulationId = this.queryParams.regulationId;
        this.handleFormRegulationChange(this.form.regulationId);
      }
      this.open = true;
      this.title = "新增条款";
    },
    handleAddChild(row) {
      this.reset();
      this.form.regulationId = row.regulationId;
      this.form.parentId = row.clauseId;
      clauseTree(row.regulationId).then(response => {
        this.parentClauseOptions = response.data || [];
      });
      this.open = true;
      this.title = "新增子条款";
    },
    handleUpdate(row) {
      this.reset();
      getClause(row.clauseId).then(response => {
        this.form = response.data;
        if (this.form.regulationId) {
          clauseTree(this.form.regulationId).then(res => {
            this.parentClauseOptions = res.data || [];
          });
        }
        this.open = true;
        this.title = "修改条款";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.clauseId != undefined) {
            updateClause(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addClause(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除条款"' + row.clauseTitle + '"？\n注意：删除条款将连带删除其下所有子条款！').then(function() {
        return delClause(row.clauseId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('assess/clause/export', {
        ...this.queryParams
      }, `clause_${new Date().getTime()}.xlsx`);
    },
    // ===== 导入条款 =====
    handleImport() {
      this.importForm = {
        regulationId: this.queryParams.regulationId || undefined
      };
      this.fileList = [];
      this.selectedFile = null;
      this.importOpen = true;
    },
    handleFileChange(file) {
      this.selectedFile = file;
    },
    handleExceed() {
      this.$modal.msgWarning("只能上传一个文件，请先删除已选文件");
    },
    submitImport() {
      this.$refs["importForm"].validate(valid => {
        if (!valid) return;
        if (!this.selectedFile) {
          this.$modal.msgWarning("请先选择要上传的文件");
          return;
        }
        this.importLoading = true;
        // 手动构建FormData提交
        const formData = new FormData();
        formData.append("regulationId", this.importForm.regulationId);
        formData.append("file", this.selectedFile.raw);
        importClause(formData).then(response => {
          this.importLoading = false;
          this.importOpen = false;
          this.$modal.msgSuccess(response.msg);
          // 自动切换到所选细则的树形视图
          this.queryParams.regulationId = this.importForm.regulationId;
          this.getList();
        }).catch(() => {
          this.importLoading = false;
        });
      });
    },
    handleUploadSuccess(response) {
      this.importLoading = false;
      if (response.code === 200) {
        this.importOpen = false;
        this.$modal.msgSuccess(response.msg);
        this.queryParams.regulationId = this.importForm.regulationId;
        this.getList();
      } else {
        this.$modal.msgError(response.msg);
      }
    },
    handleUploadError() {
      this.importLoading = false;
      this.$modal.msgError("上传失败，请检查网络或文件格式");
    },
    cancelImport() {
      this.importOpen = false;
      this.fileList = [];
      this.selectedFile = null;
    }
  }
};
</script>
