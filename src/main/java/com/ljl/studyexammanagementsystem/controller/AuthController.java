// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/controller/AuthController.java
package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.AuthService;
import com.ljl.studyexammanagementsystem.vo.LoginVO;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 接口1：登录（不需要Token，拦截器已放行）
     * POST /api/auth/login
     */
    @PostMapping("/login")
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
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.logout(userId);
    }

    /**
     * 接口3：刷新Token（需要有效Token）
     * POST /api/auth/refreshToken
     */
    @PostMapping("/refreshToken")
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
    public Result<Void> resetPassword(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.paramError("原密码和新密码不能为空");
        }
        return authService.resetPassword(userId, oldPassword, newPassword);
    }

    /**
     * 接口5：获取当前登录用户信息（需要Token）
     * GET /api/auth/currentUser
     */
    @GetMapping("/currentUser")
    public Result<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.getCurrentUser(userId);
    }

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
