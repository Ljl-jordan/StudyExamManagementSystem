package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationService {

    List<Organization> findAll();

    Optional<Organization> findById(Long id);

    List<Organization> findByParentId(Long parentId);

    Organization save(Organization organization);

    Organization update(Long id, Organization organization);

    void deleteById(Long id);
}
