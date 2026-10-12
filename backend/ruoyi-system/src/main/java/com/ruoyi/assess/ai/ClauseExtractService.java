package com.ruoyi.assess.ai;

import java.util.List;
import com.ruoyi.assess.domain.AssessRegulationClause;

/**
 * 条款抽取服务
 * <p>
 * 从综测细则文本中通过AI提取结构化条款。
 * 仅填充 clauseNo/clauseTitle/clauseText/maxScore/clauseType 五个字段，
 * regulationId/parentId/sortOrder 等由调用方设置。
 *
 * @author ruoyi
 */
public interface ClauseExtractService
{
    /**
     * 从文本中提取条款
     *
     * @param text 综测细则全文
     * @return 提取出的条款列表，失败返回空列表
     */
    public List<AssessRegulationClause> extractClauses(String text);
}
