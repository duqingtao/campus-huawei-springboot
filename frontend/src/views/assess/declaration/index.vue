<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="申报单号" prop="declarationNo">
        <el-input
          v-model="queryParams.declarationNo"
          placeholder="请输入申报单号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="申报状态" clearable style="width: 160px">
          <el-option label="草稿" value="0" />
          <el-option label="已提交" value="1" />
          <el-option label="审核中" value="2" />
          <el-option label="部分通过" value="3" />
          <el-option label="全部通过" value="4" />
          <el-option label="已驳回" value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属细则" prop="regulationId">
        <el-select v-model="queryParams.regulationId" placeholder="请选择细则" clearable filterable style="width: 240px">
          <el-option
            v-for="item in regulationOptions"
            :key="item.regulationId"
            :label="item.regulationNo + ' - ' + item.title"
            :value="item.regulationId"
          />
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
          v-hasPermi="['assess:declaration:add']"
        >新增申报</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['assess:declaration:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="declarationList" border>
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="申报ID" prop="declarationId" width="90" align="center" />
      <el-table-column label="申报单号" prop="declarationNo" width="170" :show-overflow-tooltip="true" />
      <el-table-column label="细则标题" prop="regulationTitle" :show-overflow-tooltip="true" />
      <el-table-column label="总分" prop="totalScore" width="90" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.totalScore != null ? scope.row.totalScore : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" prop="submitTime" width="170" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.submitTime ? parseTime(scope.row.submitTime) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            v-hasPermi="['assess:declaration:query']"
          >详情</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-s-promotion"
            @click="handleSubmit(scope.row)"
            v-hasPermi="['assess:declaration:submit']"
          >提交</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['assess:declaration:edit']"
          >修改</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['assess:declaration:remove']"
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

    <!-- 新增申报对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属细则" prop="regulationId">
          <el-select v-model="form.regulationId" placeholder="请选择细则" filterable style="width: 100%" @change="handleFormRegulationChange">
            <el-option
              v-for="item in regulationOptions"
              :key="item.regulationId"
              :label="item.regulationNo + ' - ' + item.title"
              :value="item.regulationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-input v-model="form.academicYear" placeholder="如 2025-2026" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="form.semester" placeholder="请选择学期" style="width: 100%">
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联材料" prop="materialIds">
          <el-table
            ref="materialTable"
            :data="materialOptions"
            @selection-change="handleMaterialSelectionChange"
            max-height="240"
            size="mini"
          >
            <el-table-column type="selection" width="45" align="center" />
            <el-table-column label="文件名" prop="materialName" :show-overflow-tooltip="true" />
            <el-table-column label="类型" prop="materialType" width="100" align="center">
              <template slot-scope="scope">
                <span>{{ materialTypeText(scope.row.materialType) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="OCR状态" prop="ocrStatus" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="ocrStatusTagType(scope.row.ocrStatus)" size="mini">{{ ocrStatusText(scope.row.ocrStatus) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 修改申报对话框 -->
    <el-dialog :title="title" :visible.sync="editOpen" width="500px" append-to-body>
      <el-form ref="editForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属细则" prop="regulationId">
          <el-select v-model="form.regulationId" placeholder="请选择细则" filterable style="width: 100%">
            <el-option
              v-for="item in regulationOptions"
              :key="item.regulationId"
              :label="item.regulationNo + ' - ' + item.title"
              :value="item.regulationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-input v-model="form.academicYear" placeholder="如 2025-2026" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="form.semester" placeholder="请选择学期" style="width: 100%">
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEditForm">确 定</el-button>
        <el-button @click="editCancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 申报详情对话框 -->
    <el-dialog title="申报详情" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="3" border size="small" style="margin-bottom: 16px;">
        <el-descriptions-item label="申报单号">{{ detailData.declarationNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总分">{{ detailData.totalScore != null ? detailData.totalScore : '-' }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ detailData.academicYear }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ detailData.semester === '1' ? '第一学期' : '第二学期' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.submitTime ? parseTime(detailData.submitTime) : '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">评分明细</el-divider>
      <el-table :data="detailData.items" size="small" border>
        <el-table-column label="条款编号" prop="evidenceClauseNo" width="100" align="center" />
        <el-table-column label="条款标题" prop="clauseTitle" :show-overflow-tooltip="true" />
        <el-table-column label="条款原文" prop="evidenceClauseText" :show-overflow-tooltip="true" />
        <el-table-column label="AI建议分" prop="suggestedScore" width="90" align="center" />
        <el-table-column label="终分" prop="finalScore" width="80" align="center">
          <template slot-scope="scope">
            <span style="font-weight: bold; color: #409EFF;">{{ scope.row.finalScore != null ? scope.row.finalScore : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="置信度" prop="evidenceConfidence" width="80" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.evidenceConfidence != null ? (scope.row.evidenceConfidence * 100).toFixed(0) + '%' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="规则调整说明" prop="ruleCheckResult" :show-overflow-tooltip="true" />
      </el-table>

      <div v-if="detailData.totalScore != null" style="text-align: right; margin-top: 16px;">
        <span style="font-size: 16px; font-weight: bold;">总分数：{{ detailData.totalScore }}</span>
      </div>

      <el-alert
        v-if="detailData.reviewComment"
        title="驳回原因"
        type="error"
        :description="detailData.reviewComment"
        show-icon
        :closable="false"
        style="margin-top: 16px;"
      />
    </el-dialog>
  </div>
</template>

<script>
import { listDeclaration, getDeclaration, addDeclaration, updateDeclaration, delDeclaration, submitDeclaration } from "@/api/assess/declaration";
import { listRegulation } from "@/api/assess/regulation";
import { listMaterial } from "@/api/assess/material";

export default {
  name: "Declaration",
  data() {
    return {
      loading: true,
      total: 0,
      showSearch: true,
      declarationList: [],
      regulationOptions: [],
      materialOptions: [],
      title: "",
      open: false,
      editOpen: false,
      detailOpen: false,
      detailData: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        declarationNo: undefined,
        status: undefined,
        regulationId: undefined
      },
      form: {},
      rules: {
        regulationId: [{ required: true, message: "所属细则不能为空", trigger: "change" }],
        academicYear: [{ required: true, message: "学年不能为空", trigger: "blur" }],
        semester: [{ required: true, message: "学期不能为空", trigger: "change" }]
      }
    };
  },
  created() {
    this.getRegulationOptions();
    this.getList();
  },
  methods: {
    getRegulationOptions() {
      listRegulation().then(response => { this.regulationOptions = response.rows; });
    },
    getList() {
      this.loading = true;
      listDeclaration(this.queryParams).then(response => {
        this.declarationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    statusText(status) {
      const map = { "0": "草稿", "1": "已提交", "2": "审核中", "3": "部分通过", "4": "全部通过", "5": "已驳回" };
      return map[status] || "未知";
    },
    statusTagType(status) {
      const map = { "0": "info", "1": "", "2": "warning", "3": "warning", "4": "success", "5": "danger" };
      return map[status] || "info";
    },
    materialTypeText(type) {
      const map = { "1": "奖学金", "2": "荣誉证书", "3": "竞赛获奖", "4": "社会实践", "5": "科研论文", "9": "其他" };
      return map[type] || type || "-";
    },
    ocrStatusText(status) {
      const map = { "0": "未处理", "1": "处理中", "2": "成功", "3": "失败" };
      return map[status] || "未知";
    },
    ocrStatusTagType(status) {
      const map = { "0": "info", "1": "warning", "2": "success", "3": "danger" };
      return map[status] || "info";
    },
    cancel() { this.open = false; this.reset(); },
    editCancel() { this.editOpen = false; this.reset(); },
    reset() {
      this.form = { declarationId: undefined, regulationId: undefined, academicYear: undefined, semester: undefined, materialIds: [] };
      this.resetForm("form");
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList(); },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery(); },
    handleFormRegulationChange() {
      listMaterial({ pageNum: 1, pageSize: 100, userId: this.$store.getters.userId }).then(response => { this.materialOptions = response.rows; });
    },
    handleMaterialSelectionChange(selection) {
      this.form.materialIds = selection.map(item => item.materialId);
    },
    handleAdd() { this.reset(); this.open = true; this.title = "新增申报"; },
    handleUpdate(row) {
      this.reset();
      getDeclaration(row.declarationId).then(response => { this.form = response.data; this.editOpen = true; this.title = "修改申报"; });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) { addDeclaration(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList(); }); }
      });
    },
    submitEditForm() {
      this.$refs["editForm"].validate(valid => {
        if (valid) { updateDeclaration(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.editOpen = false; this.getList(); }); }
      });
    },
    handleDetail(row) {
      getDeclaration(row.declarationId).then(response => { this.detailData = response.data; this.detailOpen = true; });
    },
    handleSubmit(row) {
      this.$modal.confirm('提交后将触发AI匹配和规则引擎计算，不可撤回，是否继续？').then(function() {
        return submitDeclaration(row.declarationId);
      }).then(() => { this.$modal.msgSuccess("提交成功，AI匹配和规则引擎已开始计算"); this.getList(); }).catch(() => {});
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除申报单"' + row.declarationNo + '"？').then(function() {
        return delDeclaration(row.declarationId);
      }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功"); }).catch(() => {});
    },
    handleExport() {
      this.download('assess/declaration/export', { ...this.queryParams }, `declaration_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>
