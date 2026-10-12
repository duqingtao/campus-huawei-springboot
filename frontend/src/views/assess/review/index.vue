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
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入学生姓名"
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
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reviewList">
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="申报ID" prop="declarationId" width="80" align="center" />
      <el-table-column label="申报单号" prop="declarationNo" width="160" :show-overflow-tooltip="true" />
      <el-table-column label="学生姓名" prop="studentName" width="100" />
      <el-table-column label="班级" prop="className" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="细则标题" prop="regulationTitle" :show-overflow-tooltip="true" />
      <el-table-column label="总分" prop="totalScore" width="80" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.totalScore != null ? scope.row.totalScore : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" prop="submitTime" width="160" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.submitTime ? parseTime(scope.row.submitTime) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit-outline"
            @click="handleAudit(scope.row)"
            v-hasPermi="['assess:review:edit']"
          >审核</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-time"
            @click="handleHistory(scope.row)"
            v-hasPermi="['assess:review:query']"
          >历史</el-button>
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

    <!-- 审核弹窗 -->
    <el-dialog title="审核申报" :visible.sync="auditOpen" width="900px" append-to-body>
      <el-descriptions :column="3" border size="small" style="margin-bottom: 16px;">
        <el-descriptions-item label="申报单号">{{ auditData.declarationNo }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ auditData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ auditData.className }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ auditData.totalScore != null ? auditData.totalScore : '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ auditData.submitTime ? parseTime(auditData.submitTime) : '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">评分明细</el-divider>
      <el-table :data="auditData.items" size="small" border>
        <el-table-column label="条款编号" prop="evidenceClauseNo" width="100" align="center" />
        <el-table-column label="条款标题" prop="clauseTitle" :show-overflow-tooltip="true" />
        <el-table-column label="AI建议分" prop="suggestedScore" width="90" align="center" />
        <el-table-column label="终分" prop="finalScore" width="80" align="center" />
        <el-table-column label="置信度" prop="evidenceConfidence" width="80" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.evidenceConfidence != null ? (scope.row.evidenceConfidence * 100).toFixed(0) + '%' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="匹配说明" prop="matchReason" :show-overflow-tooltip="true" />
      </el-table>

      <el-divider content-position="left">审核操作</el-divider>
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="驳回意见">
          <el-input v-model="auditForm.reviewComment" type="textarea" :rows="3" placeholder="驳回时必填，通过/存疑可留空" />
          <el-button
            size="mini"
            type="primary"
            plain
            icon="el-icon-magic-stick"
            style="margin-top: 8px;"
            :loading="draftLoading"
            @click="handleDraftComment"
            v-hasPermi="['assess:review:edit']"
          >AI生成驳回草稿</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="success" @click="submitAudit('pass')">通 过</el-button>
        <el-button type="warning" @click="submitAudit('suspect')">存 疑</el-button>
        <el-button type="danger" @click="submitAudit('reject')">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 审核历史抽屉 -->
    <el-drawer title="审核历史" :visible.sync="historyDrawer" direction="rtl" size="40%">
      <div style="padding: 20px;">
        <el-timeline v-if="historyList.length > 0">
          <el-timeline-item
            v-for="item in historyList"
            :key="item.reviewId"
            :timestamp="item.reviewTime ? parseTime(item.reviewTime) : '-'"
            :type="reviewResultTagType(item.reviewResult)"
          >
            <el-card shadow="never">
              <p><b>审核结果：</b>{{ reviewResultText(item.reviewResult) }}</p>
              <p><b>审核人：</b>{{ item.reviewerName }}</p>
              <p><b>轮次：</b>第 {{ item.reviewRound }} 轮</p>
              <p v-if="item.reviewScore"><b>评分：</b>{{ item.reviewScore }}</p>
              <p v-if="item.reviewComment"><b>意见：</b>{{ item.reviewComment }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审核记录" />
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { listReview, auditDeclaration, reviewHistory, draftComment } from "@/api/assess/review";
import { getDeclaration } from "@/api/assess/declaration";

export default {
  name: "Review",
  data() {
    return {
      loading: true,
      total: 0,
      showSearch: true,
      reviewList: [],
      auditOpen: false,
      auditData: {},
      auditForm: { declarationId: undefined, result: "", reviewComment: "" },
      draftLoading: false,
      historyDrawer: false,
      historyList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        declarationNo: undefined,
        studentName: undefined,
        status: "1"
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listReview(this.queryParams).then(response => {
        this.reviewList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    reviewResultText(result) {
      const map = { "pass": "通过", "suspect": "存疑", "reject": "驳回" };
      return map[result] || result || "-";
    },
    reviewResultTagType(result) {
      const map = { "pass": "success", "suspect": "warning", "reject": "danger" };
      return map[result] || "info";
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList(); },
    resetQuery() { this.resetForm("queryForm"); this.queryParams.status = "1"; this.handleQuery(); },
    handleAudit(row) {
      this.auditData = {};
      this.auditForm = { declarationId: row.declarationId, result: "", reviewComment: "" };
      getDeclaration(row.declarationId).then(response => {
        this.auditData = response.data;
        this.auditOpen = true;
      });
    },
    submitAudit(result) {
      if (result === 'reject' && !this.auditForm.reviewComment) {
        this.$modal.msgWarning("驳回时必须填写驳回意见");
        return;
      }
      this.auditForm.result = result;
      const resultText = { pass: "通过", suspect: "存疑", reject: "驳回" }[result];
      this.$modal.confirm('确认执行"' + resultText + '"操作？').then(() => {
        return auditDeclaration(this.auditForm);
      }).then(() => {
        this.$modal.msgSuccess("审核成功");
        this.auditOpen = false;
        this.getList();
      }).catch(() => {});
    },
    handleDraftComment() {
      this.draftLoading = true;
      draftComment({ declarationId: this.auditForm.declarationId }).then(response => {
        this.draftLoading = false;
        if (response.data) {
          this.auditForm.reviewComment = response.data;
          this.$modal.msgSuccess("AI草稿已生成，请审阅后修改");
        } else {
          this.$modal.msgWarning("AI未能生成草稿，请手动填写");
        }
      }).catch(() => { this.draftLoading = false; });
    },
    handleHistory(row) {
      reviewHistory(row.declarationId).then(response => {
        this.historyList = response.data || [];
        this.historyDrawer = true;
      });
    }
  }
};
</script>
