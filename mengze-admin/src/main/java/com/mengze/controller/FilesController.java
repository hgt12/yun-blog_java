package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.FileListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.SysFileService;
import com.mengze.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

//后台用户上传文件相关接口
@RestController
@RequestMapping("/files")
@Api(tags = "上传", description = "文件上传相关接口")
public class FilesController
{
    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private UploadService uploadService;

    @PostMapping("/image")
    @SystemLog(businessName = "上传图片")
    @ApiOperation(value = "上传图片", notes = "上传头像、文章图片等图片文件", response = String.class)
    public ResponseResult uploadImage(MultipartFile image)
    {
        return uploadService.uploadImage(image);
    }

    @PostMapping
    @SystemLog(businessName = "上传文件（包括图片）")
    @ApiOperation(value = "上传文件", notes = "上传包括图片等文件", response = String.class)
    public ResponseResult uploadFile(MultipartFile file)
    {
        return sysFileService.uploadFile(file);
    }

    @GetMapping
    @SystemLog(businessName = "分页查询文件列表")
    @ApiOperation(value = "文件列表", notes = "分页查询文件列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
    })
    public ResponseResult<PageVo> getFileList(Integer pageNum, Integer pageSize, @Valid FileListDto fileListDto)
    {
        return sysFileService.getFileList(pageNum, pageSize, fileListDto);
    }

    @DeleteMapping("/{ids}")
    @SystemLog(businessName = "删除文件(逻辑删除)")
    @ApiOperation(value = "删除文件接口（批量逻辑删除）", notes = "删除文件，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "文章id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteFile(@PathVariable Long[] ids)
    {
        return sysFileService.deleteFile(ids);
    }
}
