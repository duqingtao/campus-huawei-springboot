package com.ruoyi.assess.ai.dto;

/**
 * OCR识别结果DTO
 *
 * @author ruoyi
 */
public class OcrResult
{
    /** 是否成功 */
    private boolean success;

    /** OCR识别全文 */
    private String ocrText;

    /** 失败原因(success=false时有值) */
    private String errorMessage;

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getOcrText()
    {
        return ocrText;
    }

    public void setOcrText(String ocrText)
    {
        this.ocrText = ocrText;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    /** 快速构造成功结果 */
    public static OcrResult ok(String text)
    {
        OcrResult r = new OcrResult();
        r.setSuccess(true);
        r.setOcrText(text);
        return r;
    }

    /** 快速构造失败结果 */
    public static OcrResult fail(String message)
    {
        OcrResult r = new OcrResult();
        r.setSuccess(false);
        r.setErrorMessage(message);
        return r;
    }
}
