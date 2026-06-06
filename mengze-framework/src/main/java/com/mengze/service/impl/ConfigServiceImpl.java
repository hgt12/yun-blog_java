package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Config;
import com.mengze.domain.vo.ConfigVo;
import com.mengze.domain.vo.ConfigCacheVo;
import com.mengze.mapper.ConfigMapper;
import com.mengze.service.ConfigService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 系统配置表(Config)表服务实现类(前台)
 *
 * @author makejava
 * @since 2026-05-30 20:05:26
 */
@Service("configService")
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService
{

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    //获取前台配置
    @Override
    @SuppressWarnings("unchecked")
    public ResponseResult getBlogConfig()
    {
        //先从缓存读取前台配置
        Set<Object> keys = redisTemplate.keys(RedisKeyConstants.CONFIG_KEY_PREFIX + "*");
        List<ConfigVo> configVos = new ArrayList<>();

        if (keys != null && !keys.isEmpty()) {
            keys.forEach(key -> {
                Object value = redisTemplate.opsForValue().get(key);
                if (value instanceof ConfigCacheVo configCacheVo && SystemConstants.CONFIG_TYPE_IS_BLOG.equals(configCacheVo.getConfigType())) {
                    configVos.add(new ConfigVo(configCacheVo.getConfigKey(), configCacheVo.getConfigValue()));
                }
            });
        }

        //缓存为空时再查数据库
        if (configVos.isEmpty())
        {
            LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Config::getConfigKey, Config::getConfigValue)
                        .eq(Config::getConfigType, SystemConstants.CONFIG_TYPE_IS_BLOG)
                        .orderByAsc(Config::getId);

            List<Config> configList = list(queryWrapper);
            configVos.addAll(BeanCopyUtils.copyBeanList(configList, ConfigVo.class));
        }

        return ResponseResult.okResult(configVos);
    }
}
