package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddDictItemDto;
import com.mengze.domain.dto.DictItemListDto;
import com.mengze.domain.dto.UpdateDictItemDto;
import com.mengze.domain.entity.SysDict;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysDictItemMapper;
import com.mengze.domain.entity.SysDictItem;
import com.mengze.service.SysDictItemService;
import com.mengze.service.SysDictService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.util.List;

/**
 * 数据字典项表(SysDictItem)表服务实现类
 *
 * @author makejava
 * @since 2026-05-24 13:59:52
 */
@Service("sysDictItemService")
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService
{
    @Autowired
    private SysDictItemService sysDictItemService;

    @Autowired
    private SysDictService sysDictService;

    //获取字典项列表
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult getDictItemList(Integer pageNum, Integer pageSize, String dictCode, DictItemListDto dictItemListDto)
    {
        //根据字典编码查询字典项所属字典是否存在
        LambdaQueryWrapper<SysDict> dictQueryWrapper = new LambdaQueryWrapper<>();
        dictQueryWrapper.eq(SysDict::getDictCode, dictCode);
        SysDict dict = sysDictService.getOne(dictQueryWrapper);
        if (dict == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该字典不存在！");
        }

        //构建查询条件
        LambdaQueryWrapper<SysDictItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictItem::getDictCode, dictCode);

        //根据字典项标签或字典项值模糊查询
        if (StringUtils.hasText(dictItemListDto.getKeywords())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysDictItem::getLabel, dictItemListDto.getKeywords())
                    .or()
                    .like(SysDictItem::getValue, dictItemListDto.getKeywords())
            );
        }

        //根据状态筛选
        if (StringUtils.hasText(dictItemListDto.getStatus())) {
            queryWrapper.eq(SysDictItem::getStatus, dictItemListDto.getStatus());
        }

        //按排序和字典项值顺序排序
        queryWrapper.orderByAsc(SysDictItem::getValue)
                    .orderByAsc(SysDictItem::getSort);

        //分页查询
        Page<SysDictItem> page = new Page<>(pageNum, pageSize);
        sysDictItemService.page(page, queryWrapper);

        //查询字典项列表
        List<SysDictItem> dictItemList = page.getRecords();

        //封装查询结果为Vo返回
        List<DictItemListVo> dictItemListVos = BeanCopyUtils.copyBeanList(dictItemList, DictItemListVo.class);

        PageVo pageVo = new PageVo(dictItemListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //获取字典项下拉选项
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult getDictItemOptions(String dictCode)
    {
        //根据字典编码查询字典项所属字典是否存在
        LambdaQueryWrapper<SysDict> dictQueryWrapper = new LambdaQueryWrapper<>();
        dictQueryWrapper.eq(SysDict::getDictCode, dictCode);
        SysDict dict = sysDictService.getOne(dictQueryWrapper);
        if (dict == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该字典不存在！");
        }

        //构建查询条件
        LambdaQueryWrapper<SysDictItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictItem::getDictCode, dictCode);

        //查询状态正常的字典
        queryWrapper.eq(SysDictItem::getStatus, SystemConstants.STATUS_NORMAL)
                .orderByDesc(SysDictItem::getId);

        //按排序和字典项值顺序排序
        queryWrapper.orderByAsc(SysDictItem::getValue);

        //封装成一个集合
        List<SysDictItem> dictItemList = sysDictItemService.list(queryWrapper);

        //转换成Vo
        List<DictItemOptionVo> dictItemOptionVos = BeanCopyUtils.copyBeanList(dictItemList, DictItemOptionVo.class);

        return ResponseResult.okResult(dictItemOptionVos);
    }

    //获取字典项表单数据
    @Override
    public ResponseResult<DictItemFormDetailVo> getDictItemFormDetail(Long id, String dictCode)
    {
        //根据字典项id 查询字典项
        SysDictItem dictItem = sysDictItemService.getById(id);
        if (dictItem == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该字典项不存在！");
        }

        //转换成Vo
        DictItemFormDetailVo dictItemFormDetailVo = BeanCopyUtils.copyBean(dictItem, DictItemFormDetailVo.class);

        //返回
        return ResponseResult.okResult(dictItemFormDetailVo);
    }

    //添加字典项
    @Override
    public ResponseResult addDictItem(String dictCode, AddDictItemDto addDictItemDto)
    {
        //检查同一个字典编码（dict_code）下的字典项标签（label）和字典项值（value）是否重复
        LambdaQueryWrapper<SysDictItem> labelWrapper = new LambdaQueryWrapper<>();
        labelWrapper.eq(SysDictItem::getDictCode, dictCode)
                    .eq(SysDictItem::getLabel, addDictItemDto.getLabel());
        long labelCount = sysDictItemService.count(labelWrapper);
        if (labelCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "当前字典下字典项标签已存在，请勿重复添加！");
        }

        LambdaQueryWrapper<SysDictItem> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.eq(SysDictItem::getDictCode, dictCode)
                    .eq(SysDictItem::getValue, addDictItemDto.getValue());
        long valueCount = sysDictItemService.count(valueWrapper);
        if (valueCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "当前字典下字典项值已存在，请勿重复添加！");
        }

        //添加字典项
        SysDictItem dictItem = BeanCopyUtils.copyBean(addDictItemDto, SysDictItem.class);
        dictItem.setDictCode(dictCode);
        sysDictItemService.save(dictItem);

        return ResponseResult.okResult();
    }

    //更新字典项
    @Override
    public ResponseResult updateDictItem(Long id, String dictCode, @Valid UpdateDictItemDto updateDictItemDto)
    {
        //检查字典项是否存在
        SysDictItem oldDictItem = sysDictItemService.getById(id);
        if (oldDictItem == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该字典项不存在！");
        }

        //检查同一个字典编码（dict_code）下的字典项标签（label）和字典项值（value）是否重复(排除自己)
        LambdaQueryWrapper<SysDictItem> labelWrapper = new LambdaQueryWrapper<>();
        labelWrapper.eq(SysDictItem::getDictCode, dictCode)
                    .eq(SysDictItem::getLabel, updateDictItemDto.getLabel())
                    .ne(SysDictItem::getId, id);
        long labelCount = sysDictItemService.count(labelWrapper);
        if (labelCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "当前字典下字典项标签已存在，请修改后重试！");
        }

        LambdaQueryWrapper<SysDictItem> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.eq(SysDictItem::getDictCode, dictCode)
                    .eq(SysDictItem::getValue, updateDictItemDto.getValue())
                    .ne(SysDictItem::getId, id);
        long valueCount = sysDictItemService.count(valueWrapper);
        if (valueCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "当前字典下字典项值已存在，请修改后重试！");
        }

        //更新字典项
        SysDictItem dictItem = BeanCopyUtils.copyBean(updateDictItemDto, SysDictItem.class);
        dictItem.setId(id);
        sysDictItemService.updateById(dictItem);

        return ResponseResult.okResult();
    }

}

