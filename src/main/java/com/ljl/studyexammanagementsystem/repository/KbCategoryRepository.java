package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.KbCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbCategoryRepository extends JpaRepository<KbCategory, Long>, JpaSpecificationExecutor<KbCategory> {

    List<KbCategory> findByIsDelete(Byte isDelete);

    List<KbCategory> findByParentIdAndIsDelete(Long parentId, Byte isDelete);

    @Query("SELECT c FROM KbCategory c WHERE c.isDelete = 0 AND c.categoryName = :categoryName AND c.parentId = :parentId")
    KbCategory findByCategoryNameAndParentId(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);

    @Query("SELECT c FROM KbCategory c WHERE c.isDelete = 0 AND c.categoryName LIKE %:keyword% ORDER BY c.id")
    Page<KbCategory> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByParentIdAndIsDelete(Long parentId, Byte isDelete);
}
