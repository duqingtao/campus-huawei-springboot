<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input
          v-model="queryParams.ruleName"
          placeholder="请输入规则名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="规则类型" prop="ruleType">
        <el-select v-model="queryParams.ruleType" placeholder="规则类型" clearable style="width: 160px">
          <el-option label="封顶" value="cap" />
          <el-option label="互斥" value="mutex" />
          <el-option label="时间窗" value="time_window" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
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
          v-hasPermi="['assess:rule:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['assess:rule:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="ruleList" border>
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="规则ID" prop="ruleId" width="90" align="center" />
      <el-table-column label="规则名称" prop="ruleName" :show-overflow-tooltip="true" />
      <el-table-column label="规则类型" prop="ruleType" width="110" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="ruleTypeTagType(scope.row.ruleType)">{{ ruleTypeText(scope.row.ruleType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="作用范围" prop="regulationId" width="130" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.regulationId ? '指定细则' : '全部' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="优先级" prop="priority" width="90" align="center" />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['assess:rule:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['assess:rule:remove']"
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

    <!-- 新增或修改规则对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="规则类型" prop="ruleType">
              <el-select v-model="form.ruleType" placeholder="请选择类型" style="width: 100%">
                <el-option label="封顶（cap）" value="cap" />
                <el-option label="互斥（mutex）" value="mutex" />
                <el-option label="时间窗（time_window）" value="time_window" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="form.priority" controls-position="right" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="作用范围" prop="scopeType">
          <el-radio-group v-model="form.scopeType" @change="handleScopeChange">
            <el-radio label="all">全部细则</el-radio>
            <el-radio label="specific">指定细则</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.scopeType === 'specific'" label="所属细则" prop="regulationId">
          <el-select v-model="form.regulationId" placeholder="请选择细则" filterable style="width: 100%">
            <el-option
              v-for="item in regulationOptions"
              :key="item.regulationId"
              :label="item.regulationNo + ' - ' + item.title"
              :value="item.regulationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="规则条件" prop="ruleCondition">
          <el-input v-model="form.ruleCondition" type="textarea" :rows="6" placeholder='请输入JSON格式规则条件，如 {"maxScore": 20, "clauseIds": [101, 102]}' />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRule, getRule, addRule, updateRule, delRule } from "@/api/assess/rule";
import { listRegulation } from "@/api/assess/regulation";

export default {
  name: "Rule",
  data() {
    return {
      loading: true,
      total: 0,
      showSearch: true,
      ruleList: [],
      regulationOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        ruleName: undefined,
        ruleType: undefined,
        status: undefined
      },
      form: {},
      rules: {
        ruleName: [{ required: true, message: "规则名称不能为空", trigger: "blur" }],
        ruleType: [{ required: true, message: "规则类型不能为空", trigger: "change" }],
        priority: [{ required: true, message: "优先级不能为空", trigger: "blur" }]
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
      listRule(this.queryParams).then(response => {
        this.ruleList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    ruleTypeText(type) {
      const map = { "cap": "封顶", "mutex": "互斥", "time_window": "时间窗" };
      return map[type] || type || "-";
    },
    ruleTypeTagType(type) {
      const map = { "cap": "", "mutex": "warning", "time_window": "success" };
      return map[type] || "info";
    },
    cancel() { this.open = false; this.reset(); },
    reset() {
      this.form = {
        ruleId: undefined, ruleName: undefined, ruleType: "cap", regulationId: undefined,
        scopeType: "all", priority: 0, status: "0", ruleCondition: undefined, remark: undefined
      };
      this.resetForm("form");
    },
    handleScopeChange() {
      if (this.form.scopeType === 'all') { this.form.regulationId = undefined; }
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList(); },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery(); },
    handleAdd() { this.reset(); this.open = true; this.title = "新增规则"; },
    handleUpdate(row) {
      this.reset();
      getRule(row.ruleId).then(response => {
        this.form = response.data;
        this.form.scopeType = this.form.regulationId ? 'specific' : 'all';
        this.open = true;
        this.title = "修改规则";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.scopeType === 'all') { this.form.regulationId = null; }
          if (this.form.ruleId != undefined) {
            updateRule(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList(); });
          } else {
            addRule(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList(); });
          }
        }
      });
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除规则"' + row.ruleName + '"？').then(function() {
        return delRule(row.ruleId);
      }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功"); }).catch(() => {});
    },
    handleExport() {
      this.download('assess/rule/export', { ...this.queryParams }, `rule_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>
