package com.mengze.controller;

import com.alibaba.fastjson.JSON;
import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.*;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.service.AdminUserService;
import com.mengze.service.AuthService;
import com.mengze.utils.ExcelExportUtil;
import com.mengze.utils.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/users")
@Api(tags = "用户信息", description = "后台用户信息相关接口")
public class AdminUserController
{
    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AuthService authService;

    @GetMapping("/info")
    @SystemLog(businessName = "获取后台用户信息")
    @ApiOperation(value = "获取后台用户信息", notes = "获取当前登录后台用户的个人信息", response = SysUserInfoVo.class)
    public ResponseResult getInfo()
    {
        return adminUserService.getAdminInfo();
    }

    @GetMapping
    @PreAuthorize("@ps.hasPermission('sys:user:query')")
    @SystemLog(businessName = "分页查询用户列表")
    @ApiOperation(value = "获取用户列表", notes = "分页查询用户列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
    })
    public ResponseResult getUserList(Integer pageNum, Integer pageSize, @Valid UserListDto userListDto)
    {
        return adminUserService.getUserList(pageNum, pageSize, userListDto);
    }

    @GetMapping("/{userId}/form")
    @PreAuthorize("@ps.hasPermission('sys:user:update')")
    @SystemLog(businessName = "获取用户表单数据")
    @ApiOperation(value = "获取用户表单数据", notes = "获取用户表单数据，用于编辑用户详情", response = UserFormDetailVo.class)
    @ApiImplicitParam(name = "userId", value = "用户id", dataType = "long", paramType = "path")
    public ResponseResult<UserFormDetailVo> getUserFormDetail(@PathVariable Long userId)
    {
        return adminUserService.getUserFormDetail(userId);
    }

    @PostMapping
    @PreAuthorize("@ps.hasPermission('sys:user:create')")
    @SystemLog(businessName = "新增用户")
    @ApiOperation(value = "新增用户接口", notes = "新增用户", response = ResponseResult.class)
    public ResponseResult addUser(@Valid @RequestBody AddUserDto addUserDto)
    {
        return adminUserService.addUser(addUserDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('sys:user:update')")
    @SystemLog(businessName = "修改用户信息")
    @ApiOperation(value = "修改用户接口", notes = "修改用户信息", response = String.class)
    @ApiImplicitParam(name = "id", value = "用户ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDto updateUserDto)
    {
        return adminUserService.updateUser(id, updateUserDto);
    }

    @DeleteMapping("/{ids}")
    @PreAuthorize("@ps.hasPermission('blog:user:delete')")
    @SystemLog(businessName = "删除用户(逻辑删除)")
    @ApiOperation(value = "删除用户接口（批量逻辑删除）", notes = "删除用户，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "分类id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteUser(@PathVariable Long[] ids)
    {
        return adminUserService.deleteUser(ids);
    }

    @PutMapping("/{id}/password/reset")
    @PreAuthorize("@ps.hasPermission('sys:user:reset-password')")
    @SystemLog(businessName = "重置用户密码")
    @ApiOperation(value = "重置用户密码接口", notes = "重置用户密码", response = String.class)
    @ApiImplicitParam(name = "id", value = "用户ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateResetUserPassword(@PathVariable Long id, String password)
    {
        return adminUserService.updateResetUserPassword(id, password);
    }

    @GetMapping("/template")
    @SystemLog(businessName = "下载导入用户模板")
    @ApiOperation(value = "下载导入用户模板接口", notes = "下载导入用户的Excel模板")
    public void downloadImportUsersTemplate(HttpServletResponse response)
    {
        //生成初始数据
        try {
            List<UserImportTemplateVo> templateData = Collections.singletonList(
                    new UserImportTemplateVo(
                            "云梦泽",
                            "云梦泽",
                            "1",
                            "2962933152@qq.com",
                            "18329384753"
                    )
            );

            //下载的文件命名
            String fileName = "用户导入模板_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            ExcelExportUtil.exportExcel(response, templateData, UserImportTemplateVo.class, fileName, "用户导入模板");
        } catch (Exception e) {
            throw new RuntimeException("下载用户模板失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/export")
    @PreAuthorize("@ps.hasPermission('sys:user:export')")
    @SystemLog(businessName = "导出评论数据")
    @ApiOperation(value = "导出评论数据", notes = "导出评论数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public void exportUser(Integer pageNum, Integer pageSize, @Valid UserListDto userListDto, HttpServletResponse response)
    {
        try {
            //调用服务层获取导出数据
            ResponseResult result = adminUserService.exportUser(pageNum, pageSize, userListDto);

            //检查结果
            if (result.getCode() != 200 || result.getData() == null) {
                throw new RuntimeException("获取导出数据失败");
            }

            //获取导出数据列表
            @SuppressWarnings("unchecked")
            List<UserExportVo> exportData = (List<UserExportVo>) result.getData();

            //检查数据是否为空
            if (exportData.isEmpty()) {
                throw new RuntimeException("没有可导出的数据");
            }

            //生成文件名+时间（格式：评论数据_20260503_143056）
            String fileName = "用户数据_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            //使用工具类导出Excel
            ExcelExportUtil.exportExcel(response, exportData, UserExportVo.class, fileName, "用户数据");

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

    @PostMapping(value = "/import")
    @PreAuthorize("@ps.hasPermission('sys:user:import')")
    @SystemLog(businessName = "用户导入")
    @ApiOperation(value = "用户导入接口", notes = "用户导入", response = ResponseResult.class)
    @ApiImplicitParam(name = "file", value = "Excel导入文件", dataType = "file", paramType = "form")
    public ResponseResult importUsers(@RequestPart("file") MultipartFile file)
    {
        return adminUserService.importUsers(file);
    }

    @DeleteMapping("/{id}/dismiss/avatar")
    @PreAuthorize("@ps.hasPermission('sys:user:dismiss-avatar')")
    @SystemLog(businessName = "驳回用户头像(实际是逻辑删除该头像的源文件)")
    @ApiOperation(value = "驳回用户头像", notes = "驳回用户头像(实际是逻辑删除该头像的源文件)", response = String.class)
    @ApiImplicitParam(name = "id", value = "用户id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult dismissUserAvatar(@PathVariable Long id)
    {
        return adminUserService.dismissUserAvatar(id);
    }

    @GetMapping("/profile")
    @SystemLog(businessName = "获取个人中心信息接口")
    @ApiOperation(value = "获取个人中心信息", notes = "获取个用户人中心信息", response = UserProfileVo.class)
    public ResponseResult<UserProfileVo> getUserProfile()
    {
        return adminUserService.getUserProfile();
    }

    @PutMapping("/profile")
    @SystemLog(businessName = "修改个人中心信息接口")
    @ApiOperation(value = "修改个人中心信息接口", notes = "修改个人中心信息", response = String.class)
    public ResponseResult updateUserProfile(@Valid @RequestBody UpdateUserProfileDto updateUserProfileDto)
    {
        return adminUserService.updateUserProfile(updateUserProfileDto);
    }

    @PutMapping("/password")
    @SystemLog(businessName = "修改个人密码接口")
    @ApiOperation(value = "修改个人密码接口", notes = "修改个人密码", response = String.class)
    public ResponseResult updateIndividualPassword(@Valid @RequestBody UpdateIndividualPasswordDto updateIndividualPasswordDto)
    {
        return adminUserService.updateIndividualPassword(updateIndividualPasswordDto);
    }

    @PostMapping("/mobile/code")
    @SystemLog(businessName = "获取手机验证码接口")
    @ApiOperation(value = "获取手机验证码接口", notes = "用户解绑手机时获取手机验证码", response = String.class)
    public ResponseResult getMobileCode(String mobile)
    {
        return adminUserService.getMobileCode(mobile);
    }
}
