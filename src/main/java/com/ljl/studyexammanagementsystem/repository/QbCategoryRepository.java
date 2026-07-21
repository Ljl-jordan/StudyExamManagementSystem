package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.QbCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QbCategoryRepository extends JpaRepository<QbCategory, Long>, JpaSpecificationExecutor<QbCategory> {

    List<QbCategory> findByIsDelete(Byte isDelete);

    QbCategory findByCategoryNameAndParentId(String categoryName, Long parentId);

    long countByParentIdAndIsDelete(Long parentId, Byte isDelete);

    @Query("SELECT COUNT(q) FROM QbQuestion q WHERE q.categoryId = :categoryId AND q.isDelete = 0")
    long countQuestionsByCategoryId(@Param("categoryId") Long categoryId);
}
