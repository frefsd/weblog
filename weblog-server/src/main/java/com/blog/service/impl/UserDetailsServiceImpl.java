package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.User;
import com.blog.entity.UserRole;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // Lombok 会自动生成包含 final 字段的构造函数
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1.获取用户信息 0：未删除 1：已删除
        User user = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("username", username)
                        .eq("is_deleted", 0)
        );

        //判断用户是否存在
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        //2.查询用户的角色
        List<UserRole> userRoleList = userRoleMapper.selectList(
                new QueryWrapper<UserRole>()
                        .eq("username", username));

        //3.将字符串角色转换为 Spring Security 的权限对象
        List<SimpleGrantedAuthority> authorities = userRoleList.stream()
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole()))
                .collect(Collectors.toList());

        //4.返回结果
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
