package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.repository.OrganizationRepository;
import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    // ==================== 依赖注入 ====================

    @Autowired
    private OrganizationRepository organizationRepository;

    // ==================== 1. 组织查询 ====================

    /**
     * 查询所有组织（平铺列表）
     */
    @Override
    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    /**
     * 根据ID查询单个组织
     */
    @Override
    public Optional<Organization> findById(Long id) {
        return organizationRepository.findById(id);
    }

    /**
     * 根据父级ID查询子组织列表（用于构建组织树）
     */
    @Override
    public List<Organization> findByParentId(Long parentId) {
        return organizationRepository.findByParentId(parentId);
    }

    // ==================== 2. 组织新增 ====================

    /**
     * 新增组织（直接持久化）
     */
    @Override
    public Organization save(Organization organization) {
        return organizationRepository.save(organization);
    }

    // ==================== 3. 组织修改 ====================

    /**
     * 更新组织信息
     * 校验存在性 → 更新字段（orgName, parentId, updateUser） → 持久化
     */
    @Override
    public Organization update(Long id, Organization organization) {
        Organization existing = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("组织不存在"));
        existing.setOrgName(organization.getOrgName());
        existing.setParentId(organization.getParentId());
        existing.setUpdateUser(organization.getUpdateUser());
        return organizationRepository.save(existing);
    }

    // ==================== 4. 组织删除 ====================

    /**
     * 根据ID删除组织（物理删除，先校验存在性）
     */
    @Override
    public void deleteById(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("组织不存在");
        }
        organizationRepository.deleteById(id);
    }
}
