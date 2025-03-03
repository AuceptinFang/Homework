package com.example.homeworksystem.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * PDF工具类，用于处理图片转PDF等操作
 */
public class PdfUtil {

    /**
     * 将多个图片文件合并为一个PDF文件
     *
     * @param imageFiles 图片文件列表
     * @param outputPath 输出PDF文件路径
     * @throws IOException 如果文件操作失败
     * @throws DocumentException 如果PDF文档操作失败
     */
    public static void imagesToPdf(List<MultipartFile> imageFiles, Path outputPath) throws IOException, DocumentException {
        // 创建文档
        Document document = new Document(PageSize.A4);
        
        try {
            // 创建PdfWriter
            PdfWriter.getInstance(document, new FileOutputStream(outputPath.toFile()));
            
            // 打开文档
            document.open();
            
            // 遍历所有图片文件
            for (MultipartFile imageFile : imageFiles) {
                // 检查文件是否为图片
                String contentType = imageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    System.out.println("跳过非图片文件: " + imageFile.getOriginalFilename());
                    continue;
                }
                
                // 将MultipartFile转换为iText的Image对象
                Image image = Image.getInstance(imageFile.getBytes());
                
                // 调整图片大小以适应页面
                float width = image.getWidth();
                float height = image.getHeight();
                float pageWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
                float pageHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
                
                // 计算缩放比例
                float widthRatio = pageWidth / width;
                float heightRatio = pageHeight / height;
                float ratio = Math.min(widthRatio, heightRatio);
                
                // 设置图片大小
                image.scaleAbsolute(width * ratio, height * ratio);
                
                // 设置图片居中
                image.setAlignment(Image.ALIGN_CENTER);
                
                // 添加图片到文档
                document.add(image);
                
                // 每张图片后添加新页面（最后一张图片除外）
                if (imageFiles.indexOf(imageFile) < imageFiles.size() - 1) {
                    document.newPage();
                }
            }
        } finally {
            // 关闭文档
            if (document.isOpen()) {
                document.close();
            }
        }
    }
    
    /**
     * 检查文件是否为图片
     *
     * @param file 要检查的文件
     * @return 如果是图片则返回true，否则返回false
     */
    public static boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
} 