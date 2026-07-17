package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.loginAccount = :loginAccount AND l.loginStatus = 0 AND l.loginTime >= :since")
    int countFailuresSince(@Param("loginAccount") String loginAccount, @Param("since") Date since);

    @Query("SELECT l FROM LoginLog l WHERE l.isDelete = 0 ORDER BY l.loginTime DESC")
    Page<LoginLog> findAllActive(Pageable pageable);

    @Query("SELECT l FROM LoginLog l WHERE l.isDelete = 0 AND l.loginAccount LIKE %:keyword% ORDER BY l.loginTime DESC")
    Page<LoginLog> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
