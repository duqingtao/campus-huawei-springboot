package com.ruoyi.assess.ai.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import com.ruoyi.assess.ai.MaterialOcrService;
import com.ruoyi.assess.ai.client.MaaSClient;
import com.ruoyi.assess.ai.config.AiConfig;
import com.ruoyi.assess.ai.dto.OcrResult;
import com.ruoyi.assess.domain.AssessMaterial;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;

/**
 * MaaS OCR服务实现
 * <p>
 * PDF：PDFBox抽文本 → 抽不出(扫描件) → 转PNG(前3页)走VLM
 * 图片：base64编码 → VLM(image_url格式)
 * <p>
 * 降级：ai.enabled=false或vlm-model为空时，PDF文本抽取仍可用，VLM部分置失败。
 * 单文件≤10MB校验。
 *
 * @author ruoyi
 */
@Service
public class MaaSOcrServiceImpl implements MaterialOcrService
{
    private static final Logger log = LoggerFactory.getLogger(MaaSOcrServiceImpl.class);

    /** 最大文件大小10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** VLM最多处理前3页 */
    private static final int MAX_VLM_PAGES = 3;

    /** VLM系统提示词：原样输出文字 */
    private static final String VLM_SYSTEM_PROMPT = "你是OCR识别助手。请原样输出图中全部文字内容，保持原有排版，不要总结、不要添加解释。";

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private MaaSClient maaSClient;

    @Override
    public OcrResult processOcr(AssessMaterial material)
    {
        if (material == null)
        {
            return OcrResult.fail("材料信息为空");
        }

        // 文件大小校验
        if (material.getFileSize() != null && material.getFileSize() > MAX_FILE_SIZE)
        {
            return OcrResult.fail("文件超过10MB限制，当前大小：" + material.getFileSize() + "字节");
        }

        String fileUrl = material.getFileUrl();
        if (StringUtils.isEmpty(fileUrl))
        {
            return OcrResult.fail("文件URL为空");
        }

        // 解析本地文件路径
        File file = resolveLocalFile(fileUrl);
        if (file == null || !file.exists())
        {
            return OcrResult.fail("文件不存在：" + fileUrl);
        }

        String fileType = material.getFileType();
        if ("pdf".equalsIgnoreCase(fileType))
        {
            return processPdf(file);
        }
        else
        {
            return processImage(file);
        }
    }

    /**
     * PDF处理：先PDFBox抽文本，抽不出走VLM
     */
    private OcrResult processPdf(File file)
    {
        // Step 1: PDFBox文本抽取
        String text = extractPdfText(file);
        if (StringUtils.isNotEmpty(text) && text.trim().length() >= 10)
        {
            log.info("PDF文本抽取成功，字符数={}", text.length());
            return OcrResult.ok(text.trim());
        }

        log.info("PDF文本抽取为空(可能是扫描件)，尝试VLM识别");

        // Step 2: VLM识别(扫描件)
        if (!aiConfig.isEnabled() || StringUtils.isEmpty(aiConfig.getVlmModel()))
        {
            return OcrResult.fail("PDF为扫描件，需要VLM识别，但AI功能未启用或vlm-model未配置");
        }

        return processPdfWithVlm(file);
    }

    /**
     * PDF转PNG走VLM识别
     */
    private OcrResult processPdfWithVlm(File file)
    {
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = Math.min(doc.getNumberOfPages(), MAX_VLM_PAGES);

            StringBuilder allText = new StringBuilder();
            for (int i = 0; i < pageCount; i++)
            {
                BufferedImage image = renderer.renderImageWithDPI(i, 200);
                String base64 = imageToBase64(image, "png");
                if (base64 == null)
                {
                    continue;
                }

                String vlmResult = maaSClient.chatCompletionWithImage(
                        VLM_SYSTEM_PROMPT, "请识别这页的全部文字内容", base64, "image/png", aiConfig.getVlmModel());
                if (StringUtils.isNotEmpty(vlmResult))
                {
                    allText.append(vlmResult).append("\n");
                }
            }

            if (allText.length() > 0)
            {
                return OcrResult.ok(allText.toString().trim());
            }
            else
            {
                return OcrResult.fail("VLM识别PDF返回空内容");
            }
        }
        catch (Exception e)
        {
            log.error("PDF转VLM识别失败, file={}", file.getName(), e);
            return OcrResult.fail("PDF转VLM识别失败：" + e.getMessage());
        }
    }

    /**
     * 图片处理：base64编码走VLM
     */
    private OcrResult processImage(File file)
    {
        if (!aiConfig.isEnabled() || StringUtils.isEmpty(aiConfig.getVlmModel()))
        {
            return OcrResult.fail("图片OCR需要VLM识别，但AI功能未启用或vlm-model未配置");
        }

        String fileName = file.getName().toLowerCase();
        String format = "jpeg";
        if (fileName.endsWith(".png"))
        {
            format = "png";
        }
        else if (fileName.endsWith(".webp"))
        {
            format = "webp";
        }

        String base64 = fileToBase64(file);
        if (base64 == null)
        {
            return OcrResult.fail("图片文件读取失败");
        }

        String mimeType = "image/" + format;
        String result = maaSClient.chatCompletionWithImage(
                VLM_SYSTEM_PROMPT, "请识别图片中的全部文字内容", base64, mimeType, aiConfig.getVlmModel());

        if (StringUtils.isNotEmpty(result))
        {
            return OcrResult.ok(result);
        }
        else
        {
            return OcrResult.fail("VLM识别图片返回空内容");
        }
    }

    /**
     * PDFBox抽取PDF文本
     */
    private String extractPdfText(File file)
    {
        try (PDDocument doc = Loader.loadPDF(file))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
        catch (Exception e)
        {
            log.error("PDFBox文本抽取失败, file={}", file.getName(), e);
            return null;
        }
    }

    /**
     * 解析fileUrl为本地File对象
     * <p>
     * fileUrl格式为 /profile/upload/YYYY/MM/DD/xxx.pdf
     * 实际路径为 {ruoyi.profile}/upload/YYYY/MM/DD/xxx.pdf
     */
    private File resolveLocalFile(String fileUrl)
    {
        if (StringUtils.isEmpty(fileUrl))
        {
            return null;
        }

        // fileUrl以/profile开头，替换为实际profile路径
        String profile = RuoYiConfig.getProfile();
        String path;
        if (fileUrl.startsWith("/profile"))
        {
            path = fileUrl.replace("/profile", profile);
        }
        else if (fileUrl.startsWith(profile))
        {
            path = fileUrl;
        }
        else
        {
            // 兜底：当作相对路径拼接
            path = profile + "/upload/" + fileUrl;
        }

        return new File(path);
    }

    /**
     * BufferedImage转base64
     */
    private String imageToBase64(BufferedImage image, String format)
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            ImageIO.write(image, format, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
        catch (IOException e)
        {
            log.error("图片转base64失败", e);
            return null;
        }
    }

    /**
     * 文件转base64
     */
    private String fileToBase64(File file)
    {
        try (InputStream is = new FileInputStream(file))
        {
            byte[] bytes = is.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (IOException e)
        {
            log.error("文件转base64失败, file={}", file.getName(), e);
            return null;
        }
    }
}
