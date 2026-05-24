package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.DictListDto;
import com.mengze.domain.vo.DictListVo;
import com.mengze.domain.vo.DictOptionVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.SysDictMapper;
import com.mengze.domain.entity.SysDict;
import com.mengze.service.SysDictService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 数据字典类型表(SysDict)表服务实现类
 *
 * @author makejava
 * @since 2026-05-24 13:59:24
 */
@Service("sysDictService")
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService
{
    @Autowired
    private SysDictService sysDictService;

    //获取字典列表
    @Override
    public ResponseResult getDictList(Integer pageNum, Integer pageSize, DictListDto dictListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();

        //根据字典名称或字典编码模糊查询
        if (StringUtils.hasText(dictListDto.getKeywords())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysDict::getName, dictListDto.getKeywords())
                    .or()
                    .like(SysDict::getDictCode, dictListDto.getKeywords())
            );
        }

        //根据状态筛选
        if (StringUtils.hasText(dictListDto.getStatus())) {
            queryWrapper.eq(SysDict::getStatus, dictListDto.getStatus());
        }

        //按创建时间降序排序
        queryWrapper.orderByDesc(SysDict::getCreateTime);

        //分页查询
        Page<SysDict> page = new Page<>(pageNum, pageSize);
        sysDictService.page(page, queryWrapper);

        //查询字典表
        List<SysDict> dictList = page.getRecords();

        //封装查询结果为Vo返回
        List<DictListVo> dictListVos = BeanCopyUtils.copyBeanList(dictList, DictListVo.class);

        PageVo pageVo = new PageVo(dictListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //获取字典下拉选项
    @Override
    public ResponseResult getDictOptions()
    {
        //构建查询条件
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();

        //查询状态正常的字典
        queryWrapper.eq(SysDict::getStatus, SystemConstants.STATUS_NORMAL)
                    .orderByDesc(SysDict::getId);

        //封装成一个集合
        List<SysDict> dictList = sysDictService.list(queryWrapper);

        //转换成Vo
        List<DictOptionVo> dictOptionVos = BeanCopyUtils.copyBeanList(dictList, DictOptionVo.class);

        return ResponseResult.okResult(dictOptionVos);
    }
}

