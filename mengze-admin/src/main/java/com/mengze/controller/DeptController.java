package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.DeptOptionVo;
import com.mengze.service.SysDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门接口
 */
@RestController
@RequestMapping("/depts")
@Api(tags = "部门", description = "部门接口")
public class DeptController
{
    @Autowired
    private SysDeptService sysDeptService;







    @GetMapping("/options")
    @SystemLog(businessName = "部门下拉选项")
    @ApiOperation(value = "部门下拉选项", notes = "获取部门下拉数据", response = DeptOptionVo.class, responseContainer = "List")
    public ResponseResult getDeptOptions()
    {
        return sysDeptService.getDeptOptions();
    }


}
