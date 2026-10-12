<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="细则编号" prop="regulationNo">
        <el-input
          v-model="queryParams.regulationNo"
          placeholder="请输入细则编号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="细则标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入细则标题"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="细则状态" clearable style="width: 200px">
          <el-option label="草案" value="0" />
          <el-option label="已发布" value="1" />
          <el-option label="已废止" value="2" />
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
          v-hasPermi="['assess:regulation:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['assess:regulation:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="regulationList" border>
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="细则ID" prop="regulationId" width="90" align="center" />
      <el-table-column label="细则编号" prop="regulationNo" width="130" :show-overflow-tooltip="true" />
      <el-table-column label="细则标题" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="版本号" prop="versionNum" width="90" align="center" />
      <el-table-column label="发布日期" prop="publishDate" width="120" align="center">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.publishDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="生效日期" prop="effectiveDate" width="120" align="center">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.effectiveDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" align="center">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['assess:regulation:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['assess:regulation:remove']"
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

    <!-- 新增或修改细则对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="细则编号" prop="regulationNo">
          <el-input v-model="form.regulationNo" placeholder="请输入细则编号" />
        </el-form-item>
        <el-form-item label="细则标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入细则标题" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="版本号" prop="versionNum">
              <el-input-number v-model="form.versionNum" controls-position="right" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option label="草案" value="0" />
                <el-option label="已发布" value="1" />
                <el-option label="已废止" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="发布日期" prop="publishDate" label-width="90px">
              <el-date-picker v-model="form.publishDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生效日期" prop="effectiveDate" label-width="90px">
              <el-date-picker v-model="form.effectiveDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="失效日期" prop="expireDate" label-width="90px">
              <el-date-picker v-model="form.expireDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="细则内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入细则内容" />
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
import { listRegulation, getRegulation, addRegulation, updateRegulation, delRegulation } from "@/api/assess/regulation";

export default {
  name: "Regulation",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 显示搜索条件
      showSearch: true,
      // 细则表格数据
      regulationList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        regulationNo: undefined,
        title: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        regulationNo: [
          { required: true, message: "细则编号不能为空", trigger: "blur" }
        ],
        title: [
          { required: true, message: "细则标题不能为空", trigger: "blur" }
        ],
        versionNum: [
          { required: true, message: "版本号不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询细则列表 */
    getList() {
      this.loading = true;
      listRegulation(this.queryParams).then(response => {
        this.regulationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 状态文本映射
    statusText(status) {
      const map = { "0": "草案", "1": "已发布", "2": "已废止" };
      return map[status] || "未知";
    },
    // 状态标签类型映射
    statusTagType(status) {
      const map = { "0": "info", "1": "success", "2": "danger" };
      return map[status] || "info";
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        regulationId: undefined,
        regulationNo: undefined,
        title: undefined,
        publishDate: undefined,
        effectiveDate: undefined,
        expireDate: undefined,
        status: "0",
        versionNum: 1,
        content: undefined,
        remark: undefined
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
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增细则";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      getRegulation(row.regulationId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改细则";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.regulationId != undefined) {
            updateRegulation(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRegulation(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除细则编号为"' + row.regulationNo + '"的数据项？\n注意：删除细则将连带删除其下所有条款！').then(function() {
        return delRegulation(row.regulationId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('assess/regulation/export', {
        ...this.queryParams
      }, `regulation_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>
