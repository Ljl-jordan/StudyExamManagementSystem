package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LearnStudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnStudyRecordRepository extends JpaRepository<LearnStudyRecord, Long> {

    LearnStudyRecord findByTaskIdAndUserIdAndMaterialIdAndIsDelete(Long taskId, Long userId, Long materialId, Byte isDelete);

    List<LearnStudyRecord> findByTaskIdAndUserIdAndIsDelete(Long taskId, Long userId, Byte isDelete);

    @Query("SELECT r FROM LearnStudyRecord r WHERE r.isDelete = 0 AND r.taskId = :taskId AND r.userId = :userId AND r.materialId = :materialId")
    LearnStudyRecord findActiveRecord(@Param("taskId") Long taskId, @Param("userId") Long userId, @Param("materialId") Long materialId);
}
