package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByParentIdAndIsDelete(Long parentId, Byte isDelete);

    List<Organization> findByIsDelete(Byte isDelete);

    @Query("SELECT o FROM Organization o WHERE o.isDelete = 0 AND o.orgName LIKE %:keyword% ORDER BY o.id")
    Page<Organization> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByParentIdAndIsDelete(Long parentId, Byte isDelete);

    @Query("SELECT o.id FROM Organization o WHERE o.isDelete = 0 AND o.parentId = :parentId")
    List<Long> findChildIdsByParentId(@Param("parentId") Long parentId);

    @Query("SELECT o FROM Organization o WHERE o.isDelete = 0 AND o.orgName = :orgName AND o.parentId = :parentId")
    Organization findByOrgNameAndParentId(@Param("orgName") String orgName, @Param("parentId") Long parentId);
}
