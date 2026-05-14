package com.mengze.controller;

import com.alibaba.fastjson.JSON;
import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.CommentListDto;
import com.mengze.domain.dto.ReplyCommentDto;
import com.mengze.domain.vo.CommentExportVo;
import com.mengze.domain.vo.CommentStatisticsVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.service.AdminCommentService;
import com.mengze.utils.ExcelExportUtil;
import com.mengze.utils.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 评论管理接口
 */
@RestController
@RequestMapping("/comments")
@Api(tags = "评论管理", description = "评论管理相关接口")
public class CommentController
{
    @Autowired
    private AdminCommentService adminCommentService;

    @GetMapping
    @SystemLog(businessName = "分页查询评论列表")
    @ApiOperation(value = "评论列表", notes = "分页查询评论列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult getCommentList(Integer pageNum, Integer pageSize, @Valid CommentListDto commentListDto)
    {
        return adminCommentService.getCommentList(pageNum, pageSize, commentListDto);
    }

    @PostMapping
    @SystemLog(businessName = "回复评论")
    @ApiOperation(value = "回复评论", notes = "管理员回复评论", response = String.class)
    public ResponseResult replyComment(@RequestBody @Valid ReplyCommentDto replyCommentDto)
    {
        return adminCommentService.replyComment(replyCommentDto);
    }

    @PutMapping("/{ids}/approve")
    @SystemLog(businessName = "审核评论（允许评论发布）")
    @ApiOperation(value = "审核评论接口（允许评论发布）", notes = "审核评论（允许评论发布）", response = String.class)
    @ApiImplicitParam(name = "ids", value = "评论ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult approveComment(@PathVariable Long[] ids)
    {
        return adminCommentService.approveComment(ids);
    }

    @PutMapping("/{ids}/reject")
    @SystemLog(businessName = "审核评论（拒绝评论发布）")
    @ApiOperation(value = "审核评论接口（拒绝评论发布）", notes = "审核评论（拒绝评论发布）", response = String.class)
    @ApiImplicitParam(name = "ids", value = "评论ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult rejectComment(@PathVariable Long[] ids)
    {
        return adminCommentService.rejectComment(ids);
    }

    @GetMapping("/statistics")
    @SystemLog(businessName = "获取统计信息")
    @ApiOperation(value = "获取评论统计信息", notes = "获取统计信息", response = CommentStatisticsVo.class)
    public ResponseResult statisticsComment()
    {
        return adminCommentService.statisticsComment();
    }

    @GetMapping("/export")
    @SystemLog(businessName = "导出评论数据")
    @ApiOperation(value = "导出评论数据", notes = "导出评论数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public void exportComment(Integer pageNum, Integer pageSize, @Valid @RequestBody CommentListDto commentListDto, HttpServletResponse response)
    {
        try {
            //调用服务层获取导出数据
            ResponseResult result = adminCommentService.exportComment(pageNum, pageSize, commentListDto);
            
            //检查结果
            if (result.getCode() != 200 || result.getData() == null) {
                throw new RuntimeException("获取导出数据失败");
            }
            
            //获取导出数据列表
            @SuppressWarnings("unchecked")
            List<CommentExportVo> exportData = (List<CommentExportVo>) result.getData();
            
            //检查数据是否为空
            if (exportData.isEmpty()) {
                throw new RuntimeException("没有可导出的数据");
            }
            
            //生成文件名+时间（格式：评论数据_20260503_143056）
            String fileName = "评论数据_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            
            //使用工具类导出Excel
            ExcelExportUtil.exportExcel(response, exportData, CommentExportVo.class, fileName, "评论数据");
            
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

    @DeleteMapping("/{ids}")
    @SystemLog(businessName = "删除评论(逻辑删除)")
    @ApiOperation(value = "删除评论接口（批量逻辑删除）", notes = "删除评论，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "评论id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteComment(@PathVariable Long[] ids)
    {
        return adminCommentService.deleteComment(ids);
    }
}
