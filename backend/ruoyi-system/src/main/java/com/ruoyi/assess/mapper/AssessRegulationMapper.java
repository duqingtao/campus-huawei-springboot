package com.ruoyi.assess.mapper;

import java.util.List;
import com.ruoyi.assess.domain.AssessRegulation;

/**
 * 综测细则 数据层
 *
 * @author ruoyi
 */
public interface AssessRegulationMapper
{
    /**
     * 查询综测细则列表
     *
     * @param regulation 综测细则
     * @return 综测细则集合
     */
    public List<AssessRegulation> selectRegulationList(AssessRegulation regulation);

    /**
     * 通过细则ID查询细则信息
     *
     * @param regulationId 细则ID
     * @return 细则对象
     */
    public AssessRegulation selectRegulationById(Long regulationId);

    /**
     * 新增综测细则
     *
     * @param regulation 综测细则
     * @return 结果
     */
    public int insertRegulation(AssessRegulation regulation);

    /**
     * 修改综测细则
     *
     * @param regulation 综测细则
     * @return 结果
     */
    public int updateRegulation(AssessRegulation regulation);

    /**
     * 逻辑删除综测细则
     *
     * @param regulationId 细则ID
     * @return 结果
     */
    public int deleteRegulationById(Long regulationId);

    /**
     * 批量逻辑删除综测细则
     *
     * @param regulationIds 细则ID数组
     * @return 结果
     */
    public int deleteRegulationByIds(Long[] regulationIds);

    /**
     * 校验细则编号是否唯一
     *
     * @param regulationNo 细则编号
     * @return 细则对象
     */
    public AssessRegulation checkRegulationNoUnique(String regulationNo);
}
