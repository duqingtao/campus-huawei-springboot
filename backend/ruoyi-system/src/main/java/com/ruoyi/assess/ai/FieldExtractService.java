package com.ruoyi.assess.ai;

import java.util.List;
import com.ruoyi.assess.ai.dto.MaterialFieldDto;

/**
 * 材料字段结构化抽取服务接口(Step 1)
 * <p>
 * 从OCR文本中抽取结构化字段(award_name/award_level/award_date等)，
 * 每个字段携带置信度。解析失败的字段跳过不入库。
 * <p>
 * 统一定义在 com.ruoyi.assess.ai 包，禁止散落在业务代码中。
 *
 * @author ruoyi
 */
public interface FieldExtractService
{
    /**
     * 从OCR文本中抽取结构化字段
     *
     * @param ocrText      OCR识别全文
     * @param materialType 材料类型提示(如"竞赛证书"/"志愿证明")
     * @return 字段列表(解析失败的字段不包含在内)
     */
    List<MaterialFieldDto> extractFields(String ocrText, String materialType);
}
