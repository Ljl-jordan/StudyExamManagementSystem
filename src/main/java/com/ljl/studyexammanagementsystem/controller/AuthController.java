// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/controller/AuthController.java
package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.AuthService;
import com.ljl.studyexammanagementsystem.vo.LoginVO;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 接口1：登录（不需要Token，拦截器已放行）
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @ApiOperation(value = "登录", notes = "登录接口")
    public Result<Map<String, Object>> login(@RequestBody LoginVO loginVO, HttpServletRequest request) {
        if (loginVO.getLoginAccount() == null || loginVO.getPassword() == null) {
            return Result.paramError("账号和密码不能为空");
        }
        String ip = getClientIp(request);
        return authService.login(loginVO, ip);
    }

    /**
     * 接口2：退出登录（需要Token）
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @ApiOperation(value = "退出登录", notes = "退出登录接口")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.logout(userId);
    }

    /**
     * 接口3：刷新Token（需要有效Token）
     * POST /api/auth/refreshToken
     */
    @PostMapping("/refreshToken")
    @ApiOperation(value = "刷新Token", notes = "刷新Token接口")
    public Result<Map<String, Object>> refreshToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return Result.unauthorized("Token不能为空");
        }
        return authService.refreshToken(token);
    }

    /**
     * 接口4：重置密码（需要Token）
     * POST /api/auth/resetPassword
     * Body: {"oldPassword":"xxx", "newPassword":"xxx"}
     */
    @PostMapping("/resetPassword")
    @ApiOperation(value = "重置密码", notes = "重置密码接口")
    public Result<Void> resetPassword(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.paramError("原密码和新密码不能为空");
        }
        return authService.resetPassword(userId, oldPassword, newPassword);
    }

    // ... existing code ...

    /**
     * 接口5：获取当前登录用户信息（需要Token）
     * GET /api/auth/currentUser
     */
    @GetMapping("/currentUser")
    @ApiOperation(value = "获取当前登录用户信息", notes = "获取当前登录用户信息接口")
    public Result<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.getCurrentUser(userId);
    }

    /**
     * 接口6：手动解锁账号（需要Token，管理员操作）
     * POST /api/auth/unlock
     * Body: {"loginAccount":"xxx"}
     */
    @PostMapping("/unlock")
    @ApiOperation(value = "手动解锁账号", notes = "手动解锁账号接口")
    public Result<Void> unlockAccount(@RequestBody Map<String, String> params) {
        String loginAccount = params.get("loginAccount");
        if (loginAccount == null || loginAccount.trim().isEmpty()) {
            return Result.paramError("登录账号不能为空");
        }
        return authService.unlockAccount(loginAccount);
    }

    // ... existing code ...

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
