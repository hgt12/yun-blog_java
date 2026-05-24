package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.DictListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.SysDeptService;
import com.mengze.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 字典接口
 */
@RestController
@RequestMapping("/dicts")
@Api(tags = "字典", description = "字典接口")
public class DictController
{
    @Autowired
    private SysDictService sysDictService;

    @GetMapping
    @PreAuthorize("@ps.hasPermission('blog:dict:query')")
    @SystemLog(businessName = "分页查询字典列表")
    @ApiOperation(value = "字典列表", notes = "分页查询字典列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult getDictList(Integer pageNum, Integer pageSize, @Valid DictListDto dictListDto)
    {
        return sysDictService.getDictList(pageNum, pageSize, dictListDto);
    }
}
