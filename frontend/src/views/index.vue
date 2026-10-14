<template>
  <div class="app-container assess-home">
    <!-- 顶部 hero 渐变区（约 70px，深蓝渐变，左对齐） -->
    <div class="hero-section">
      <h1 class="hero-title">综测智办</h1>
      <p class="hero-subtitle">大学生综合素质测评智能认定系统</p>
    </div>

    <!-- 数字卡片区 -->
    <el-row :gutter="16" class="stat-cards" v-loading="loading">
      <el-col :span="6" v-for="(card, idx) in statCards" :key="idx">
        <el-card shadow="hover" class="stat-card" @click.native="goTo(card.path)">
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近申报表 -->
    <el-card shadow="never" class="recent-card">
      <div slot="header" class="recent-header">
        <span class="recent-title">最近申报</span>
      </div>
      <el-table v-loading="loading" :data="recentList" border size="small">
        <el-table-column label="申报单号" prop="declarationNo" width="170" :show-overflow-tooltip="true" />
        <el-table-column label="细则标题" prop="regulationTitle" :show-overflow-tooltip="true" />
        <el-table-column label="总分" prop="totalScore" width="90" align="center">
          <template slot-scope="scope">
            <span style="font-weight: bold;">{{ scope.row.totalScore != null && scope.row.status !== '0' ? scope.row.totalScore : '-' }}</span>
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
        <el-table-column label="操作" align="center" width="80">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click="goTo('/assess/declaration')">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-button v-for="(action, idx) in quickActions" :key="idx"
        :type="action.type" :plain="action.plain" :icon="action.icon" size="small"
        @click="goTo(action.path)">
        {{ action.label }}
      </el-button>
    </div>
  </div>
</template>

<script>
import { listMaterial } from "@/api/assess/material";
import { listDeclaration } from "@/api/assess/declaration";
import { statsOverview } from "@/api/assess/stats";

