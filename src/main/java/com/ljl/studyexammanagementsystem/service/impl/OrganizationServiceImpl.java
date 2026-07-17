package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.repository.OrganizationRepository;
import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.service.OrganizationService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Override
    public Result<List<Organization>> getTree() {
        List<Organization> allOrgs = organizationRepository.findByIsDelete((byte) 0);
        List<Organization> tree = buildTree(allOrgs, 0L);
        return Result.success(tree);
    }

    @Override
    public Result<Map<String, Object>> page(Integer pageNum, Integer pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Organization> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = organizationRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            page = organizationRepository.findAll(pageable);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @Override
    @Transactional
    public Result<Void> add(Organization organization, Long userId) {
        if (organization.getOrgName() == null || organization.getOrgName().trim().isEmpty()) {
            return Result.paramError("组织名称不能为空");
        }
        if (organization.getParentId() == null) {
            organization.setParentId(0L);
        }
        Organization duplicate = organizationRepository.findByOrgNameAndParentId(
                organization.getOrgName().trim(), organization.getParentId());
        if (duplicate != null) {
            return Result.paramError("同级下组织名称已存在");
        }
        organization.setCreateUser(userId);
        organization.setCreateTime(new Date());
        organization.setIsDelete((byte) 0);
        organizationRepository.save(organization);
        return Result.success("新增成功", null);
    }

    @Override
    @Transactional
    public Result<Void> update(Long id, Organization organization, Long userId) {
        Organization existing = organizationRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("组织不存在");
        }
        if (organization.getParentId() != null) {
            if (organization.getParentId().equals(id)) {
                return Result.paramError("父级组织不能选择自身");
            }
            if (isDescendant(id, organization.getParentId())) {
                return Result.paramError("父级组织不能选择自身的子节点");
            }
        }
        if (organization.getOrgName() != null && !organization.getOrgName().trim().isEmpty()) {
            Long parentId = organization.getParentId() != null ? organization.getParentId() : existing.getParentId();
            Organization duplicate = organizationRepository.findByOrgNameAndParentId(
                    organization.getOrgName().trim(), parentId);
            if (duplicate != null && !duplicate.getId().equals(id)) {
                return Result.paramError("同级下组织名称已存在");
            }
            existing.setOrgName(organization.getOrgName().trim());
        }
        if (organization.getParentId() != null) {
            existing.setParentId(organization.getParentId());
        }
        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        organizationRepository.save(existing);
        return Result.success("修改成功", null);
    }

    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        Organization existing = organizationRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("组织不存在");
        }
        long childCount = organizationRepository.countByParentIdAndIsDelete(id, (byte) 0);
        if (childCount > 0) {
            return Result.businessBlock("存在子组织，无法删除");
        }
        long userCount = sysUserRepository.countByOrgIdAndIsDelete(id, (byte) 0);
        if (userCount > 0) {
            return Result.businessBlock("存在绑定用户，无法删除");
        }
        existing.setIsDelete((byte) 1);
        existing.setUpdateTime(new Date());
        organizationRepository.save(existing);
        return Result.success("删除成功", null);
    }

    @Override
    public List<Organization> findAll() {
        return organizationRepository.findByIsDelete((byte) 0);
    }

    private List<Organization> buildTree(List<Organization> allOrgs, Long parentId) {
        return allOrgs.stream()
                .filter(o -> parentId.equals(o.getParentId()))
                .peek(o -> {
                    List<Organization> children = buildTree(allOrgs, o.getId());
                    o.setChildren(children.isEmpty() ? null : children);
                })
                .collect(Collectors.toList());
    }

    private boolean isDescendant(Long currentId, Long targetParentId) {
        List<Organization> allOrgs = organizationRepository.findByIsDelete((byte) 0);
        Set<Long> visited = new HashSet<>();
        return checkDescendant(allOrgs, currentId, targetParentId, visited);
    }

    private boolean checkDescendant(List<Organization> allOrgs, Long currentId, Long targetParentId, Set<Long> visited) {
        if (visited.contains(currentId)) return false;
        visited.add(currentId);
        List<Organization> children = allOrgs.stream()
                .filter(o -> currentId.equals(o.getParentId()) && o.getIsDelete() == 0)
                .collect(Collectors.toList());
        for (Organization child : children) {
            if (child.getId().equals(targetParentId)) return true;
            if (checkDescendant(allOrgs, child.getId(), targetParentId, visited)) return true;
        }
        return false;
    }
}
