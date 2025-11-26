package com.mengze.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

//Bean封装工具类
public class BeanCopyUtils
{
    private BeanCopyUtils(){
    }

    //因为有一些实体类只涉及一个字段而有些涉及多个字段，所以要分情况写
    //期望是在方法内部自己去创建Vo对象，通过传入参数来实现(通过Class字节码的反射方式来创建目标对象，然后再实现拷贝)
    //通过泛型来实现对象相同类型的返回

    //涉及单个字段的拷贝
    public static <V> V copyBean(Object source, Class<V> clazz)
    {
        //创建目标对对象
        V result;
        try {
            result = clazz.newInstance();
            //实现属性拷贝
            BeanUtils.copyProperties(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //返回结果
        return result;
    }

    //如果要实现多个字段的拷贝，例如列表的拷贝
    public static <O,V> List<V> copyBeanList(List<O> list, Class<V> clazz)
    {
        //通过遍历list集合来调用上面的copyBean方法来实现（不用for循环，使用stream流来实现）
        return list.stream()
                .map(o -> copyBean(o,clazz))
                .collect(Collectors.toList());
    }
}
