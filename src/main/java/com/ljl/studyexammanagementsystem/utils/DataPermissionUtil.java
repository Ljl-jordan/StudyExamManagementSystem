package com.ljl.studyexammanagementsystem.utils;

import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.repository.OrganizationRepository;
import com.ljl.studyexammanagementsystem.repository.SysRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataPermissionUtil {

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    public Byte getDataScope(Long userId) {
        List<Byte> scopes = sysRoleRepository.findDataScopesByUserId(userId);
        if (scopes == null || scopes.isEmpty()) {
            return 2;
        }
        Byte minScope = scopes.get(0);
        for (Byte s : scopes) {
            if (s < minScope) {
                minScope = s;
            }
        }
        return minScope;
    }

    public List<Long> getVisibleOrgIds(Long orgId) {
        Set<Long> result = new HashSet<>();
        result.add(orgId);
        collectChildOrgIds(orgId, result);
        return new ArrayList<>(result);
    }

    private void collectChildOrgIds(Long parentId, Set<Long> result) {
        List<Organization> children = organizationRepository.findByParentIdAndIsDelete(parentId, (byte) 0);
        for (Organization child : children) {
            result.add(child.getId());
            collectChildOrgIds(child.getId(), result);
        }
    }
}
