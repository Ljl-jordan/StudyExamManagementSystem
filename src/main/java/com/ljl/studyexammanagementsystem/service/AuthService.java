// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/service/AuthService.java
package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.dao.repository.LoginLogRepository;
import com.ljl.studyexammanagementsystem.dao.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.entity.LoginLog;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.utils.JwtUtil;
import com.ljl.studyexammanagementsystem.utils.PasswordUtil;
import com.ljl.studyexammanagementsystem.vo.LoginVO;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final int MAX_LOGIN_FAIL = 5;
    private static final int LOCK_MINUTES = 30;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ==================== 1. 登录 ====================
    public Result<Map<String, Object>> login(LoginVO loginVO, String loginIp) {
        String loginAccount = loginVO.getLoginAccount();
        String inputPassword = loginVO.getPassword();

        // ① 查询用户（未删除的）
        SysUser user = sysUserRepository.findByLoginAccountAndIsDelete(loginAccount, (byte) 0).orElse(null);
        if (user == null) {
            saveLoginLog(null, loginAccount, loginIp, (byte) 0);
            return Result.unauthorized("账号或密码错误");
        }

        // ② 检查账号是否禁用（user_status=1为禁用）
        if (user.getUserStatus() == 1) {
            saveLoginLog(user.getId(), loginAccount, loginIp, (byte) 0);
            return Result.forbidden("账号已被禁用，请联系管理员");
        }

        // ③ 检查账号是否锁定（lock_time未过期说明仍在锁定期内）
        if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
            saveLoginLog(user.getId(), loginAccount, loginIp, (byte) 0);
            return Result.paramError("账号已锁定，请" + LOCK_MINUTES + "分钟后再试");
        }

        // ④ 校验密码
        if (!PasswordUtil.verify(inputPassword, user.getPassword())) {
            handleLoginFail(user);
            saveLoginLog(user.getId(), loginAccount, loginIp, (byte) 0);
            return Result.unauthorized("账号或密码错误");
        }

        // ⑤ 密码正确 → 清除锁定状态
        if (user.getLockTime() != null) {
            user.setLockTime(null);
            sysUserRepository.save(user);
        }

        // ⑥ 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getLoginAccount());

        // ⑦ 保存登录成功日志
        saveLoginLog(user.getId(), loginAccount, loginIp, (byte) 1);

        // ⑧ 返回Token和用户基本信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("loginAccount", user.getLoginAccount());
        data.put("userName", user.getUserName());
        return Result.success("登录成功", data);
    }

    // ==================== 2. 退出登录 ====================
    public Result<Void> logout(Long userId) {
        if (userId != null) {
            SysUser user = sysUserRepository.findById(userId).orElse(null);
            if (user != null) {
                LoginLog log = new LoginLog();
                log.setUserId(userId);
                log.setLoginAccount(user.getLoginAccount());
                log.setLoginTime(new Date());
                log.setLogoutTime(new Date());
                log.setLoginStatus((byte) 1);
                log.setIsDelete((byte) 0);
                loginLogRepository.save(log);
            }
        }
        return Result.success("退出成功", null);
    }

    // ==================== 3. 刷新Token ====================
    public Result<Map<String, Object>> refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return Result.unauthorized("Token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String loginAccount = jwtUtil.getLoginAccountFromToken(token);

        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null || user.getUserStatus() == 1) {
            return Result.unauthorized("用户不存在或已被禁用");
        }

        String newToken = jwtUtil.generateToken(userId, loginAccount);
        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        return Result.success("刷新成功", data);
    }

    // ==================== 4. 重置密码 ====================
    public Result<Void> resetPassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.unauthorized("用户不存在");
        }

        if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
            return Result.paramError("原密码错误");
        }

        if (newPassword == null || newPassword.length() < 6) {
            return Result.paramError("新密码长度不能少于6位");
        }

        user.setPassword(PasswordUtil.encrypt(newPassword));
        user.setUpdateTime(new Date());
        sysUserRepository.save(user);

        return Result.success("密码重置成功", null);
    }

// ... existing code ...

    // ==================== 5. 获取当前登录用户信息 ====================
    public Result<Map<String, Object>> getCurrentUser(Long userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.unauthorized("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("loginAccount", user.getLoginAccount());
        data.put("userName", user.getUserName());
        data.put("phone", user.getPhone());
        data.put("orgId", user.getOrgId());
        return Result.success(data);
    }

    // ==================== 6. 手动解锁账号 ====================
    public Result<Void> unlockAccount(String loginAccount) {
        SysUser user = sysUserRepository.findByLoginAccountAndIsDelete(loginAccount, (byte) 0).orElse(null);
        if (user == null) {
            return Result.paramError("用户不存在");
        }
        if (user.getLockTime() == null) {
            return Result.success("该账号未被锁定", null);
        }
        user.setLockTime(null);
        user.setUpdateTime(new Date());
        sysUserRepository.save(user);
        return Result.success("解锁成功", null);
    }

    // ... existing code ...


    // ==================== 私有方法 ====================

    /**
     * 处理登录失败 - 统计24h内失败次数，达5次则锁定30分钟
     */
    private void handleLoginFail(SysUser user) {
        if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -24);
        int failCount = loginLogRepository.countFailuresSince(user.getLoginAccount(), cal.getTime());

        if (failCount >= MAX_LOGIN_FAIL) {
            Calendar lockCal = Calendar.getInstance();
            lockCal.add(Calendar.MINUTE, LOCK_MINUTES);
            user.setLockTime(lockCal.getTime());
            sysUserRepository.save(user);
        }
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(Long userId, String loginAccount, String loginIp, Byte status) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setLoginAccount(loginAccount);
        log.setLoginIp(loginIp);
        log.setLoginTime(new Date());
        log.setLoginStatus(status);
        log.setIsDelete((byte) 0);
        loginLogRepository.save(log);
    }
}
