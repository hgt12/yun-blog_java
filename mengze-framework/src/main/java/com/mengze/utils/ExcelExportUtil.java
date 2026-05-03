package com.mengze.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Excel导出工具类
 * 基于EasyExcel实现Excel文件导出功能
 * 
 * @author mengze
 */
public class ExcelExportUtil
{
    /**
     * 导出Excel文件（通用方法）
     * 
     * @param response HttpServletResponse对象
     * @param data 要导出的数据列表
     * @param clazz 数据对应的实体类Class（需要使用@ExcelProperty注解标注字段）
     * @param fileName 导出的文件名（不含扩展名）
     * @param sheetName Excel工作表名称
     * @param <T> 数据类型
     * @throws IOException IO异常
     */
    public static <T> void exportExcel(HttpServletResponse response, 
                                       List<T> data, 
                                       Class<T> clazz, 
                                       String fileName, 
                                       String sheetName) throws IOException
    {
        try {
            // 设置响应头
            setExcelResponseHeader(response, fileName);
            
            // 使用EasyExcel写入数据
            EasyExcel.write(response.getOutputStream(), clazz)
                    .autoCloseStream(Boolean.FALSE)  // 不自动关闭流，由Servlet容器管理
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())  // 自动列宽
                    .sheet(sheetName)
                    .doWrite(data);
                    
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            throw new IOException("Excel导出失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 导出Excel文件（简化版，使用默认sheet名称）
     * 
     * @param response HttpServletResponse对象
     * @param data 要导出的数据列表
     * @param clazz 数据对应的实体类Class
     * @param fileName 导出的文件名（不含扩展名）
     * @param <T> 数据类型
     * @throws IOException IO异常
     */
    public static <T> void exportExcel(HttpServletResponse response, 
                                       List<T> data, 
                                       Class<T> clazz, 
                                       String fileName) throws IOException
    {
        exportExcel(response, data, clazz, fileName, "Sheet1");
    }
    
    /**
     * 导出Excel文件（从RequestContextHolder获取response）
     * 
     * @param data 要导出的数据列表
     * @param clazz 数据对应的实体类Class
     * @param fileName 导出的文件名（不含扩展名）
     * @param sheetName Excel工作表名称
     * @param <T> 数据类型
     * @throws IOException IO异常
     */
    public static <T> void exportExcel(List<T> data, 
                                       Class<T> clazz, 
                                       String fileName, 
                                       String sheetName) throws IOException
    {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IOException("无法获取HttpServletResponse对象");
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            throw new IOException("HttpServletResponse对象为空");
        }
        exportExcel(response, data, clazz, fileName, sheetName);
    }
    
    /**
     * 设置Excel文件下载的响应头
     * 
     * @param response HttpServletResponse对象
     * @param fileName 文件名（不含扩展名）
     * @throws IOException IO异常
     */
    private static void setExcelResponseHeader(HttpServletResponse response, String fileName) throws IOException
    {
        try {
            // 设置内容类型为Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            
            // URL编码文件名，防止中文乱码
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            
            // 设置Content-Disposition响应头，告诉浏览器以附件形式下载
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
            
            // 设置其他响应头
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
        } catch (Exception e) {
            throw new IOException("设置响应头失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 导出多个Sheet的Excel文件
     * 
     * @param response HttpServletResponse对象
     * @param fileName 导出的文件名（不含扩展名）
     * @param sheets Sheet数据列表
     * @throws IOException IO异常
     */
    public static void exportMultiSheetExcel(HttpServletResponse response, 
                                             String fileName, 
                                             List<SheetData<?>> sheets) throws IOException
    {
        try {
            // 设置响应头
            setExcelResponseHeader(response, fileName);
            
            // 创建ExcelWriter
            com.alibaba.excel.ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .autoCloseStream(Boolean.FALSE)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .build();
            
            // 写入多个Sheet
            for (int i = 0; i < sheets.size(); i++) {
                SheetData<?> sheetData = sheets.get(i);
                com.alibaba.excel.write.metadata.WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetData.getSheetName())
                        .head(sheetData.getClazz())
                        .build();
                excelWriter.write(sheetData.getData(), writeSheet);
            }
            
            // 关闭ExcelWriter
            excelWriter.finish();
            
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            throw new IOException("Excel导出失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sheet数据封装类（用于多Sheet导出）
     * 
     * @param <T> 数据类型
     */
    public static class SheetData<T>
    {
        private String sheetName;
        private Class<T> clazz;
        private List<T> data;
        
        public SheetData(String sheetName, Class<T> clazz, List<T> data) {
            this.sheetName = sheetName;
            this.clazz = clazz;
            this.data = data;
        }
        
        public String getSheetName() {
            return sheetName;
        }
        
        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }
        
        public Class<T> getClazz() {
            return clazz;
        }
        
        public void setClazz(Class<T> clazz) {
            this.clazz = clazz;
        }
        
        public List<T> getData() {
            return data;
        }
        
        public void setData(List<T> data) {
            this.data = data;
        }
    }
}
