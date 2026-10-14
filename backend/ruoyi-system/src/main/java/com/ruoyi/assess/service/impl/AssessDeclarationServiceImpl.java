package com.ruoyi.assess.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.assess.ai.ClauseMatchService;
import com.ruoyi.assess.ai.dto.MatchRequest;
import com.ruoyi.assess.ai.dto.MatchResponse;
import com.ruoyi.assess.domain.AssessDeclaration;
import com.ruoyi.assess.domain.AssessDeclarationItem;
import com.ruoyi.assess.domain.AssessMaterial;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.assess.domain.AssessRuleConfig;
import com.ruoyi.assess.engine.RuleEngine;
import com.ruoyi.assess.engine.RuleEngineResult;
import com.ruoyi.assess.mapper.AssessDeclarationItemMapper;
import com.ruoyi.assess.mapper.AssessDeclarationMapper;
import com.ruoyi.assess.mapper.AssessMaterialMapper;
import com.ruoyi.assess.mapper.AssessRegulationClauseMapper;
import com.ruoyi.assess.mapper.AssessRuleConfigMapper;
import com.ruoyi.assess.service.IAssessDeclarationService;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 综测申报单 服务层实现
 * <p>
 * 含核心提交流程：AI匹配 → 规则引擎 → 定分 → 汇总
 *
 * @author ruoyi
 */
@Service
public class AssessDeclarationServiceImpl implements IAssessDeclarationService
{
    private static final Logger log = LoggerFactory.getLogger(AssessDeclarationServiceImpl.class);

    @Autowired
    private AssessDeclarationMapper declarationMapper;

    @Autowired
    private AssessDeclarationItemMapper itemMapper;

    @Autowired
    private AssessMaterialMapper materialMapper;

    @Autowired
    private AssessRegulationClauseMapper clauseMapper;

    @Autowired
    private AssessRuleConfigMapper ruleConfigMapper;

    @Autowired
    private ClauseMatchService clauseMatchService;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 查询综测申报单列表（含数据隔离）
     */
    @Override
    public List<AssessDeclaration> selectDeclarationList(AssessDeclaration declaration)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysRole> roles = loginUser.getUser().getRoles();

        boolean isStudent = roles != null && roles.stream()
                .anyMatch(r -> "assess_student".equals(r.getRoleKey()));
        boolean isCommittee = roles != null && roles.stream()
                .anyMatch(r -> "assess_committee".equals(r.getRoleKey()));

        if (SecurityUtils.isAdmin())
        {
            // 管理员：看全部
        }
        else if (isStudent)
        {
            // 学生：只看自己的申报
            declaration.setUserId(SecurityUtils.getUserId());
        }
        else if (isCommittee)
        {
            // 班委：按 dept_id 过滤
            declaration.setDeptId(SecurityUtils.getDeptId());
        }
        // 辅导员（data_scope=1）：不加过滤

