package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddNoticeDto;
import com.mengze.domain.dto.NoticeListDto;
import com.mengze.domain.dto.UpdateNoticeDto;
import com.mengze.domain.vo.NoticeDetailVo;
import com.mengze.domain.vo.NoticeFormDetailVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.SysNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 通知相关接口
 */
@RestController
@RequestMapping("/notices")
@Api(tags = "通知", description = "通知相关接口")
public class NoticeController
{
    @Autowired
    private SysNoticeService sysNoticeService;

    @GetMapping
    @PreAuthorize("@ps.hasPermission('sys:notice:query')")
    @SystemLog(businessName = "获取通知列表接口")
    @ApiOperation(value = "获取通知列表接口", notes = "分页查询通知列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize, @Valid NoticeListDto noticeListDto)
    {
        return sysNoticeService.getNoticeList(pageNum, pageSize, noticeListDto);
    }

    @GetMapping("/{id}/form")
    @PreAuthorize("@ps.hasPermission('sys:notice:update')")
    @SystemLog(businessName = "获取通知表单数据（编辑公告时用上）")
    @ApiOperation(value = "获取通知表单数据（编辑公告时用上）", notes = "获取通知表单数据，用于编辑通知详情", response = NoticeFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "友链id", dataType = "long", paramType = "path")
    })
    public ResponseResult<NoticeFormDetailVo> getNoticeFormDetail(@PathVariable Long id)
    {
        return sysNoticeService.getNoticeFormDetail(id);
    }

    @PostMapping
    @PreAuthorize("@ps.hasPermission('sys:notice:create')")
    @SystemLog(businessName = "新增通知")
    @ApiOperation(value = "新增通知接口", notes = "新增通知", response = String.class)
    public ResponseResult addNotice(@Valid @RequestBody AddNoticeDto addNoticeDto)
    {
        return sysNoticeService.addNotice(addNoticeDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('sys:notice:update')")
    @SystemLog(businessName = "修改通知")
    @ApiOperation(value = "修改通知接口", notes = "修改通知", response = String.class)
    @ApiImplicitParam(name = "id", value = "通知ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateNotice(@PathVariable Long id, @Valid @RequestBody UpdateNoticeDto updateNoticeDto)
    {
        return sysNoticeService.updateNotice(id, updateNoticeDto);
    }

    @DeleteMapping("/{ids}")
    @PreAuthorize("@ps.hasPermission('sys:notice:delete')")
    @SystemLog(businessName = "删除通知(逻辑删除)")
    @ApiOperation(value = "删除通知接口（批量逻辑删除）", notes = "删除通知，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "通知id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteNotice(@PathVariable Long[] ids)
    {
        return sysNoticeService.deleteNotice(ids);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("@ps.hasPermission('sys:notice:publish')")
    @SystemLog(businessName = "发布通知")
    @ApiOperation(value = "发布通知接口", notes = "发布通知", response = String.class)
    @ApiImplicitParam(name = "id", value = "通知ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult publishNotice(@PathVariable Long id)
    {
        return sysNoticeService.publishNotice(id);
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize("@ps.hasPermission('sys:notice:revoke')")
    @SystemLog(businessName = "撤回通知")
    @ApiOperation(value = "撤回通知接口", notes = "撤回通知", response = String.class)
    @ApiImplicitParam(name = "id", value = "通知ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult revokeNotice(@PathVariable Long id)
    {
        return sysNoticeService.revokeNotice(id);
    }

    @GetMapping("/{id}/detail")
    @SystemLog(businessName = "获取通知详情信息（用户查看通知信息时用上）")
    @ApiOperation(value = "获取通知详情信息", notes = "获取通知详情信息，用户查看通知时用", response = NoticeFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "友链id", dataType = "long", paramType = "path")
    })
    public ResponseResult<NoticeDetailVo> getNoticeDetail(@PathVariable Long id)
    {
        return sysNoticeService.getNoticeDetail(id);
    }
}
