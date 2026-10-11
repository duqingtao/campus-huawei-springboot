package com.ruoyi.assess.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.assess.domain.AssessStatsOverview;
import com.ruoyi.assess.domain.AssessStatsStudent;

/**
 * 班级统计 数据层
 *
 * @author ruoyi
 */
public interface AssessStatsMapper
{
    /**
     * 班级概览统计（聚合查询，单条返回）
     *
     * @param deptId 部门ID(班级)，null=全部
     * @return 概览统计结果
     */
    public AssessStatsOverview selectOverview(@Param("deptId") Long deptId);

    /**
     * 学生申报明细列表
     *
     * @param query 查询条件(含deptId/academicYear/semester/status)
     * @return 学生申报明细列表
     */
    public List<AssessStatsStudent> selectStudentList(AssessStatsStudent query);
}
