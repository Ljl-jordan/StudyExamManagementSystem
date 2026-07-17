package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface OrganizationService {

    Result<List<Organization>> getTree();

    Result<Page<Organization>> page(Integer pageNum, Integer pageSize, String keyword);

    Result<Void> add(Organization organization, Long userId);

    Result<Void> update(Long id, Organization organization, Long userId);

    Result<Void> delete(Long id);

    List<Organization> findAll();
}
