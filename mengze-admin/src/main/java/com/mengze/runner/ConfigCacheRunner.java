package com.mengze.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.entity.Config;
import com.mengze.domain.vo.ConfigCacheVo;
import com.mengze.mapper.ConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 应用启动时预热系统配置缓存
 */
@Component
@Order(1)
public class ConfigCacheRunner implements CommandLineRunner
{
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private ConfigMapper configMapper;

    @Override
    public void run(String... args)
    {
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Config::getConfigKey, Config::getConfigValue, Config::getConfigType)
                .eq(Config::getDelFlag, SystemConstants.STATUS_NORMAL)
                .orderByAsc(Config::getId);

        List<Config> configList = configMapper.selectList(queryWrapper);
        if (configList == null || configList.isEmpty()) {
            return;
        }

        configList.forEach(config -> {
            ConfigCacheVo cacheVo = new ConfigCacheVo(config.getConfigKey(), config.getConfigValue(), config.getConfigType());
            redisTemplate.opsForValue().set(RedisKeyConstants.CONFIG_KEY_PREFIX + config.getConfigKey(), cacheVo);
        });

        System.out.println("! ========>系统配置已缓存至 Redis !<======== !");
    }
}