export default {
  name: "Index",
  data() {
    return {
      loading: true,
      statCards: [],
      recentList: [],
      quickActions: []
    };
  },
  computed: {
    roles() {
      return this.$store.getters.roles || [];
    },
    isStudent() {
      return this.roles.includes("assess_student");
    },
    isCounselor() {
      return this.roles.includes("assess_counselor") || this.roles.includes("admin");
    },
    isCommittee() {
      return this.roles.includes("assess_committee");
    }
  },
  created() {
    this.initQuickActions();
    this.fetchData();
  },
  methods: {
    goTo(path) {
      this.$router.push(path).catch(() => {});
    },
    initQuickActions() {
      if (this.isStudent) {
        this.quickActions = [
          { label: "上传材料", icon: "el-icon-upload", type: "primary", plain: true, path: "/assess/material" },
          { label: "新建申报", icon: "el-icon-plus", type: "primary", plain: false, path: "/assess/declaration" }
        ];
      } else if (this.isCounselor) {
        this.quickActions = [
          { label: "去审核", icon: "el-icon-check", type: "primary", plain: false, path: "/assess/review" }
        ];
      } else if (this.isCommittee) {
        this.quickActions = [
          { label: "查看班级统计", icon: "el-icon-data-analysis", type: "primary", plain: false, path: "/assess/stats" }
        ];
      }
    },
    fetchData() {
      this.loading = true;
      if (this.isStudent) {
        this.fetchStudentData();
      } else if (this.isCounselor) {
        this.fetchCounselorData();
      } else if (this.isCommittee) {
        this.fetchCommitteeData();
      } else {
        this.loading = false;
      }
    },
    /** 学生视角 */
    fetchStudentData() {
      const pMaterial = listMaterial({ pageNum: 1, pageSize: 1 }).then(r => r.total || 0).catch(() => 0);
      const pDeclTotal = listDeclaration({ pageNum: 1, pageSize: 1 }).then(r => r.total || 0).catch(() => 0);
      const pPending = listDeclaration({ pageNum: 1, pageSize: 1, status: "1" }).then(r => r.total || 0).catch(() => 0);
      const pScore = listDeclaration({ pageNum: 1, pageSize: 50 }).then(r => {
        const rows = r.rows || [];
        return rows
          .filter(row => row.status !== "0" && row.totalScore != null)
          .reduce((sum, row) => sum + Number(row.totalScore), 0);
      }).catch(() => 0);
      const pRecent = listDeclaration({ pageNum: 1, pageSize: 5 }).then(r => r.rows || []).catch(() => []);

      Promise.all([pMaterial, pDeclTotal, pPending, pScore, pRecent]).then(([materialTotal, declTotal, pending, score, recent]) => {
        this.statCards = [
          { label: "我的材料", value: materialTotal, color: "#409EFF", path: "/assess/material" },
          { label: "我的申报", value: declTotal, color: "#409EFF", path: "/assess/declaration" },
          { label: "待审核", value: pending, color: "#E6A23C", path: "/assess/declaration" },
          { label: "累计得分", value: score, color: "#409EFF", path: "/assess/declaration" }
        ];
        this.recentList = recent;
        this.loading = false;
      });
    },
    /** 辅导员/admin 视角 */
    fetchCounselorData() {
      const pPending = listDeclaration({ pageNum: 1, pageSize: 1, status: "1" }).then(r => r.total || 0).catch(() => 0);
      const pPassed = listDeclaration({ pageNum: 1, pageSize: 1, status: "4" }).then(r => r.total || 0).catch(() => 0);
      const pRejected = listDeclaration({ pageNum: 1, pageSize: 1, status: "5" }).then(r => r.total || 0).catch(() => 0);
      const pAll = listDeclaration({ pageNum: 1, pageSize: 1 }).then(r => r.total || 0).catch(() => 0);
      const pRecent = listDeclaration({ pageNum: 1, pageSize: 5 }).then(r => r.rows || []).catch(() => []);

      Promise.all([pPending, pPassed, pRejected, pAll, pRecent]).then(([pending, passed, rejected, all, recent]) => {
        this.statCards = [
          { label: "待审核", value: pending, color: "#E6A23C", path: "/assess/review" },
          { label: "全部通过", value: passed, color: "#409EFF", path: "/assess/declaration" },
          { label: "已驳回", value: rejected, color: "#F56C6C", path: "/assess/declaration" },
          { label: "申报总数", value: all, color: "#409EFF", path: "/assess/declaration" }
        ];
        this.recentList = recent;
        this.loading = false;
      });
    },
    /** 班委视角 */
    fetchCommitteeData() {
      const pOverview = statsOverview({}).then(r => r.data || {}).catch(() => ({}));
      const pRecent = listDeclaration({ pageNum: 1, pageSize: 5 }).then(r => r.rows || []).catch(() => []);

      Promise.all([pOverview, pRecent]).then(([overview, recent]) => {
        this.statCards = [
          { label: "学生数", value: overview.studentCount || 0, color: "#409EFF", path: "/assess/stats" },
          { label: "待审核", value: overview.pendingCount || 0, color: "#E6A23C", path: "/assess/stats" },
          { label: "已通过", value: overview.passedCount || 0, color: "#67C23A", path: "/assess/stats" },
          { label: "平均分", value: overview.avgScore != null ? overview.avgScore : "-", color: "#409EFF", path: "/assess/stats" }
        ];
        this.recentList = recent;
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
    }
  }
};
</script>

<style scoped>
.assess-home {
  padding: 20px;
}

/* hero 渐变区（约 70px，深蓝渐变，左对齐） */
.hero-section {
  text-align: left;
  padding: 18px 24px;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  color: #fff;
}
.hero-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px 0;
  letter-spacing: 2px;
}
.hero-subtitle {
  font-size: 13px;
  margin: 0;
  opacity: 0.8;
}

/* 数字卡片区 */
.stat-cards {
  margin-bottom: 20px;
}
.stat-card {
  cursor: pointer;
  text-align: center;
  padding: 10px 0;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-3px);
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

/* 最近申报表 */
.recent-card {
  margin-bottom: 20px;
}
.recent-header {
  display: flex;
  align-items: center;
}
.recent-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 快捷操作 */
.quick-actions {
  text-align: center;
  padding: 10px 0;
}
.quick-actions .el-button {
  margin: 0 8px;
}
</style>
