package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.SysMenu;
import com.ljl.studyexammanagementsystem.service.MenuService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@Api(tags = "菜单管理接口")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取菜单树形结构
     */
    @GetMapping("/tree")
    @ApiOperation(value = "获取菜单树形")
    public Result<List<SysMenu>> tree() {
        return menuService.getTree();
    }

    /**
     * 获取当前用户的菜单权限 + 按钮权限标识
     */
    @GetMapping("/userPerms")
    @ApiOperation(value = "获取当前用户权限")
    public Result<Map<String, Object>> userPerms(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return menuService.getUserPermissions(userId);
    }
}
