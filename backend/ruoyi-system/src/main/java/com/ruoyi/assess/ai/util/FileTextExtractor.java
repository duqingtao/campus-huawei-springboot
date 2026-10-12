package com.ruoyi.assess.ai.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件文本抽取工具类
 * <p>
 * 支持三种格式：
 * - PDF: PDFBox PDFTextStripper
 * - Word(.docx): POI XWPFWordExtractor
 * - TXT: 直接读取InputStream
 * <p>
 * 不支持的格式抛出IllegalArgumentException。
 *
 * @author ruoyi
 */
public class FileTextExtractor
{
    private static final Logger log = LoggerFactory.getLogger(FileTextExtractor.class);

    private FileTextExtractor()
    {
    }

    /**
     * 从文件中抽取纯文本
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名（用于判断格式）
     * @return 抽取出的文本内容
     * @throws Exception 抽取失败
     */
    public static String extractText(InputStream inputStream, String fileName) throws Exception
    {
        if (fileName == null)
        {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".pdf"))
        {
            return extractPdfText(inputStream);
        }
        else if (lowerName.endsWith(".docx"))
        {
            return extractWordText(inputStream);
        }
        else if (lowerName.endsWith(".txt"))
        {
            return extractTxtText(inputStream);
        }
        else
        {
            throw new IllegalArgumentException("仅支持PDF/Word/TXT格式");
        }
    }

    /**
     * PDFBox抽取PDF文本
     */
    private static String extractPdfText(InputStream inputStream) throws Exception
    {
        try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes()))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            log.info("PDF文本抽取成功，字符数={}", text != null ? text.length() : 0);
            return text;
        }
    }

    /**
     * POI抽取Word(.docx)文本
     */
    private static String extractWordText(InputStream inputStream) throws Exception
    {
        try (XWPFDocument doc = new XWPFDocument(inputStream))
        {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String text = extractor.getText();
            log.info("Word文本抽取成功，字符数={}", text != null ? text.length() : 0);
            return text;
        }
    }

    /**
     * 直接读取TXT文本
     */
    private static String extractTxtText(InputStream inputStream) throws Exception
    {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        log.info("TXT文本读取成功，字符数={}", text.length());
        return text;
    }
}
