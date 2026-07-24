package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.ExamAnswerSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamAnswerSheetRepository extends JpaRepository<ExamAnswerSheet, Long>, JpaSpecificationExecutor<ExamAnswerSheet> {

    Page<ExamAnswerSheet> findByUserIdAndIsDelete(Long userId, Byte isDelete, Pageable pageable);

    Page<ExamAnswerSheet> findByPaperIdAndIsDelete(Long paperId, Byte isDelete, Pageable pageable);

    ExamAnswerSheet findByPaperIdAndUserIdAndIsDelete(Long paperId, Long userId, Byte isDelete);

    // 添加按试卷ID和状态统计的方法
    long countByPaperIdAndStatusAndIsDelete(Long paperId, Byte status, Byte isDelete);
}
