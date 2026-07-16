package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByParentId(Long parentId);
}
