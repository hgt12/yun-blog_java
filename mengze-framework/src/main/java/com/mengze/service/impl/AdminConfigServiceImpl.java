package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddConfigDto;
import com.mengze.domain.dto.ConfigListDto;
import com.mengze.domain.dto.UpdateConfigDto;
import com.mengze.domain.entity.Category;
import com.mengze.domain.vo.AdminConfigListVo;
import com.mengze.domain.vo.ConfigFormDetailVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.ConfigMapper;
import com.mengze.domain.entity.Config;
import com.mengze.service.AdminConfigService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 系统配置表(SysConfig)表服务实现类
 *
 * @author makejava
 * @since 2026-04-24 13:38:11
 */
@Service("sysConfigService")
public class AdminConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements AdminConfigService
{
    @Autowired
    private AdminConfigService adminConfigService;

    //获取配置列表
    @Override
    public ResponseResult<PageVo> getConfigList(Integer pageNum, Integer pageSize, ConfigListDto configListDto)
    {
        //创建查询条件
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();

        //根据关键字模糊查询
        if (StringUtils.hasText(configListDto.getKeywords())){
            queryWrapper.and(wrapper -> wrapper
                    .like(Config::getConfigName, configListDto.getKeywords())
                    .or()
                    .like(Config::getConfigKey, configListDto.getKeywords())
            );
        }

        //按id顺序排序
        queryWrapper.orderByAsc(Config::getId);

        //设置分页查询
        Page<Config> page = new Page<>(pageNum, pageSize);
        adminConfigService.page(page, queryWrapper);

        //封装成vo
        List<AdminConfigListVo> configListVos = BeanCopyUtils.copyBeanList(page.getRecords(), AdminConfigListVo.class);
        PageVo pageVo = new PageVo(configListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //获取配置表单数据
    @Override
    public ResponseResult<ConfigFormDetailVo> getConfigFormDetail(Long id)
    {
        //获取配置id
        Config config = adminConfigService.getById(id);
        if (config == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该配置不存在！");
        }

        //封装为Vo
        ConfigFormDetailVo configFormDetailVo = BeanCopyUtils.copyBean(config, ConfigFormDetailVo.class);

        //返回
        return ResponseResult.okResult(configFormDetailVo);
    }

    //添加配置
    @Override
    public ResponseResult addConfig(AddConfigDto addConfigDto)
    {
        //检查配置键是否已存在
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Config::getConfigKey, addConfigDto.getConfigKey());
        long count = adminConfigService.count(queryWrapper);

        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "配置键已存在，请勿重复添加！");
        }

        //添加配置
        Config config = BeanCopyUtils.copyBean(addConfigDto, Config.class);
        adminConfigService.save(config);

        return ResponseResult.okResult();
    }

    //更新配置
    @Override
    public ResponseResult updateConfig(Long id, UpdateConfigDto updateConfigDto)
    {
        //检查配置是否存在
        Config oldConfig = adminConfigService.getById(id);
        if (oldConfig == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该配置不存在！");
        }

        //检查配置键是否与其他配置键重复（排除自己）
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Config::getConfigKey, updateConfigDto.getConfigKey())
                .ne(Config::getId, id); // 排除当前配置
        long count = adminConfigService.count(queryWrapper);

        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "配置键已存在，请勿重复添加！");
        }

        //更新配置信息
        Config config = BeanCopyUtils.copyBean(updateConfigDto, Config.class);
        config.setId(id);
        adminConfigService.updateById(config);

        return ResponseResult.okResult();
    }
}

