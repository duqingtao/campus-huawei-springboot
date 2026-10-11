package com.ruoyi.assess.ai;

import com.ruoyi.assess.ai.dto.OcrResult;
import com.ruoyi.assess.domain.AssessMaterial;

/**
 * 材料OCR识别服务接口
 * <p>
 * 统一定义在 com.ruoyi.assess.ai 包，禁止散落在业务代码中。
 * 支持PDF(PDFBox文本抽取→扫描件转VLM)和图片(VLM直接识别)。
 *
 * @author ruoyi
 */
public interface MaterialOcrService
{
    /**
     * 对材料文件执行OCR识别
     *
     * @param material 材料信息(含fileUrl/fileType/fileSize)
     * @return OCR结果(成功含ocrText，失败含errorMessage)
     */
    OcrResult processOcr(AssessMaterial material);
}
