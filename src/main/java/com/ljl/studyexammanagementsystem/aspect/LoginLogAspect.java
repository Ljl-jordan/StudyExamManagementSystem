package com.ljl.studyexammanagementsystem.aspect;

import com.ljl.studyexammanagementsystem.annotation.LoginLog;
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
 * 登录日志AOP切面
 * 拦截标注了 @LoginLog 注解的方法，自动记录登录日志
 */
@Aspect
@Component
public class LoginLogAspect {

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    /**
     * 环绕通知：拦截标注了 @LoginLog 的方法
     * 方法执行完毕后，根据返回的 Result 判断成功/失败，自动记录日志
     */
    @Around("@annotation(loginLog)")
    public Object around(ProceedingJoinPoint joinPoint, LoginLog loginLog) throws Throwable {
        // 先执行目标方法
        Object result = joinPoint.proceed();

        // 解析方法参数
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
            return result;
        }

        String loginAccount = loginVO.getLoginAccount();

        // 尝试从数据库查找用户（获取userId）
        SysUser user = sysUserRepository.findByLoginAccountAndIsDelete(loginAccount, (byte) 0).orElse(null);
        Long userId = user != null ? user.getId() : null;

        // 根据返回结果判断登录成功/失败
        if (result instanceof Result) {
            Result<?> r = (Result<?>) result;
            byte status = Result.CODE_SUCCESS.equals(r.getCode()) ? (byte) 1 : (byte) 0;
            saveLoginLog(userId, loginAccount, loginIp, status);
        }

        return result;
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(Long userId, String loginAccount, String loginIp, Byte status) {
        com.ljl.studyexammanagementsystem.entity.LoginLog log = new com.ljl.studyexammanagementsystem.entity.LoginLog();
        log.setUserId(userId);
        log.setLoginAccount(loginAccount);
        log.setLoginIp(loginIp);
        log.setLoginTime(new Date());
        log.setLoginStatus(status);
        log.setIsDelete((byte) 0);
        loginLogRepository.save(log);
    }
}
