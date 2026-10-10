package com.ruoyi.assess.ai;

import java.util.List;
import com.ruoyi.assess.ai.dto.MatchRequest;
import com.ruoyi.assess.ai.dto.MatchResponse;
import com.ruoyi.assess.domain.AssessRegulationClause;

/**
 * AI条款匹配服务接口
 * <p>
 * 统一定义在 com.ruoyi.assess.ai 包，禁止散落在业务代码中。
 * 当前使用 MockClauseMatchServiceImpl，后续真实盘古实现直接替换Mock，不改调用方。
 *
 * @author ruoyi
 */
public interface ClauseMatchService
{
    /**
     * 对单份材料进行条款语义匹配，产出建议分+证据+置信度
     * <p>
     * AGENTS.md 第5条合规：suggested_score 仅供参考，不直接入库为 final_score
     *
     * @param request         材料匹配入参(材料ID/名称/类型/OCR文本)
     * @param candidateClauses 候选条款列表(按material_type映射后传入)
     * @return 匹配响应(含建议分/证据/置信度/匹配状态)
     */
    MatchResponse match(MatchRequest request, List<AssessRegulationClause> candidateClauses);
}
