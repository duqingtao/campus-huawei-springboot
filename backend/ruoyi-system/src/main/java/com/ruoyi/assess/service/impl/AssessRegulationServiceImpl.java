package com.ruoyi.assess.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.assess.domain.AssessRegulation;
import com.ruoyi.assess.mapper.AssessRegulationClauseMapper;
import com.ruoyi.assess.mapper.AssessRegulationMapper;
import com.ruoyi.assess.service.IAssessRegulationService;

/**
 * 综测细则 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssessRegulationServiceImpl implements IAssessRegulationService
{
    @Autowired
    private AssessRegulationMapper regulationMapper;

    @Autowired
    private AssessRegulationClauseMapper clauseMapper;

    /**
     * 查询综测细则列表
     */
    @Override
    public List<AssessRegulation> selectRegulationList(AssessRegulation regulation)
    {
        return regulationMapper.selectRegulationList(regulation);
    }

    /**
     * 通过细则ID查询细则信息
     */
    @Override
    public AssessRegulation selectRegulationById(Long regulationId)
    {
        return regulationMapper.selectRegulationById(regulationId);
    }

    /**
     * 新增综测细则
     */
    @Override
    public int insertRegulation(AssessRegulation regulation)
    {
        return regulationMapper.insertRegulation(regulation);
    }

    /**
     * 修改综测细则
     */
    @Override
    public int updateRegulation(AssessRegulation regulation)
    {
        return regulationMapper.updateRegulation(regulation);
    }

    /**
     * 删除综测细则（连带逻辑删除其下所有条款）
     */
    @Override
    @Transactional
    public int deleteRegulationByIds(Long[] regulationIds)
    {
        for (Long regulationId : regulationIds)
        {
            // 连带逻辑删除该细则下所有条款
            clauseMapper.deleteClauseByRegulationId(regulationId);
        }
        return regulationMapper.deleteRegulationByIds(regulationIds);
    }

    /**
     * 校验细则编号是否唯一
     */
    @Override
    public boolean checkRegulationNoUnique(AssessRegulation regulation)
    {
        Long regulationId = StringUtils.isNull(regulation.getRegulationId()) ? -1L : regulation.getRegulationId();
        AssessRegulation info = regulationMapper.checkRegulationNoUnique(regulation.getRegulationNo());
        if (StringUtils.isNotNull(info) && info.getRegulationId().longValue() != regulationId.longValue())
        {
            return false;
        }
        return true;
    }
}
