package com.ruoyi.assess.service;

import java.util.List;
import com.ruoyi.assess.domain.AssessStatsOverview;
import com.ruoyi.assess.domain.AssessStatsStudent;

/**
 * 班级统计 服务层
 *
 * @author ruoyi
 */
public interface IAssessStatsService
{
    /**
     * 班级概览统计（含数据隔离）
     *
     * @param deptId 部门ID(班委忽略此参数强制用自身deptId，辅导员可传指定班)
     * @return 概览统计结果
     */
    public AssessStatsOverview selectOverview(Long deptId);

    /**
     * 学生申报明细列表（含数据隔离）
     *
     * @param query 查询条件
     * @return 学生申报明细列表
     */
    public List<AssessStatsStudent> selectStudentList(AssessStatsStudent query);
}
