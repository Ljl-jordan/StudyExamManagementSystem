package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<SysMessage, Long> {

    @Query("SELECT m FROM SysMessage m WHERE m.receiveUserId = :userId AND m.isDelete = 0 ORDER BY m.createTime DESC")
    Page<SysMessage> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM SysMessage m WHERE m.receiveUserId = :userId AND m.isDelete = 0 AND m.readFlag = 0 ORDER BY m.createTime DESC")
    Page<SysMessage> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM SysMessage m WHERE m.receiveUserId = :userId AND m.isDelete = 0 AND m.readFlag = 0")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM SysMessage m WHERE m.receiveUserId = :userId AND m.isDelete = 0 AND m.readFlag = 0")
    List<SysMessage> findAllUnreadByUserId(@Param("userId") Long userId);
}
