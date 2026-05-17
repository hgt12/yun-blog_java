package com.mengze.controller;

import com.alibaba.fastjson.JSON;
import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LogListDto;
import com.mengze.domain.vo.VisitLogExportVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.service.VisitLogService;
import com.mengze.utils.ExcelExportUtil;
import com.mengze.utils.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 日志管理接口
 */
@RestController
@RequestMapping("/logs")
@Api(tags = "日志管理", description = "日志管理相关接口")
public class LogController
{
    @Autowired
    private VisitLogService visitLogService;

    @GetMapping
    @SystemLog(businessName = "分页查询日志列表")
    @ApiOperation(value = "日志列表", notes = "分页查询日志列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult<PageVo> getLogList(Integer pageNum, Integer pageSize, @Valid LogListDto logListDto)
    {
        return visitLogService.getLogList(pageNum, pageSize, logListDto);
    }

    @GetMapping("/export")
    @SystemLog(businessName = "导出日志数据")
    @ApiOperation(value = "导出日志数据接口", notes = "导出日志数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public void exportLog(Integer pageNum, Integer pageSize, @Valid LogListDto logListDto, HttpServletResponse response)
    {
        try {
            //调用服务层获取导出数据
            ResponseResult result = visitLogService.exportLog(pageNum, pageSize, logListDto);

            //检查结果
            if (result.getCode() != 200 || result.getData() == null) {
                throw new RuntimeException("获取导出数据失败");
            }

            //获取导出数据列表
            @SuppressWarnings("unchecked")
            List<VisitLogExportVo> exportData = (List<VisitLogExportVo>) result.getData();

            //检查数据是否为空
            if (exportData.isEmpty()) {
                throw new RuntimeException("没有可导出的数据");
            }

            //生成文件名+时间（格式：日志数据_20260503_143056）
            String fileName = "日志数据_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            //使用工具类导出Excel
            ExcelExportUtil.exportExcel(response, exportData, VisitLogExportVo.class, fileName, "日志数据");

        } catch (Exception e) {
            //异常处理：只有在响应流未被写入时才能返回JSON错误
            e.printStackTrace();

            //检查响应是否已提交
            if (!response.isCommitted()) {
                try {
                    response.reset();
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");

                    ResponseResult result = ResponseResult.errorResult(
                            AppHttpCodeEnum.SYSTEM_ERROR.getCode(),
                            "导出失败: " + e.getMessage()
                    );
                    WebUtils.renderString(response, JSON.toJSONString(result));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
