package com.ruoyi.assess.ai;

import java.util.List;
import com.ruoyi.assess.ai.dto.ClausePreviewResult;
import com.ruoyi.assess.domain.AssessRegulationClause;

/**
 * 条款抽取服务
 * <p>
 * 从综测细则文本中通过AI提取结构化条款。
 *
 * @author ruoyi
 */
public interface ClauseExtractService
{
    /**
     * 从文本中提取条款（含元数据，支持分块）
     * <p>
     * 超长文本按章节正则分块，逐块调 MaaS 解析后合并去重。
     * 返回 ClausePreviewResult 含条款模块树和 meta（文本统计/分块信息/错误）。
     *
     * @param text 综测细则全文
     * @return 预览结果，含条款列表和元数据
     */
    public ClausePreviewResult extractClausesWithMeta(String text);
}
