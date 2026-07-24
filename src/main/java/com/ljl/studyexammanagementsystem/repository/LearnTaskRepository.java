package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LearnTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LearnTaskRepository extends JpaRepository<LearnTask, Long>, JpaSpecificationExecutor<LearnTask> {

    // 添加查找指定状态和截止时间范围内的任务的方法
    @Query("SELECT lt FROM LearnTask lt WHERE lt.taskStatus = :status AND lt.endTime BETWEEN :startDate AND :endDate AND lt.isDelete = 0")
    List<LearnTask> findByTaskStatusAndEndTimeBetween(@Param("status") Byte status,
                                                      @Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate);
}
