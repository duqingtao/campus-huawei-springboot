package com.ruoyi.assess.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.assess.domain.AssessRuleConfig;
import com.ruoyi.assess.mapper.AssessRuleConfigMapper;
import com.ruoyi.assess.service.IAssessRuleConfigService;

/**
 * 综测规则引擎配置 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssessRuleConfigServiceImpl implements IAssessRuleConfigService
{
    @Autowired
    private AssessRuleConfigMapper ruleConfigMapper;

    @Override
    public List<AssessRuleConfig> selectRuleConfigList(AssessRuleConfig ruleConfig)
    {
        return ruleConfigMapper.selectRuleConfigList(ruleConfig);
    }

    @Override
    public AssessRuleConfig selectRuleConfigById(Long ruleId)
    {
        return ruleConfigMapper.selectRuleConfigById(ruleId);
    }

    @Override
    public int insertRuleConfig(AssessRuleConfig ruleConfig)
    {
        return ruleConfigMapper.insertRuleConfig(ruleConfig);
    }

    @Override
    public int updateRuleConfig(AssessRuleConfig ruleConfig)
    {
        return ruleConfigMapper.updateRuleConfig(ruleConfig);
    }

    @Override
    public int deleteRuleConfigByIds(Long[] ruleIds)
    {
        return ruleConfigMapper.deleteRuleConfigByIds(ruleIds);
    }

    @Override
    public List<AssessRuleConfig> selectEnabledRuleByRegulationId(Long regulationId)
    {
        return ruleConfigMapper.selectEnabledRuleByRegulationId(regulationId);
    }
}