        return declarationMapper.selectDeclarationList(declaration);
    }

    /**
     * 通过申报单ID查询申报单信息（含明细列表）
     */
    @Override
    public AssessDeclaration selectDeclarationById(Long declarationId)
    {
        checkDeclarationOwnership(declarationId);
        AssessDeclaration declaration = declarationMapper.selectDeclarationById(declarationId);
        if (declaration != null)
        {
            List<AssessDeclarationItem> items = itemMapper.selectItemByDeclarationId(declarationId);
            declaration.setItems(items);
        }
        return declaration;
    }

    /**
     * 新增综测申报单（含自动生成单号、创建明细）
     */
    @Override
    @Transactional
    public int insertDeclaration(AssessDeclaration declaration)
    {
        // 生成申报单号
        declaration.setDeclarationNo(generateDeclarationNo());

        // 从当前登录用户获取学生信息
        LoginUser loginUser = SecurityUtils.getLoginUser();
        declaration.setUserId(loginUser.getUserId());
        declaration.setStudentName(loginUser.getUser().getNickName());
        if (loginUser.getUser().getDept() != null)
        {
            declaration.setClassName(loginUser.getUser().getDept().getDeptName());
        }
        declaration.setTotalScore(BigDecimal.ZERO);
        declaration.setStatus("0");

        // 插入申报单
        declarationMapper.insertDeclaration(declaration);

        // 为每个材料创建明细
        if (declaration.getMaterialIds() != null && !declaration.getMaterialIds().isEmpty())
        {
            List<AssessDeclarationItem> items = new ArrayList<>();
            for (Long materialId : declaration.getMaterialIds())
            {
                AssessDeclarationItem item = new AssessDeclarationItem();
                item.setDeclarationId(declaration.getDeclarationId());
                item.setMaterialId(materialId);
                item.setClauseId(0L);
                item.setMatchStatus("0");
                item.setCreateBy(declaration.getCreateBy());
                items.add(item);
            }
            itemMapper.batchInsertItem(items);
        }

        return 1;
    }

    /**
     * 修改综测申报单（仅草稿状态可改）
     */
    @Override
    public int updateDeclaration(AssessDeclaration declaration)
    {
        checkDeclarationOwnership(declaration.getDeclarationId());
        checkDraftStatus(declaration.getDeclarationId());
        return declarationMapper.updateDeclaration(declaration);
    }

    /**
     * 批量逻辑删除综测申报单（仅草稿状态可删）
     */
    @Override
    @Transactional
    public int deleteDeclarationByIds(Long[] declarationIds)
    {
        for (Long declarationId : declarationIds)
        {
            checkDeclarationOwnership(declarationId);
            checkDraftStatus(declarationId);
            itemMapper.deleteItemByDeclarationId(declarationId);
        }
        return declarationMapper.deleteDeclarationByIds(declarationIds);
    }

    /**
     * 提交申报单 — 核心流程
     * <p>
     * 1. 校验：存在 + 草稿 + 本人 + 有明细
     * 2. AI匹配：逐条明细调ClauseMatchService
     * 3. 规则引擎：time_window → mutex → cap
     * 4. 定分：final_score = rule_adjusted_score
     * 5. 汇总：total_score = sum(final_score), status=1
     */
    @Override
    @Transactional
    public int submitDeclaration(Long declarationId, String username)
    {
        // ── 1. 校验 ──
        AssessDeclaration declaration = declarationMapper.selectDeclarationById(declarationId);
        if (declaration == null)
        {
            throw new ServiceException("申报单不存在或已被删除");
        }
        if (!"0".equals(declaration.getStatus()))
        {
            throw new ServiceException("仅草稿状态的申报单可以提交，当前状态：" + declaration.getStatus());
        }
        if (!SecurityUtils.isAdmin() && declaration.getUserId().longValue() != SecurityUtils.getUserId().longValue())
        {
            throw new ServiceException("只能提交本人的申报单");
        }

        List<AssessDeclarationItem> items = itemMapper.selectItemByDeclarationId(declarationId);
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("申报单没有明细，无法提交");
        }

        // 获取该细则下所有条款（作为候选）
        List<AssessRegulationClause> allClauses = clauseMapper.selectLeafClauseByRegulationId(declaration.getRegulationId());
        if (allClauses == null || allClauses.isEmpty())
        {
            throw new ServiceException("该细则下没有条款，无法匹配");
        }

        // ── 2. AI条款匹配 ──
        for (AssessDeclarationItem item : items)
        {
            MatchRequest request = new MatchRequest();
            request.setMaterialId(item.getMaterialId());

            // 获取材料信息
            if (item.getMaterialId() != null && item.getMaterialId() > 0)
            {
                AssessMaterial material = materialMapper.selectMaterialById(item.getMaterialId());
                if (material != null)
                {
                    request.setMaterialName(material.getMaterialName());
                    request.setMaterialType(material.getMaterialType());
                    request.setOcrText(material.getOcrText());
                }
            }

            MatchResponse response = clauseMatchService.match(request, allClauses);

            // 写回明细的AI匹配结果
            item.setClauseId(response.getClauseId() != null ? response.getClauseId() : 0L);
            item.setSuggestedScore(response.getSuggestedScore());
            item.setEvidenceClauseNo(response.getEvidenceClauseNo());
            item.setEvidenceClauseText(response.getEvidenceClauseText());
            item.setEvidenceConfidence(response.getEvidenceConfidence());
            item.setMatchReason(response.getMatchReason());
            item.setMatchStatus(response.getMatchStatus());
            // 同步条款的扩展字段（clauseType/clauseNo/clauseTitle/maxScore），供规则引擎使用
            if (response.getClauseId() != null && response.getClauseId() > 0)
            {
                for (AssessRegulationClause clause : allClauses)
                {
                    if (clause.getClauseId().equals(response.getClauseId()))
                    {
                        item.setClauseType(clause.getClauseType());
                        item.setClauseNo(clause.getClauseNo());
                        item.setClauseTitle(clause.getClauseTitle());
                        item.setMaxScore(clause.getMaxScore());
                        break;
                    }
                }
            }
        }

        // ── 3. 规则引擎校验 ──
        List<AssessRuleConfig> rules = ruleConfigMapper.selectEnabledRuleByRegulationId(declaration.getRegulationId());
        RuleEngineResult engineResult = ruleEngine.executeRules(items, rules);

        // ── 4. 写回明细：final_score = rule_adjusted_score ──
        for (AssessDeclarationItem item : items)
        {
            // 找到对应的规则引擎结果
            for (com.ruoyi.assess.engine.ItemRuleResult ir : engineResult.getItemResults())
            {
                if (ir.getItemId().longValue() == item.getItemId().longValue())
                {
                    item.setRuleAdjustedScore(ir.getRuleAdjustedScore());
                    item.setFinalScore(ir.getFinalScore());
                    // rule_check_result 序列化为JSON
                    try
                    {
                        item.setRuleCheckResult(objectMapper.writeValueAsString(ir.getCheckDetails()));
                    }
                    catch (Exception e)
                    {
                        item.setRuleCheckResult("[]");
                    }
                    break;
                }
            }
            item.setUpdateBy(username);
            itemMapper.updateItem(item);
        }

        // ── 5. 更新申报单：汇总总分 + 状态 ──
        AssessDeclaration update = new AssessDeclaration();
        update.setDeclarationId(declarationId);
        update.setTotalScore(engineResult.getTotalScore());
        update.setStatus("1");
        update.setSubmitTime(new Date());
        update.setUpdateBy(username);
        declarationMapper.updateDeclaration(update);

        log.info("申报单[{}]提交成功，总分={}", declarationId, engineResult.getTotalScore());
        return 1;
    }

    // ========== 私有方法 ==========

    /**
     * 行级归属校验（防水平越权）
     * <p>
     * 学生只能操作自己的申报单；班委只能操作本部门学生的申报单；辅导员/admin 不受限。
     */
    private void checkDeclarationOwnership(Long declarationId)
    {
        AssessDeclaration declaration = declarationMapper.selectDeclarationById(declarationId);
        if (declaration == null)
        {
            throw new ServiceException("数据不存在或已被删除");
        }
        if (SecurityUtils.isAdmin())
        {
            return;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysRole> roles = loginUser.getUser().getRoles();
        boolean isStudent = roles != null && roles.stream()
                .anyMatch(r -> "assess_student".equals(r.getRoleKey()));
        boolean isCommittee = roles != null && roles.stream()
                .anyMatch(r -> "assess_committee".equals(r.getRoleKey()));
        if (isCommittee)
        {
            // 班委：校验申报单的创建学生是否属于本部门
            SysUser declareUser = sysUserMapper.selectUserById(declaration.getUserId());
            if (declareUser == null || declareUser.getDeptId() == null
                    || declareUser.getDeptId().longValue() != SecurityUtils.getDeptId().longValue())
            {
                throw new ServiceException("无权操作他人数据");
            }
        }
        else if (isStudent && declaration.getUserId().longValue() != SecurityUtils.getUserId().longValue())
        {
            throw new ServiceException("无权操作他人数据");
        }
    }

    /**
     * 生成申报单号：SB + yyyyMMdd + 4位序号
     * synchronized 防并发
     */
    private synchronized String generateDeclarationNo()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String prefix = "SB" + dateStr;

        String maxNo = declarationMapper.selectMaxDeclarationNoToday(prefix + "%");
        int seq = 1;
        if (StringUtils.isNotEmpty(maxNo))
        {
            // 取最后4位序号
            String seqStr = maxNo.substring(maxNo.length() - 4);
            try
            {
                seq = Integer.parseInt(seqStr) + 1;
            }
            catch (NumberFormatException e)
            {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    /**
     * 校验申报单是否为草稿状态
     */
    private void checkDraftStatus(Long declarationId)
    {
        AssessDeclaration declaration = declarationMapper.selectDeclarationById(declarationId);
        if (declaration == null)
        {
            throw new ServiceException("申报单不存在或已被删除");
        }
        if (!"0".equals(declaration.getStatus()))
        {
            throw new ServiceException("仅草稿状态的申报单可以操作，当前状态：" + declaration.getStatus());
        }
    }
}
