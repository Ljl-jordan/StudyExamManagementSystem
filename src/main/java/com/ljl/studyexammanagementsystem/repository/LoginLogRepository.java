// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/dao/repository/LoginLogRepository.java
package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.loginAccount = :account AND l.loginStatus = 0 AND l.loginTime >= :since")
    int countFailuresSince(@Param("account") String account, @Param("since") Date since);
}
