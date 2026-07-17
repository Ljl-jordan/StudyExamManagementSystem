package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface UserService {

    Result<Page<SysUser>> page(Integer pageNum, Integer pageSize, String keyword, Long orgId, HttpServletRequest request);

    Result<Void> add(Map<String, Object> params, Long userId);

    Result<Void> update(Long id, Map<String, Object> params, Long userId);

    Result<Void> delete(Long id);

    Result<Void> resetPassword(Long userId, String newPassword, Long operatorId);

    Result<List<Map<String, Object>>> importUsers(MultipartFile file, Long operatorId);

    Result<Void> batchUpdateOrg(List<Long> userIds, Long orgId, Long operatorId);

    Result<Void> batchDisable(List<Long> userIds, Long operatorId);

    Result<Map<String, Object>> selectUsers(Integer pageNum, Integer pageSize, String keyword);
}
