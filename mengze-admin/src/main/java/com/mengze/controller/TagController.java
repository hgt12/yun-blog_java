package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.TagDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.TagVo;
import com.mengze.service.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//标签相关接口
@RestController
@RequestMapping("/tags")
@Api(tags = "标签", description = "文章标签相关接口")
public class TagController
{
    @Autowired
    private TagService tagService;

    @GetMapping
    @SystemLog(businessName = "分页查询标签列表")
    @ApiOperation(value = "分页查询标签列表", notes = "获取标签列表", response = TagVo.class, responseContainer = "List")
    public ResponseResult<PageVo> tagList(Integer pageNum, Integer pageSize, TagDto tagDto)
    {
        return tagService.tagList(pageNum, pageSize, tagDto);
    }
}
