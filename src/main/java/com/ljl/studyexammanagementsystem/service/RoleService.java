package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.SysRole;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface RoleService {

    Result<Page<SysRole>> page(Integer pageNum, Integer pageSize);

    Result<Void> add(Map<String, Object> params, Long userId);

    Result<Void> update(Long id, Map<String, Object> params, Long userId);

    Result<Void> delete(Long id);

    Result<Void> copyRole(Long id, Long userId);

    Result<Void> allotMenu(Long roleId, List<Long> menuIds, Long userId);

    Result<Void> allotUser(List<Long> userIds, Long roleId);
}

