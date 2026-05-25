package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddDictDto;
import com.mengze.domain.dto.DictListDto;
import com.mengze.domain.dto.UpdateDictDto;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysDictMapper;
import com.mengze.domain.entity.SysDict;
import com.mengze.domain.entity.SysDictItem;
import com.mengze.service.SysDictItemService;
import com.mengze.service.SysDictService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private SysDictItemService sysDictItemService;

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

    //获取字典表单数据
    @Override
    public ResponseResult<DictFormDetailVo> getDictFormDetail(Long id)
    {
        //根据字典id 查询字典
        SysDict dict = sysDictService.getById(id);
        if (dict == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该字典不存在！");
        }

        //转换成Vo
        DictFormDetailVo dictFormDetailVo = BeanCopyUtils.copyBean(dict, DictFormDetailVo.class);

        //返回
        return ResponseResult.okResult(dictFormDetailVo);
    }

    //添加字典
    @Override
    public ResponseResult addDict(AddDictDto addDictDto)
    {
        // 检查字典名称和字典编码是否已存在
        LambdaQueryWrapper<SysDict> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(SysDict::getName, addDictDto.getName());
        long nameCount = sysDictService.count(nameWrapper);
        if (nameCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "字典名称已存在，请勿重复添加！");
        }

        LambdaQueryWrapper<SysDict> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(SysDict::getDictCode, addDictDto.getDictCode());
        long codeCount = sysDictService.count(codeWrapper);
        if (codeCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "字典编码已存在，请勿重复添加！");
        }

        //添加字典
        SysDict dict = BeanCopyUtils.copyBean(addDictDto, SysDict.class);
        sysDictService.save(dict);

        return ResponseResult.okResult();
    }

    //更新字典
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateDict(Long id, UpdateDictDto updateDictDto)
    {
        //检查字典是否存在
        SysDict oldDict = sysDictService.getById(id);
        if (oldDict == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该字典不存在！");
        }

        //校验字典名称是否重复(排除自己)
        LambdaQueryWrapper<SysDict> nameQueryWrapper = new LambdaQueryWrapper<>();
        nameQueryWrapper.eq(SysDict::getName, updateDictDto.getName())
                        .ne(SysDict::getId, id);
        long nameCount = sysDictService.count(nameQueryWrapper);
        if (nameCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "字典名称已存在，请修改后重试！");
        }

        //校验字典编码是否重复(排除自己)
        LambdaQueryWrapper<SysDict> codeQueryWrapper = new LambdaQueryWrapper<>();
        codeQueryWrapper.eq(SysDict::getDictCode, updateDictDto.getDictCode())
                        .ne(SysDict::getId, id);
        long codeCount = sysDictService.count(codeQueryWrapper);
        if (codeCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "字典编码已存在，请修改后重试！");
        }

        //更新字典
        SysDict dict = BeanCopyUtils.copyBean(updateDictDto, SysDict.class);
        dict.setId(id);

        //修改字典信息（dict_code）时自动把原来属于这个字典的所有字典项里的数据也一起更新
        boolean result = sysDictService.updateById(dict);
        if (result) {
            //查询当前字典编码下的所有字典项 然后只取字典项的id 方便后续批量更新
            List<SysDictItem> dictItemList = sysDictItemService.list(
                    new LambdaQueryWrapper<SysDictItem>()
                            .eq(SysDictItem::getDictCode, oldDict.getDictCode())
                            .select(SysDictItem::getId)
            );

            //如果存在该字典下的字典项 则把它们的 dictCode 一并改成新的字典编码
            if (!dictItemList.isEmpty()){
                List<Long> dictItemIds = dictItemList.stream().map(SysDictItem::getId).toList();
                SysDictItem dictItem = new SysDictItem();
                dictItem.setDictCode(dict.getDictCode());
                sysDictItemService.update(dictItem,
                        new LambdaQueryWrapper<SysDictItem>()
                                .in(SysDictItem::getId, dictItemIds)
                );
            }
        }

        return ResponseResult.okResult();
    }

    //删除字典
    @Override
    public ResponseResult deleteDict(Long[] ids)
    {
        //检查是否有选择到要删除的字典
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的字典");
        }

        //获取字典id列表
        List<Long> idList = List.of(ids);

        //检查选择的字典是否存在
        List<SysDict> dictsToDelete = sysDictService.listByIds(idList);
        if (dictsToDelete.size() != ids.length){
            //如果有不存在的字典，就找出对应的字典id
            Set<Long> existIds = dictsToDelete.stream()
                    .map(SysDict::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下字典 id 不存在：" + notExistIds);
        }

        //删除字典
        sysDictService.removeByIds(idList);

        return ResponseResult.okResult();
    }
}

