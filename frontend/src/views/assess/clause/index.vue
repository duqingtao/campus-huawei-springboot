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
      default-expand-all
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

    <!-- 导入条款对话框（两步：上传解析 → 预览确认） -->
    <el-dialog title="导入条款" :visible.sync="importOpen" width="850px" append-to-body :close-on-click-modal="false">
      <!-- 步骤1：选择文件并解析 -->
      <div v-if="importStep === 1">
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
              accept=".pdf,.docx,.txt"
              :auto-upload="false"
              :limit="1"
              :on-exceed="handleExceed"
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
          <el-button type="primary" :loading="importLoading" @click="submitImport">
            <span v-if="importLoading">AI 解析中，长文档约需 30~60 秒...</span>
            <span v-else>开始解析</span>
          </el-button>
          <el-button @click="cancelImport">取 消</el-button>
        </div>
      </div>

      <!-- 步骤2：预览确认 -->
      <div v-if="importStep === 2">
        <!-- 元数据 -->
        <el-descriptions :column="4" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="原文长度">{{ previewMeta.textLength }} 字</el-descriptions-item>
          <el-descriptions-item label="分块数">{{ previewMeta.chunkCount }}</el-descriptions-item>
          <el-descriptions-item label="截断">
            <el-tag :type="previewMeta.truncated ? 'warning' : 'success'" size="mini">
              {{ previewMeta.truncated ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="模块数">{{ previewClauses.length }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="previewMeta.errors && previewMeta.errors.length > 0" style="margin-bottom: 12px">
          <el-alert
            v-for="(err, idx) in previewMeta.errors"
            :key="idx"
            :title="err"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 4px"
          />
        </div>

        <!-- 预览条款树 -->
        <el-table
          ref="previewTable"
          :data="previewClauses"
          row-key="clauseNo"
          :tree-props="{children: 'children'}"
          default-expand-all
          border
          max-height="400"
          @selection-change="handlePreviewSelectionChange"
        >
          <el-table-column type="selection" width="45" align="center" />
          <el-table-column label="编号" prop="clauseNo" width="100" :show-overflow-tooltip="true" />
          <el-table-column label="标题" prop="clauseTitle" :show-overflow-tooltip="true">
            <template slot-scope="scope">
              <span :style="{color: scope.row.invalid ? '#c0c4cc' : '#303133', textDecoration: scope.row.invalid ? 'line-through' : 'none'}">
                {{ scope.row.clauseTitle || '(空)' }}
              </span>
              <el-tag v-if="scope.row.invalid" type="danger" size="mini" style="margin-left: 6px">
                {{ scope.row.invalidReason }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="90" align="center">
            <template slot-scope="scope">
              <el-tag :type="clauseTypeTagType(scope.row.clauseType)" size="mini">
                {{ clauseTypeText(scope.row.clauseType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分值" prop="maxScore" width="70" align="center" />
          <el-table-column label="原文摘抄" prop="clauseText" :show-overflow-tooltip="true" min-width="200" />
        </el-table>

        <div slot="footer" class="dialog-footer">
          <el-button @click="importStep = 1">返回重选</el-button>
          <el-button type="primary" :loading="confirmLoading" @click="submitConfirm">
            确认导入（{{ selectedPreviewCount }} 条）
          </el-button>
          <el-button @click="cancelImport">取 消</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listClause, getClause, addClause, updateClause, delClause, clauseTree, previewImportClause, confirmImportClause } from "@/api/assess/clause";
import { listRegulation } from "@/api/assess/regulation";

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
      confirmLoading: false,
      importStep: 1,
      importForm: {
        regulationId: undefined
      },
      importRules: {
        regulationId: [
          { required: true, message: "所属细则不能为空", trigger: "change" }
        ]
      },
      fileList: [],
      selectedFile: null,
      // 预览数据
      previewClauses: [],
      previewMeta: {},
      selectedPreviewCount: 0
    };
  },
  computed: {
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
    // ===== 导入条款（两步流程） =====
    clauseTypeTagType(type) {
      const map = { "0": "info", "1": "success", "2": "danger", "3": "warning", "4": "danger" };
      return map[type] || "info";
    },
    handleImport() {
      this.importForm = {
        regulationId: this.queryParams.regulationId || undefined
      };
      this.fileList = [];
      this.selectedFile = null;
      this.importStep = 1;
      this.previewClauses = [];
      this.previewMeta = {};
      this.selectedPreviewCount = 0;
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
        const formData = new FormData();
        formData.append("regulationId", this.importForm.regulationId);
        formData.append("file", this.selectedFile.raw);
        previewImportClause(formData).then(response => {
          this.importLoading = false;
          if (response.code === 200 && response.data) {
            this.previewClauses = response.data.clauses || [];
            this.previewMeta = response.data.meta || {};
            this.importStep = 2;
            // 默认全选非 invalid 项
            this.$nextTick(() => {
              this.setDefaultSelection();
            });
          } else {
            this.$modal.msgError(response.msg || "解析失败");
          }
        }).catch(() => {
          this.importLoading = false;
        });
      });
    },
    setDefaultSelection() {
      // 默认勾选所有非 invalid 的模块行
      this.previewClauses.forEach(row => {
        if (!row.invalid) {
          this.$refs.previewTable.toggleRowSelection(row, true);
        }
      });
    },
    handlePreviewSelectionChange(selection) {
      this.selectedPreviewCount = selection.length;
    },
    submitConfirm() {
      const selection = this.$refs.previewTable.selection || [];
      if (selection.length === 0) {
        this.$modal.msgWarning("请至少勾选一条条款");
        return;
      }
      this.doConfirm(selection);
    },
    doConfirm(selectedModules) {
      this.confirmLoading = true;
      confirmImportClause(this.importForm.regulationId, selectedModules).then(response => {
        this.confirmLoading = false;
        if (response.code === 200) {
          this.importOpen = false;
          this.$modal.msgSuccess(response.msg);
          this.queryParams.regulationId = this.importForm.regulationId;
          this.getList();
        } else {
          this.$modal.msgError(response.msg);
        }
      }).catch(() => {
        this.confirmLoading = false;
      });
    },
    cancelImport() {
      this.importOpen = false;
      this.fileList = [];
      this.selectedFile = null;
      this.importStep = 1;
      this.previewClauses = [];
      this.previewMeta = {};
    }
  }
};
</script>
