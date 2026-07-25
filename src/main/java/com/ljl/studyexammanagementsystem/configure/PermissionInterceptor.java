package com.ljl.studyexammanagementsystem.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljl.studyexammanagementsystem.annotation.RequirePermission;
import com.ljl.studyexammanagementsystem.entity.SysMenu;
import com.ljl.studyexammanagementsystem.entity.SysRoleMenu;
import com.ljl.studyexammanagementsystem.entity.SysUserRole;
import com.ljl.studyexammanagementsystem.repository.SysMenuRepository;
import com.ljl.studyexammanagementsystem.repository.SysRoleMenuRepository;
import com.ljl.studyexammanagementsystem.repository.SysRoleRepository;
import com.ljl.studyexammanagementsystem.repository.SysUserRoleRepository;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;

    @Autowired
    private SysMenuRepository sysMenuRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysRoleRepository sysRoleRepository;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (annotation == null) {
            return true;
        }

        String requiredPerm = annotation.value();
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            writeResponse(response, "未登录");
            return false;
        }
        if(isSuperAdmin(userId)){
            return true;
        }

        Set<String> userPerms = getUserPermissions(userId);
        if (!userPerms.contains(requiredPerm)) {
            writeResponse(response, "无操作权限");
            return false;
        }

        return true;
    }

    private boolean isSuperAdmin(Long userId) {
        List<Byte> scopes = sysRoleRepository.findDataScopesByUserId(userId);
        return scopes != null && scopes.contains((byte) 0);
    }
    private Set<String> getUserPermissions(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        List<SysRoleMenu> roleMenus = sysRoleMenuRepository.findByRoleIdIn(roleIds);
        if (roleMenus.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());

        List<SysMenu> menus = sysMenuRepository.findAllById(menuIds);

        Set<String> perms = new HashSet<>();
        for (SysMenu menu : menus) {
            if (menu.getButtonPerms() != null && !menu.getButtonPerms().isEmpty()) {
                String[] permArray = menu.getButtonPerms().split(",");
                for (String p : permArray) {
                    perms.add(p.trim());
                }
            }
        }
        return perms;
    }

    private void writeResponse(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(Result.forbidden(msg)));
    }
}
