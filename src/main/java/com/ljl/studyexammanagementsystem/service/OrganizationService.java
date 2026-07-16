package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.dao.repository.OrganizationRepository;
import com.ljl.studyexammanagementsystem.entity.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    public Optional<Organization> findById(Long id) {
        return organizationRepository.findById(id);
    }

    public List<Organization> findByParentId(Long parentId) {
        return organizationRepository.findByParentId(parentId);
    }

    public Organization save(Organization organization) {
        return organizationRepository.save(organization);
    }

    public Organization update(Long id, Organization organization) {
        Organization existing = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("组织不存在"));
        existing.setOrgName(organization.getOrgName());
        existing.setParentId(organization.getParentId());
        existing.setUpdateUser(organization.getUpdateUser());
        return organizationRepository.save(existing);
    }

    public void deleteById(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("组织不存在");
        }
        organizationRepository.deleteById(id);
    }
}
