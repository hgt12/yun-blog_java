package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.DeptOptionVo;
import com.mengze.mapper.SysDeptMapper;
import com.mengze.domain.entity.SysDept;
import com.mengze.service.SysDeptService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门表(SysDept)表服务实现类
 *
 * @author makejava
 * @since 2026-05-15 13:06:17
 */
@Service("sysDeptService")
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService
{



    //获取部门下拉选项（树结构）
    @Override
    public ResponseResult<List<DeptOptionVo>> getDeptOptions()
    {
        //构建查询条件
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();

        //查询状态正常的部门,按排序降序
        queryWrapper.eq(SysDept::getStatus, SystemConstants.STATUS_NORMAL)
                    .orderByAsc(SysDept::getSort);

        //查询所有符合条件的部门
        List<SysDept> sysDepts = list(queryWrapper);

        //封装成vo
        List<DeptOptionVo> deptOptionVos = BeanCopyUtils.copyBeanList(sysDepts, DeptOptionVo.class);

        //构建菜单树
        List<DeptOptionVo> deptOptionTree = builderDeptOptionsTree(deptOptionVos, 0L);

        return ResponseResult.okResult(deptOptionTree);
    }



    //创建DeptOptions的菜单树
    private List<DeptOptionVo> builderDeptOptionsTree(List<DeptOptionVo> deptOptionVos, Long parentId)
    {
        return deptOptionVos.stream()
                .filter(deptOptionVo -> parentId.equals(deptOptionVo.getParentId()))
                .map(menu -> menu.setChildren(getChildrenByDeptOptions(menu, deptOptionVos)))
                .collect(Collectors.toList());
    }

    //获取传入参数的 子部门集合
    private List<DeptOptionVo> getChildrenByDeptOptions(DeptOptionVo deptOptionVo, List<DeptOptionVo> deptOptionVos)
    {
        return deptOptionVos.stream()
                .filter(child -> deptOptionVo.getId().equals(child.getParentId()))
                .map(child -> child.setChildren(getChildrenByDeptOptions(child, deptOptionVos)))
                .collect(Collectors.toList());
    }
}

