package com.ruoyi.assess.engine;

/**
 * 单条规则校验明细
 *
 * @author ruoyi
 */
public class CheckDetail
{
    /** 规则类型: cap / mutex / time_window */
    private String ruleType;

    /** 是否通过 */
    private boolean passed;

    /** 人可读的说明 */
    private String message;

    public CheckDetail()
    {
    }

    public CheckDetail(String ruleType, boolean passed, String message)
    {
        this.ruleType = ruleType;
        this.passed = passed;
        this.message = message;
    }

    public String getRuleType()
    {
        return ruleType;
    }

    public void setRuleType(String ruleType)
    {
        this.ruleType = ruleType;
    }

    public boolean isPassed()
    {
        return passed;
    }

    public void setPassed(boolean passed)
    {
        this.passed = passed;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
