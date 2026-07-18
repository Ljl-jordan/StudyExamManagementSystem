package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.KbMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbMaterialRepository extends JpaRepository<KbMaterial, Long>, JpaSpecificationExecutor<KbMaterial> {

    long countByCategoryIdAndIsDelete(Long categoryId, Byte isDelete);

    long countByCategoryIdAndMaterialStatusAndIsDelete(Long categoryId, Byte materialStatus, Byte isDelete);

    @Query("SELECT m FROM KbMaterial m WHERE m.isDelete = 0 AND m.materialStatus = 1")
    Page<KbMaterial> findPublishedPage(Pageable pageable);

    @Query("SELECT m FROM KbMaterial m WHERE m.isDelete = 0 AND m.materialStatus = 1 AND m.materialName LIKE %:keyword%")
    Page<KbMaterial> findPublishedByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<KbMaterial> findByCategoryIdAndIsDelete(Long categoryId, Byte isDelete);
}
