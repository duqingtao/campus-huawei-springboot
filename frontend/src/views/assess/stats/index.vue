<template>
  <div class="app-container">
    <!-- 数字卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ overview.studentCount || 0 }}</div>
          <div class="stat-label">申报总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #409EFF;">{{ overview.avgScore != null ? overview.avgScore : '-' }}</div>
          <div class="stat-label">平均分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C;">{{ overview.reviewingCount || 0 }}</div>
          <div class="stat-label">待审核数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #67C23A;">{{ overview.passedCount || 0 }}</div>
          <div class="stat-label">已通过数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="班级" prop="className">
        <el-input
          v-model="queryParams.className"
          placeholder="请输入班级"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入姓名"
          clearable
          style="width: 160px"
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
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['assess:stats:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 学生明细分页表格 -->
    <el-table v-loading="loading" :data="studentList" border>
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="学号" prop="studentNo" width="130" align="center" />
      <el-table-column label="姓名" prop="studentName" width="100" />
      <el-table-column label="班级" prop="className" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="申报单号" prop="declarationNo" width="170" :show-overflow-tooltip="true" />
      <el-table-column label="总分" prop="totalScore" width="90" align="center">
        <template slot-scope="scope">
          <span style="font-weight: bold;">{{ scope.row.totalScore != null ? scope.row.totalScore : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="学年" prop="academicYear" width="110" align="center" />
      <el-table-column label="学期" prop="semester" width="90" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.semester === '1' ? '第一学期' : '第二学期' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
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
  </div>
</template>

<script>
import { statsOverview, statsStudents } from "@/api/assess/stats";

export default {
  name: "Stats",
  data() {
    return {
      loading: true,
      total: 0,
      showSearch: true,
      overview: {},
      studentList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        className: undefined,
        studentName: undefined
      }
    };
  },
  created() {
    this.getOverview();
    this.getList();
  },
  methods: {
    getOverview() {
      statsOverview(this.queryParams).then(response => { this.overview = response.data || {}; });
    },
    getList() {
      this.loading = true;
      statsStudents(this.queryParams).then(response => {
        this.studentList = response.rows;
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
    handleQuery() { this.queryParams.pageNum = 1; this.getOverview(); this.getList(); },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery(); },
    handleExport() {
      this.download('assess/stats/export', { ...this.queryParams }, `stats_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 10px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.4;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
