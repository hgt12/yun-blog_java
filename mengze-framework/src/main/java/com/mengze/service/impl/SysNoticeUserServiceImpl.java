package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.SysNoticeUserMapper;
import com.mengze.domain.entity.SysNoticeUser;
import com.mengze.service.SysNoticeUserService;
import org.springframework.stereotype.Service;

/**
 * 通知接收人与已读状态表(SysNoticeUser)表服务实现类
 *
 * @author makejava
 * @since 2026-05-25 20:31:17
 */
@Service("sysNoticeUserService")
public class SysNoticeUserServiceImpl extends ServiceImpl<SysNoticeUserMapper, SysNoticeUser> implements SysNoticeUserService {

}

