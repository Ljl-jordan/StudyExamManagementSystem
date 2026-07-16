// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/configure/ScheduleTask.java
package com.ljl.studyexammanagementsystem.configure;

import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class ScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTask.class);

    @Autowired
    private SysUserRepository sysUserRepository;

    /**
     * 每小时执行一次：检查锁定超过24小时的账号，自动禁用
     * 逻辑：如果 lock_time 距今已超过24小时，说明锁定已过期但账号可能仍有风险，
     * 此处将其 user_status 设为 1（禁用），实现"24小时自动锁定"
     */
    @Scheduled(fixedRate = 3600000)
    public void autoLockExpiredAccounts() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -24);
        Date threshold = cal.getTime();

        List<SysUser> allUsers = sysUserRepository.findByIsDelete((byte) 0);
        int count = 0;
        for (SysUser user : allUsers) {
            if (user.getLockTime() != null && user.getLockTime().before(threshold) && user.getUserStatus() != 1) {
                user.setUserStatus((byte) 1);
                user.setUpdateTime(new Date());
                sysUserRepository.save(user);
                count++;
                log.warn("账号[{}]锁定超过24小时，已自动禁用", user.getLoginAccount());
            }
        }
        if (count > 0) {
            log.info("本次定时任务共自动禁用{}个账号", count);
        }
    }
}
