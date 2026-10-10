<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="单号" prop="declarationNo">
        <el-input v-model="queryParams.declarationNo" placeholder="请输入申报单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="学年" prop="academicYear">
        <el-input v-model="queryParams.academicYear" placeholder="请输入学年" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="学期" prop="semester">
        <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable>
          <el-option label="第一学期" value="1" />
          <el-option label="第二学期" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="草稿" value="0" />
          <el-option label="已提交" value="1" />
          <el-option label="已审核" value="2" />
          <el-option label="已退回" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['assess:declaration:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['assess:declaration:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['assess:declaration:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['assess:declaration:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="declarationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="申报单号" align="center" prop="declarationNo" width="160" />
      <el-table-column label="学生姓名" align="center" prop="studentName" width="100" />
      <el-table-column label="班级" align="center" prop="className" width="120" />
      <el-table-column label="学年" align="center" prop="academicYear" width="100" />
      <el-table-column label="学期" align="center" prop="semester" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.semester === '1' ? '第一学期' : '第二学期' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总分" align="center" prop="totalScore" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="submitTime" width="160" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)"
            v-hasPermi="['assess:declaration:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['assess:declaration:edit']" v-if="scope.row.status === '0'">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-s-promotion" @click="handleSubmit(scope.row)"
            v-hasPermi="['assess:declaration:edit']" v-if="scope.row.status === '0'">提交</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['assess:declaration:remove']" v-if="scope.row.status === '0'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="细则ID" prop="regulationId">
          <el-input v-model="form.regulationId" placeholder="请输入评测细则ID" />
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-input v-model="form.academicYear" placeholder="请输入学年，如2025-2026" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="form.semester" placeholder="请选择学期">
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="材料选择" prop="materialIds">
          <el-select v-model="form.materialIds" multiple placeholder="请选择已上传的材料" style="width: 100%">
            <el-option v-for="item in materialOptions" :key="item.materialId"
              :label="item.materialName" :value="item.materialId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="申报单详情" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="申报单号">{{ detail.declarationNo }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detail.className }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ detail.academicYear }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ detail.semester === '1' ? '第一学期' : '第二学期' }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ detail.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">明细列表</el-divider>
      <el-table :data="detail.items" border size="small">
        <el-table-column label="序号" type="index" width="50" align="center" />
        <el-table-column label="材料名称" align="center" prop="materialName" width="160" />
        <el-table-column label="匹配条款" align="center" prop="clauseNo" width="100" />
        <el-table-column label="条款标题" align="center" prop="clauseTitle" show-overflow-tooltip />
        <el-table-column label="AI建议分" align="center" prop="suggestedScore" width="90" />
        <el-table-column label="规则调整分" align="center" prop="ruleAdjustedScore" width="90" />
        <el-table-column label="最终分" align="center" prop="finalScore" width="80">
          <template slot-scope="scope">
            <span style="font-weight: bold; color: #409EFF">{{ scope.row.finalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="匹配状态" align="center" prop="matchStatus" width="80">
          <template slot-scope="scope">
            <el-tag :type="matchTagType(scope.row.matchStatus)" size="mini">{{ matchText(scope.row.matchStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="证据条款" align="center" prop="evidenceClauseNo" width="80" />
        <el-table-column label="置信度" align="center" prop="evidenceConfidence" width="80" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDeclaration, getDeclaration, delDeclaration, addDeclaration, updateDeclaration, submitDeclaration } from "@/api/assess/declaration";

export default {
  name: "Declaration",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 申报单表格数据
      declarationList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 详情弹出层
      detailOpen: false,
      // 详情数据
      detail: {},
      // 材料选项（实际应从材料接口获取）
      materialOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        declarationNo: undefined,
        academicYear: undefined,
        semester: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        regulationId: [{ required: true, message: "评测细则ID不能为空", trigger: "blur" }],
        academicYear: [{ required: true, message: "学年不能为空", trigger: "blur" }],
        semester: [{ required: true, message: "学期不能为空", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      listDeclaration(this.queryParams).then(response => {
        this.declarationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 状态文本 */
    statusText(status) {
      const map = { '0': '草稿', '1': '已提交', '2': '已审核', '3': '已退回' };
      return map[status] || '未知';
    },
    /** 状态标签类型 */
    statusTagType(status) {
      const map = { '0': 'info', '1': 'warning', '2': 'success', '3': 'danger' };
      return map[status] || 'info';
    },
    /** 匹配状态文本 */
    matchText(status) {
      const map = { '0': '待匹配', '1': '已匹配', '2': '未匹配', '3': '匹配失败' };
      return map[status] || '未知';
    },
    /** 匹配状态标签类型 */
    matchTagType(status) {
      const map = { '0': 'info', '1': 'success', '2': 'warning', '3': 'danger' };
      return map[status] || 'info';
    },
    /** 搜索按钮 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.declarationId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增申报单";
    },
    /** 修改按钮 */
    handleUpdate(row) {
      this.reset();
      const declarationId = row.declarationId || this.ids;
      getDeclaration(declarationId).then(response => {
        this.form = response.data;
        this.form.materialIds = (response.data.items || []).map(i => i.materialId);
        this.open = true;
        this.title = "修改申报单";
      });
    },
    /** 详情按钮 */
    handleDetail(row) {
      getDeclaration(row.declarationId).then(response => {
        this.detail = response.data;
        this.detailOpen = true;
      });
    },
    /** 提交按钮 */
    handleSubmit(row) {
      this.$modal.confirm('确认提交申报单"' + row.declarationNo + '"吗？提交后将触发AI匹配和规则引擎定分，不可再修改。').then(() => {
        return submitDeclaration(row.declarationId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("提交成功");
      }).catch(() => {});
    },
    /** 删除按钮 */
    handleDelete(row) {
      const declarationIds = row.declarationId || this.ids;
      this.$modal.confirm('确认删除申报单编号为"' + declarationIds + '"的数据项？').then(() => {
        return delDeclaration(declarationIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮 */
    handleExport() {
      this.download('assess/declaration/export', {
        ...this.queryParams
      }, `declaration_${new Date().getTime()}.xlsx`);
    },
    /** 提交表单 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.declarationId != null) {
            updateDeclaration(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDeclaration(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        declarationId: undefined,
        regulationId: undefined,
        academicYear: undefined,
        semester: undefined,
        materialIds: [],
        remark: undefined
      };
      this.resetForm("form");
    }
  }
};
</script>
