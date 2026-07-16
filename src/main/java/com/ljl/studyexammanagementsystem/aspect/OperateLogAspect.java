package com.ljl.studyexammanagementsystem.aspect;

import com.ljl.studyexammanagementsystem.annotation.OperateLog;
import com.ljl.studyexammanagementsystem.entity.LoginLog;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.repository.LoginLogRepository;
import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.vo.LoginVO;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 操作日志AOP切面
 * 拦截标注了 @OperateLog 注解的方法，自动记录登录日志到 sys_login_log 表
 */
@Aspect
@Component
public class OperateLogAspect {

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    /**
     * 环绕通知：拦截所有标注了 @OperateLog 的方法
     * 方法执行完毕后，根据操作类型和返回结果自动记录日志
     */
    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        // 先执行目标方法
        Object result = joinPoint.proceed();

        // 根据操作类型分发处理
        String type = operateLog.type();
        switch (type) {
            case "LOGIN":
                handleLoginLog(joinPoint, result);
                break;
            case "LOGOUT":
                handleLogoutLog(joinPoint);
                break;
            default:
                handleOperateLog(joinPoint, result, operateLog);
                break;
        }

        return result;
    }

    // ==================== 登录日志处理 ====================

    /**
     * 处理登录日志
     * 从方法参数中提取 LoginVO 和 loginIp，根据返回结果判断成功/失败
     */
    private void handleLoginLog(ProceedingJoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        LoginVO loginVO = null;
        String loginIp = null;

        for (Object arg : args) {
            if (arg instanceof LoginVO) {
                loginVO = (LoginVO) arg;
            } else if (arg instanceof String) {
                loginIp = (String) arg;
            }
        }

        if (loginVO == null) {
            return;
        }

        String loginAccount = loginVO.getLoginAccount();

        // 查找用户获取userId（用户不存在时userId为null）
        SysUser user = sysUserRepository.findByLoginAccountAndIsDelete(loginAccount, (byte) 0).orElse(null);
        Long userId = user != null ? user.getId() : null;

        // 根据返回Result的code判断成功/失败
        byte status = 0;
        if (result instanceof Result) {
            Result<?> r = (Result<?>) result;
            if (Result.CODE_SUCCESS.equals(r.getCode())) {
                status = 1;
            }
        }

        saveLoginLog(userId, loginAccount, loginIp, status, null);
    }

    // ==================== 退出日志处理 ====================

    /**
     * 处理退出登录日志
     * 从方法参数中提取 userId，记录登录时间+退出时间
     */
    private void handleLogoutLog(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = null;

        for (Object arg : args) {
            if (arg instanceof Long) {
                userId = (Long) arg;
                break;
            }
        }

        if (userId == null) {
            return;
        }

        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setLoginAccount(user.getLoginAccount());
        log.setLoginTime(new Date());
        log.setLogoutTime(new Date());
        log.setLoginStatus((byte) 1);
        log.setIsDelete((byte) 0);
        loginLogRepository.save(log);
    }

    // ==================== 通用操作日志处理 ====================

    /**
     * 处理通用操作日志（重置密码、解锁等）
     * 根据返回结果判断操作是否成功
     */
    private void handleOperateLog(ProceedingJoinPoint joinPoint, Object result, OperateLog operateLog) {
        // 尝试从参数中获取userId或loginAccount
        Long userId = null;
        String loginAccount = null;

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) {
                userId = (Long) arg;
            } else if (arg instanceof String) {
                loginAccount = (String) arg;
            }
        }

        // 如果只有loginAccount，查一下userId
        if (userId == null && loginAccount != null) {
            SysUser user = sysUserRepository.findByLoginAccountAndIsDelete(loginAccount, (byte) 0).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }

        // 如果只有userId，查一下loginAccount
        if (userId != null && loginAccount == null) {
            SysUser user = sysUserRepository.findById(userId).orElse(null);
            if (user != null) {
                loginAccount = user.getLoginAccount();
            }
        }

        if (loginAccount == null) {
            return;
        }

        byte status = 0;
        if (result instanceof Result) {
            Result<?> r = (Result<?>) result;
            if (Result.CODE_SUCCESS.equals(r.getCode())) {
                status = 1;
            }
        }

        saveLoginLog(userId, loginAccount, null, status, operateLog.module());
    }

    // ==================== 公共保存方法 ====================

    /**
     * 保存登录/操作日志
     */
    private void saveLoginLog(Long userId, String loginAccount, String loginIp, Byte status, String remark) {
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
