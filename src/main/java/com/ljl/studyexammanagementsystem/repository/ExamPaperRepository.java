package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.ExamPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long>, JpaSpecificationExecutor<ExamPaper> {

    ExamPaper findByIdAndIsDelete(Long id, Byte isDelete);

    List<ExamPaper> findByIdInAndIsDelete(List<Long> ids, Byte isDelete);

    Page<ExamPaper> findAll(Specification<ExamPaper> spec, Pageable pageable);
}
