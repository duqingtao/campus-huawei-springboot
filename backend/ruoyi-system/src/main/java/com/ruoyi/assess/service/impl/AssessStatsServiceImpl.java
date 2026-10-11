package com.ruoyi.assess.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.assess.domain.AssessStatsOverview;
import com.ruoyi.assess.domain.AssessStatsStudent;
import com.ruoyi.assess.mapper.AssessStatsMapper;
import com.ruoyi.assess.service.IAssessStatsService;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 班级统计 服务层实现
 * <p>
 * 只读模块，数据隔离逻辑：
 * <ul>
 *   <li>班委(assess_committee)：强制 deptId = 当前用户 deptId，只看本班</li>
 *   <li>辅导员(assess_counselor)：使用传入的 deptId，不传查全部</li>
 *   <li>管理员：使用传入的 deptId，可空</li>
 * </ul>
 * 学生角色未分配统计权限，天然拦截
 *
 * @author ruoyi
 */
@Service
public class AssessStatsServiceImpl implements IAssessStatsService
{
    @Autowired
    private AssessStatsMapper statsMapper;

    /**
     * 班级概览统计（含数据隔离）
     */
    @Override
    public AssessStatsOverview selectOverview(Long deptId)
    {
        deptId = resolveDeptId(deptId);
        AssessStatsOverview overview = statsMapper.selectOverview(deptId);
        if (overview == null)
        {
            overview = new AssessStatsOverview();
            overview.setStudentCount(0);
            overview.setPendingCount(0);
            overview.setReviewingCount(0);
            overview.setPassedCount(0);
            overview.setRejectedCount(0);
            overview.setAvgScore(java.math.BigDecimal.ZERO);
        }
        return overview;
    }

    /**
     * 学生申报明细列表（含数据隔离）
     */
    @Override
    public List<AssessStatsStudent> selectStudentList(AssessStatsStudent query)
    {
        query.setDeptId(resolveDeptId(query.getDeptId()));
        return statsMapper.selectStudentList(query);
    }

    // ========== 私有方法 ==========

    /**
     * 解析部门ID：班委强制使用自身deptId，辅导员/管理员使用传入值
     *
     * @param deptId 前端传入的部门ID
     * @return 实际使用的部门ID
     */
    private Long resolveDeptId(Long deptId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysRole> roles = loginUser.getUser().getRoles();

        boolean isCommittee = roles != null && roles.stream()
                .anyMatch(r -> "assess_committee".equals(r.getRoleKey()));

        if (SecurityUtils.isAdmin())
        {
            // 管理员：使用传入的 deptId（可空=全部）
            return deptId;
        }
        else if (isCommittee)
        {
            // 班委：强制使用自身 dept_id，忽略前端传入
            return SecurityUtils.getDeptId();
        }
        // 辅导员：使用传入的 deptId（可空=全部）
        return deptId;
    }
}
