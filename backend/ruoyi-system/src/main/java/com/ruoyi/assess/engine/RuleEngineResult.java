package com.ruoyi.assess.engine;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 规则引擎整体输出
 *
 * @author ruoyi
 */
public class RuleEngineResult
{
    /** 每条明细的校验结果 */
    private List<ItemRuleResult> itemResults;

    /** 申报单总分(各明细finalScore之和) */
    private BigDecimal totalScore;

    public List<ItemRuleResult> getItemResults()
    {
        return itemResults;
    }

    public void setItemResults(List<ItemRuleResult> itemResults)
    {
        this.itemResults = itemResults;
    }

    public BigDecimal getTotalScore()
    {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore)
    {
        this.totalScore = totalScore;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemResults", getItemResults())
            .append("totalScore", getTotalScore())
            .toString();
    }
}
