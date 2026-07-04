package com.cloudmall.service.impl;

import com.cloudmall.common.exception.BusinessException;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RBAC权限服务 — 角色-权限-用户管理
 */
@Service
public class RbacService {

    @Resource private RoleMapper roleMapper;
    @Resource private PermissionMapper permMapper;
    @Resource private RolePermissionMapper rpMapper;
    @Resource private UserRoleMapper urMapper;
    @Resource private AdminUserMapper adminUserMapper;
    @Resource private UserMapper userMapper;

    // ==================== 角色管理 ====================
    public List<Role> allRoles() { return roleMapper.selectList(null); }

    public void saveRole(Role role) {
        if (role.getId() == null) roleMapper.insert(role);
        else roleMapper.updateById(role);
    }

    public void deleteRole(Long roleId) {
        rpMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));
        roleMapper.deleteById(roleId);
    }

    // ==================== 权限管理 ====================
    public List<Permission> allPermissions() {
        return permMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Permission>()
                        .orderByAsc(Permission::getSort));
    }

    /** 获取角色的权限编码列表 */
    public Set<String> getPermCodesByRoleId(Long roleId) {
        List<RolePermission> rps = rpMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId));
        if (rps.isEmpty()) return Collections.emptySet();
        return permMapper.selectBatchIds(
                rps.stream().map(RolePermission::getPermId).collect(Collectors.toList()))
                .stream().map(Permission::getPermCode).collect(Collectors.toSet());
    }

    /** 获取用户的所有权限 */
    public Set<String> getUserPermCodes(Long userId) {
        List<UserRole> urs = urMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId));
        Set<String> codes = new HashSet<>();
        for (UserRole ur : urs) {
            codes.addAll(getPermCodesByRoleId(ur.getRoleId()));
        }
        return codes;
    }

    // ==================== 角色-权限关联 ====================
    public void assignPerms(Long roleId, List<Long> permIds) {
        rpMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));
        for (Long permId : permIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermId(permId);
            rpMapper.insert(rp);
        }
    }

    // ==================== 用户-角色关联 ====================
    public void assignRoles(Long userId, List<Long> roleIds) {
        urMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            urMapper.insert(ur);
        }
    }

    public List<Long> getUserRoleIds(Long userId) {
        return urMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }
}
