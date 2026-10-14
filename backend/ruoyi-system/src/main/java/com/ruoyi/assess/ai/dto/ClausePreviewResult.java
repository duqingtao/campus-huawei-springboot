package com.ruoyi.assess.ai.dto;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.assess.domain.AssessRegulationClause;

/**
 * 条款预览结果 DTO
 * <p>
 * AI 提取后返回给前端预览，不落库。
 * clauses 为模块树（含 children），meta 记录文本统计与分块信息。
 *
 * @author ruoyi
 */
public class ClausePreviewResult
{
    /** 提取出的条款模块树 */
    private List<AssessRegulationClause> clauses = new ArrayList<>();

    /** 元数据 */
    private Meta meta = new Meta();

    public List<AssessRegulationClause> getClauses()
    {
        return clauses;
    }

    public void setClauses(List<AssessRegulationClause> clauses)
    {
        this.clauses = clauses;
    }

    public Meta getMeta()
    {
        return meta;
    }

    public void setMeta(Meta meta)
    {
        this.meta = meta;
    }

    public static class Meta
    {
        /** 原文本字符数 */
        private int textLength;

        /** 模型实际消费字符数 */
        private int consumedLength;

        /** 是否发生截断 */
        private boolean truncated;

        /** 分块数 */
        private int chunkCount;

        /** 各块错误信息 */
        private List<String> errors = new ArrayList<>();

        public int getTextLength()
        {
            return textLength;
        }

        public void setTextLength(int textLength)
        {
            this.textLength = textLength;
        }

        public int getConsumedLength()
        {
            return consumedLength;
        }

        public void setConsumedLength(int consumedLength)
        {
            this.consumedLength = consumedLength;
        }

        public boolean isTruncated()
        {
            return truncated;
        }

        public void setTruncated(boolean truncated)
        {
            this.truncated = truncated;
        }

        public int getChunkCount()
        {
            return chunkCount;
        }

        public void setChunkCount(int chunkCount)
        {
            this.chunkCount = chunkCount;
        }

        public List<String> getErrors()
        {
            return errors;
        }

        public void setErrors(List<String> errors)
        {
            this.errors = errors;
        }
    }
}
