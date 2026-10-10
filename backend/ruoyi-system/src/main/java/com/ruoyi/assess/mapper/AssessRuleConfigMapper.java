package com.ruoyi.assess.mapper;

import java.util.List;
import com.ruoyi.assess.domain.AssessRuleConfig;

/**
 * 综测规则引擎配置 数据层
 *
 * @author ruoyi
 */
public interface AssessRuleConfigMapper
{
    /**
     * 查询规则配置列表
     */
    public List<AssessRuleConfig> selectRuleConfigList(AssessRuleConfig ruleConfig);

    /**
     * 通过规则ID查询规则配置信息
     */
    public AssessRuleConfig selectRuleConfigById(Long ruleId);

    /**
     * 新增规则配置
     */
    public int insertRuleConfig(AssessRuleConfig ruleConfig);

    /**
     * 修改规则配置
     */
    public int updateRuleConfig(AssessRuleConfig ruleConfig);

    /**
     * 批量逻辑删除规则配置
     */
    public int deleteRuleConfigByIds(Long[] ruleIds);

    /**
     * 根据细则ID查询启用的规则配置列表(按优先级排序)
     */
    public List<AssessRuleConfig> selectEnabledRuleByRegulationId(Long regulationId);
}
