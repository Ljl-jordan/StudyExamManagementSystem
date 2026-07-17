package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.SysRole;
import com.ljl.studyexammanagementsystem.entity.SysRoleMenu;
import com.ljl.studyexammanagementsystem.entity.SysUserRole;
import com.ljl.studyexammanagementsystem.repository.SysRoleMenuRepository;
import com.ljl.studyexammanagementsystem.repository.SysRoleRepository;
import com.ljl.studyexammanagementsystem.repository.SysUserRoleRepository;
import com.ljl.studyexammanagementsystem.service.RoleService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;

    /**
     * 角色分页列表
     */
    @Override
    public Result<Map<String, Object>> page(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<SysRole> page = sysRoleRepository.findAllActive(pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /**
     * 新增角色
     */
    @Override
    @Transactional
    public Result<Void> add(Map<String, Object> params, Long userId) {
        String roleName = (String) params.get("roleName");
        if (roleName == null || roleName.trim().isEmpty()) {
            return Result.paramError("角色名称不能为空");
        }

        SysRole role = new SysRole();
        role.setRoleName(roleName.trim());
        role.setRoleType((byte) 1);
        Object dataScopeObj = params.get("dataScope");
        role.setDataScope(dataScopeObj != null ? Byte.valueOf(dataScopeObj.toString()) : (byte) 2);
        role.setCreateUser(userId);
        role.setCreateTime(new Date());
        role.setIsDelete((byte) 0);
        sysRoleRepository.save(role);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑角色（内置角色允许修改菜单与数据范围）
     */
    @Override
    @Transactional
    public Result<Void> update(Long id, Map<String, Object> params, Long userId) {
        SysRole role = sysRoleRepository.findById(id).orElse(null);
        if (role == null || role.getIsDelete() == 1) {
            return Result.paramError("角色不存在");
        }

        String roleName = (String) params.get("roleName");
        if (roleName != null && !roleName.trim().isEmpty()) {
            role.setRoleName(roleName.trim());
        }
        Object dataScopeObj = params.get("dataScope");
        if (dataScopeObj != null) {
            role.setDataScope(Byte.valueOf(dataScopeObj.toString()));
        }

        role.setUpdateUser(userId);
        role.setUpdateTime(new Date());
        sysRoleRepository.save(role);
        return Result.success("修改成功", null);
    }

    /**
     * 删除角色
     * 逻辑：内置角色(role_type=0)禁止删 → 已绑定用户禁止删 → 逻辑删除 + 清除角色菜单绑定
     */
    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        SysRole role = sysRoleRepository.findById(id).orElse(null);
        if (role == null || role.getIsDelete() == 1) {
            return Result.paramError("角色不存在");
        }
        if (role.getRoleType() == 0) {
            return Result.businessBlock("内置角色禁止删除");
        }
        long bindCount = sysUserRoleRepository.countByRoleId(id);
        if (bindCount > 0) {
            return Result.businessBlock("该角色已绑定用户，无法删除");
        }
        role.setIsDelete((byte) 1);
        role.setUpdateTime(new Date());
        sysRoleRepository.save(role);
        sysRoleMenuRepository.deleteByRoleId(id);
        return Result.success("删除成功", null);
    }

    /**
     * 复制角色
     * 逻辑：复制角色基本信息（名称加后缀_副本） → 复制sys_role_menu绑定关系
     */
    @Override
    @Transactional
    public Result<Void> copyRole(Long id, Long userId) {
        SysRole source = sysRoleRepository.findById(id).orElse(null);
        if (source == null || source.getIsDelete() == 1) {
            return Result.paramError("源角色不存在");
        }

        SysRole newRole = new SysRole();
        newRole.setRoleName(source.getRoleName() + "_副本");
        newRole.setRoleType((byte) 1);
        newRole.setDataScope(source.getDataScope());
        newRole.setCreateUser(userId);
        newRole.setCreateTime(new Date());
        newRole.setIsDelete((byte) 0);
        sysRoleRepository.save(newRole);

        List<SysRoleMenu> sourceMenus = sysRoleMenuRepository.findByRoleId(id);
        for (SysRoleMenu sm : sourceMenus) {
            SysRoleMenu newRm = new SysRoleMenu();
            newRm.setRoleId(newRole.getId());
            newRm.setMenuId(sm.getMenuId());
            newRm.setCreateUser(userId);
            newRm.setCreateTime(new Date());
            newRm.setIsDelete((byte) 0);
            sysRoleMenuRepository.save(newRm);
        }

        return Result.success("复制成功", null);
    }

    /**
     * 为角色批量分配菜单权限
     * 逻辑：先删除该角色所有旧绑定 → 再批量插入新绑定（覆盖式，事务控制）
     */
    @Override
    @Transactional
    public Result<Void> allotMenu(Long roleId, List<Long> menuIds, Long userId) {
        SysRole role = sysRoleRepository.findById(roleId).orElse(null);
        if (role == null || role.getIsDelete() == 1) {
            return Result.paramError("角色不存在");
        }

        sysRoleMenuRepository.deleteByRoleId(roleId);

        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                rm.setCreateUser(userId);
                rm.setCreateTime(new Date());
                rm.setIsDelete((byte) 0);
                sysRoleMenuRepository.save(rm);
            }
        }
        return Result.success("菜单分配成功", null);
    }

    /**
     * 为用户分配角色
     * 逻辑：清除该用户旧的角色绑定 → 批量绑定新角色
     */
    @Override
    @Transactional
    public Result<Void> allotUser(List<Long> userIds, Long roleId) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.paramError("请选择用户");
        }
        SysRole role = sysRoleRepository.findById(roleId).orElse(null);
        if (role == null || role.getIsDelete() == 1) {
            return Result.paramError("角色不存在");
        }

        for (Long userId : userIds) {
            sysUserRoleRepository.deleteByUserId(userId);
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            sysUserRoleRepository.save(ur);
        }
        return Result.success("分配成功", null);
    }
}
