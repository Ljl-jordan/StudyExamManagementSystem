package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.SysMenu;
import com.ljl.studyexammanagementsystem.repository.SysMenuRepository;
import com.ljl.studyexammanagementsystem.repository.SysUserRoleRepository;
import com.ljl.studyexammanagementsystem.entity.SysUserRole;
import com.ljl.studyexammanagementsystem.service.MenuService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {
//注入菜单表，用户角色表
    @Autowired
    private SysMenuRepository sysMenuRepository;

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    /**
     * 获取菜单树形结构
     * 逻辑：查全部未删除菜单 → 按parentId分组递归构建树
     */
    @Override
    public Result<List<SysMenu>> getTree() {
        //查询所有未被删除的菜单
        List<SysMenu> allMenus = sysMenuRepository.findAllActive();
        List<SysMenu> tree = buildTree(allMenus, 0L);//调用递归方法构建菜单树
        return Result.success(tree);
    }

    /**
     * 获取当前用户的菜单权限 + 按钮权限
     * 逻辑：userId → 查sys_user_role获取roleIds → 查sys_role_menu获取menuIds → 查菜单详情 → 收集button_perms
     */
    @Override
    public Result<Map<String, Object>> getUserPermissions(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("menus", Collections.emptyList());
            data.put("buttons", Collections.emptyList());
            return Result.success(data);
        }

        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysMenu> menus = sysMenuRepository.findByRoleIds(roleIds);
        List<SysMenu> menuTree = buildTree(menus, 0L);

        Set<String> buttons = new HashSet<>();
        List<String> permsList = sysMenuRepository.findButtonPermsByRoleIds(roleIds);
        for (String perms : permsList) {
            if (perms != null && !perms.isEmpty()) {
                buttons.addAll(Arrays.asList(perms.split(",")));
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("menus", menuTree);
        data.put("buttons", new ArrayList<>(buttons));
        return Result.success(data);
    }

    /**
     * 递归构建菜单树
     */
    private List<SysMenu> buildTree(List<SysMenu> allMenus, Long parentId) {
        return allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .peek(m -> {
                    List<SysMenu> children = buildTree(allMenus, m.getId());
                    m.setChildren(children.isEmpty() ? null : children);
                })
                .collect(Collectors.toList());
    }
}
