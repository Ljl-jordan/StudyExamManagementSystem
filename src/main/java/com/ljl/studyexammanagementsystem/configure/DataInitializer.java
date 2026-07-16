// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/configure/DataInitializer.java
package com.ljl.studyexammanagementsystem.configure;

import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 启动初始化器
 * 确保admin用户密码使用当前PasswordUtil加密规则
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private SysUserRepository sysUserRepository;

    @Override
    public void run(String... args) {
        SysUser admin = sysUserRepository.findByLoginAccountAndIsDelete("admin", (byte) 0).orElse(null);
        if (admin != null) {
            String expectedHash = PasswordUtil.encrypt("123456");
            if (!expectedHash.equals(admin.getPassword())) {
                admin.setPassword(expectedHash);
                admin.setUpdateTime(new Date());
                sysUserRepository.save(admin);
                log.info("已更新admin用户密码为当前加密规则");
            }
        }
    }
}
