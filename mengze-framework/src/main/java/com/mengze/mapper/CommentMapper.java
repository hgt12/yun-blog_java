package com.mengze.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengze.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论表(Comment)表数据库访问层
 *
 * @author makejava
 * @since 2026-01-12 15:48:35
 */
//Alt + 回车 可以自动生成 CommentMapper.xml
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    //在mapper里直接写也可以，是另一种写法，实现都一样的
    //@Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id} AND status = 0")
    int increaseLikeCount(@Param("id") Long id);

}

