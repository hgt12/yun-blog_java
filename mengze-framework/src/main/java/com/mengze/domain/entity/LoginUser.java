package com.mengze.domain.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.mengze.constants.SystemConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser implements UserDetails
{
    private SysUser sysUser;

    /**
     * 当前用户拥有的权限标识，例如 sys:user:list
     */
    private List<String> permissions;

    public LoginUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    @JSONField(serialize = false)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return sysUser == null || SystemConstants.USER_STATUS_NORMAL.equals(sysUser.getStatus());
    }
}
