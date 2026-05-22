package com.mengze.filter;

import com.alibaba.fastjson.JSON;
import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Content-Type验证过滤器
 * 防止Content-Type伪造攻击
 * 
 * @author mengze
 */
@Slf4j
@Component
public class ContentTypeValidationFilter extends OncePerRequestFilter
{

    /**
     * 需要验证Content-Type的请求方法
     */
    private static final List<String> METHODS_TO_VALIDATE = Arrays.asList("POST", "PUT", "PATCH");

    /**
     * 允许的Content-Type列表
     */
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "application/json",
        "application/x-www-form-urlencoded",
        "multipart/form-data"
    );

    /**
     * 白名单路径（不需要验证Content-Type）
     */
    private static final List<String> WHITELIST_PATHS = Arrays.asList(
            "/users/email/code",       // 后台获取邮箱验证码接口
            "/users/mobile/code",       // 后台获取手机验证码接口
            "/users/*/password/reset",   // 后台重置密码接口
            "/auth/refreshToken",   // 后台刷新token接口
            "/comment/like",    // 前台点赞接口
            "/auth/logout",     // 后台退出登录
            "/upload",           // 后台文件上传接口
            "/swagger",          // Swagger文档
            "/v2/api-docs",      // Swagger API文档
            "/v3/api-docs"       // Swagger API文档
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        // 只验证POST、PUT、PATCH请求
        if (!METHODS_TO_VALIDATE.contains(method)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查是否在白名单中
        if (isWhitelistPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取Content-Type
        String contentType = request.getContentType();
        
        // Content-Type不能为空
        if (contentType == null || contentType.isEmpty()) {
            log.warn("请求缺少Content-Type header, path: {}, method: {}", path, method);
            sendErrorResponse(response, "请求必须包含Content-Type");
            return;
        }
        
        // 验证Content-Type是否合法（忽略charset等参数）
        String baseContentType = contentType.split(";")[0].trim().toLowerCase();
        boolean isValid = ALLOWED_CONTENT_TYPES.stream()
            .anyMatch(allowed -> baseContentType.startsWith(allowed));
        
        if (!isValid) {
            log.warn("不支持的Content-Type: {}, path: {}, method: {}", contentType, path, method);
            sendErrorResponse(response, "不支持的Content-Type: " + baseContentType);
            return;
        }
        
        // 验证通过，继续处理
        filterChain.doFilter(request, response);
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelistPath(String path) {
        return WHITELIST_PATHS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        
        ResponseResult<?> result = ResponseResult.errorResult(
            AppHttpCodeEnum.SYSTEM_ERROR.getCode(), 
            message
        );
        
        response.getWriter().write(JSON.toJSONString(result));
    }
}
