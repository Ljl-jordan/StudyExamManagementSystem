package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.QbQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QbQuestionRepository extends JpaRepository<QbQuestion, Long>, JpaSpecificationExecutor<QbQuestion> {

    @Query("SELECT q FROM QbQuestion q WHERE q.isDelete = 0 AND (:categoryId IS NULL OR q.categoryId = :categoryId) AND (:keyword IS NULL OR q.questionTitle LIKE %:keyword%)")
    Page<QbQuestion> findByConditions(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT q FROM QbQuestion q WHERE q.id = :id AND q.isDelete = 0")
    QbQuestion findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT q FROM QbQuestion q WHERE q.questionTitle = :questionTitle AND q.categoryId = :categoryId AND q.isDelete = 0")
    List<QbQuestion> findByQuestionTitleAndCategoryId(@Param("questionTitle") String questionTitle, @Param("categoryId") Long categoryId);

    @Query("SELECT q FROM QbQuestion q WHERE q.id IN :ids AND q.isDelete = :isDelete")
    List<QbQuestion> findByIdInAndIsDelete(@Param("ids") List<Long> ids, @Param("isDelete") Byte isDelete);

    long countByCategoryId(Long id);
}
