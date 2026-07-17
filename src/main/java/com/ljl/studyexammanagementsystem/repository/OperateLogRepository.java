package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.OperateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperateLogRepository extends JpaRepository<OperateLog, Long> {

    @Query("SELECT o FROM OperateLog o WHERE o.isDelete = 0 ORDER BY o.operTime DESC")
    Page<OperateLog> findAllActive(Pageable pageable);

    @Query("SELECT o FROM OperateLog o WHERE o.isDelete = 0 AND (o.operUserName LIKE %:keyword% OR o.module LIKE %:keyword%) ORDER BY o.operTime DESC")
    Page<OperateLog> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
