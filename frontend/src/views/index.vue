<template>
  <div class="app-container assess-home">
    <!-- 顶部大标题区 -->
    <div class="hero-section">
      <h1 class="hero-title">综测智办</h1>
      <p class="hero-subtitle-main">大学生综合素质测评智能认定系统</p>
      <p class="hero-subtitle-desc">
        基于华为云MaaS大模型，实现OCR材料识别、AI条款匹配、规则引擎定分、三级审核全流程自动化
      </p>
    </div>

    <!-- 角色入口卡片（按权限显隐） -->
    <el-row :gutter="20" class="role-cards" v-if="hasAnyAccess">
      <el-col :xs="24" :sm="8" v-hasPermi="['assess:material:list']">
        <el-card shadow="hover" class="role-card" @click.native="goTo('/assess/material')">
          <div class="role-card-inner">
            <div class="role-icon role-icon-student">
              <i class="el-icon-upload"></i>
            </div>
            <h3 class="role-title">学生端</h3>
            <p class="role-desc">上传证明材料，AI自动识别并计算加分</p>
            <span class="role-action">进入材料管理 <i class="el-icon-arrow-right"></i></span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" v-hasPermi="['assess:stats:list']">
        <el-card shadow="hover" class="role-card" @click.native="goTo('/assess/stats')">
          <div class="role-card-inner">
            <div class="role-icon role-icon-monitor">
              <i class="el-icon-data-analysis"></i>
            </div>
            <h3 class="role-title">班委端</h3>
            <p class="role-desc">查看班级统计，导出Excel报表</p>
            <span class="role-action">进入班级统计 <i class="el-icon-arrow-right"></i></span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" v-hasPermi="['assess:review:list']">
        <el-card shadow="hover" class="role-card" @click.native="goTo('/assess/review')">
          <div class="role-card-inner">
            <div class="role-icon role-icon-reviewer">
              <i class="el-icon-check"></i>
            </div>
            <h3 class="role-title">辅导员端</h3>
            <p class="role-desc">待审列表，一键审核（通过/存疑/驳回）</p>
            <span class="role-action">进入审核管理 <i class="el-icon-arrow-right"></i></span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 无权限提示 -->
    <el-empty
      v-if="!hasAnyAccess"
      description="您暂无综测智办相关权限，请联系管理员分配角色"
      :image-size="120"
      style="margin-bottom: 30px;"
    />

    <!-- 底部核心特性区 -->
    <div class="features-section">
      <h2 class="features-heading">核心特性</h2>
      <el-row :gutter="20" class="feature-cards">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="feature-card">
            <div class="feature-card-inner">
              <div class="feature-icon">
                <i class="el-icon-picture-outline"></i>
              </div>
              <h4 class="feature-title">OCR智能识别</h4>
              <p class="feature-desc">支持图片/PDF材料，自动提取关键信息</p>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="feature-card">
            <div class="feature-card-inner">
              <div class="feature-icon">
                <i class="el-icon-magic-stick"></i>
              </div>
              <h4 class="feature-title">AI条款匹配</h4>
              <p class="feature-desc">大模型语义理解，精准匹配综测条款</p>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="feature-card">
            <div class="feature-card-inner">
              <div class="feature-icon">
                <i class="el-icon-set-up"></i>
              </div>
              <h4 class="feature-title">规则引擎定分</h4>
              <p class="feature-desc">cap封顶/mutex互斥/time_window时间窗，确保公平公正</p>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="feature-card">
            <div class="feature-card-inner">
              <div class="feature-icon">
                <i class="el-icon-document-checked"></i>
              </div>
              <h4 class="feature-title">全程留痕</h4>
              <p class="feature-desc">审核记录可追溯，驳回意见AI辅助生成</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: "Index",
  computed: {
    /** 检查当前用户是否拥有综测模块任意权限 */
    hasAnyAccess() {
      const perms = this.$store.getters.permissions || [];
      // 管理员拥有 *:*:* 通配权限
      if (perms.includes("*:*:*")) return true;
      // 普通用户按前缀匹配三个角色对应的权限域
      return perms.some(
        p =>
          p.startsWith("assess:material:") ||
          p.startsWith("assess:stats:") ||
          p.startsWith("assess:review:")
      );
    }
  },
  methods: {
    goTo(path) {
      this.$router.push(path).catch(() => {});
    }
  }
};
</script>

<style scoped>
.assess-home {
  padding: 20px;
}

/* 顶部大标题区 */
.hero-section {
  text-align: center;
  padding: 40px 20px 30px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 8px;
  margin-bottom: 30px;
  color: #fff;
}
.hero-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 10px 0;
  letter-spacing: 4px;
}
.hero-subtitle-main {
  font-size: 20px;
  font-weight: 400;
  margin: 0 0 12px 0;
  opacity: 0.95;
}
.hero-subtitle-desc {
  font-size: 14px;
  margin: 0;
  opacity: 0.8;
  line-height: 1.6;
}

/* 角色入口卡片 */
.role-cards {
  margin-bottom: 30px;
}
.role-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 15px;
  border-top: 3px solid transparent;
}
.role-card:hover {
  transform: translateY(-4px);
}
.role-card-inner {
  text-align: center;
  padding: 10px 0;
}
.role-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 28px;
  color: #fff;
}
.role-icon-student {
  background: #409eff;
}
.role-icon-monitor {
  background: #67c23a;
}
.role-icon-reviewer {
  background: #e6a23c;
}
.role-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
}
.role-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px 0;
  line-height: 1.5;
}
.role-action {
  font-size: 13px;
  color: #409eff;
}

/* 核心特性区 */
.features-section {
  margin-top: 10px;
}
.features-heading {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}
.feature-card {
  margin-bottom: 15px;
  transition: box-shadow 0.3s;
}
.feature-card-inner {
  text-align: center;
  padding: 8px 0;
}
.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 24px;
  color: #409eff;
}
.feature-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #303133;
}
.feature-desc {
  font-size: 12px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
}
</style>
