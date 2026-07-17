package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.repository.OrganizationRepository;
import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.service.UserService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.utils.PasswordUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
//注入两个repository表，一个用于用户操作，一个用于组织操作；注入工具类用于处理数据权限
    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;
//正则表达式用于手机号格式校验
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public Result<Page<SysUser>> page(Integer pageNum, Integer pageSize, String keyword, Long orgId, HttpServletRequest request) {
        //从请求拿到当前登陆用户id，所属组织id
        Long userId = (Long) request.getAttribute("userId");
        Long orgIdAttr = (Long) request.getAttribute("orgId");
        Byte dataScope = dataPermissionUtil.getDataScope(userId);//获取当前用户的数据权限范围
/*构建分页对象，同时构建动态查询条件Specification：
        1.永久过滤逻辑删除用户 isDelete=0
        2.根据权限控制能看到哪些组织的用户
        */
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Specification<SysUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDelete"), (byte) 0));

            if (dataScope == 1) {
                List<Long> visibleOrgIds = dataPermissionUtil.getVisibleOrgIds(orgIdAttr);
                predicates.add(root.get("orgId").in(visibleOrgIds));
            } else if (dataScope == 2) {
                predicates.add(cb.equal(root.get("id"), userId));
            }

            if (orgId != null) {
                if (dataScope == 0) {
                    predicates.add(cb.equal(root.get("orgId"), orgId));
                } else {
                    List<Long> visibleOrgIds = dataPermissionUtil.getVisibleOrgIds(orgIdAttr);
                    if (visibleOrgIds.contains(orgId)) {
                        predicates.add(cb.equal(root.get("orgId"), orgId));
                    }
                }
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim();
                predicates.add(cb.or(
                        cb.like(root.get("userName"), "%" + kw + "%"),
                        cb.like(root.get("loginAccount"), "%" + kw + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysUser> page = sysUserRepository.findAll(spec, pageable);

//        Map<String, Object> data = new HashMap<>();
//        List<Map<String, Object>> list = new ArrayList<>();
//        for (SysUser u : page.getContent()) {
//            //通过map组装用户信息
//            Map<String, Object> item = new HashMap<>();
//            item.put("userId", u.getId());
//            item.put("loginAccount", u.getLoginAccount());
//            item.put("userName", u.getUserName());
//            item.put("phone", u.getPhone());
//            item.put("orgId", u.getOrgId());
//            item.put("userStatus", u.getUserStatus());
//            item.put("createTime", u.getCreateTime());
//            list.add(item);
//        }
//        //封装分页信息
//        data.put("list", list);
//        data.put("total", page.getTotalElements());
//        data.put("pageNum", pageNum);
//        data.put("pageSize", pageSize);
        return Result.success(page);
    }

    @Override
    @Transactional
    public Result<Void> add(Map<String, Object> params, Long userId) {
        String loginAccount = (String) params.get("loginAccount");
        String userName = (String) params.get("userName");
        String phone = (String) params.get("phone");
        Long orgId = params.get("orgId") != null ? Long.valueOf(params.get("orgId").toString()) : null;

        if (loginAccount == null || loginAccount.trim().isEmpty()) {
            return Result.paramError("登录账号不能为空");
        }
        if (userName == null || userName.trim().isEmpty()) {
            return Result.paramError("用户姓名不能为空");
        }
        if (sysUserRepository.existsByLoginAccountAndIsDelete(loginAccount.trim(), (byte) 0)) {
            return Result.paramError("登录账号已存在");
        }
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return Result.paramError("手机号格式不正确");
        }
        if (orgId != null) {
            Organization org = organizationRepository.findById(orgId).orElse(null);
            if (org == null || org.getIsDelete() == 1) {
                return Result.paramError("所属组织不存在");
            }
        }

        SysUser user = new SysUser();
        user.setLoginAccount(loginAccount.trim());
        user.setPassword(PasswordUtil.encrypt("123456"));
        user.setUserName(userName.trim());
        user.setPhone(phone != null ? phone.trim() : null);
        user.setOrgId(orgId != null ? orgId : 1L);
        user.setUserStatus((byte) 0);
        user.setCreateUser(userId);
        user.setCreateTime(new Date());
        user.setUpdateUser(userId);
        user.setUpdateTime(new Date());
        user.setIsDelete((byte) 0);
        sysUserRepository.save(user);
        return Result.success("新增成功", null);
    }

    @Override
    @Transactional
    public Result<Void> update(Long id, Map<String, Object> params, Long userId) {
        SysUser existing = sysUserRepository.findActiveById(id).orElse(null);
        if (existing == null) {
            return Result.paramError("用户不存在");
        }

        String userName = (String) params.get("userName");
        String phone = (String) params.get("phone");
        Long orgId = params.get("orgId") != null ? Long.valueOf(params.get("orgId").toString()) : null;

        if (userName != null && !userName.trim().isEmpty()) {
            existing.setUserName(userName.trim());
        }
        if (phone != null) {
            if (!phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
                return Result.paramError("手机号格式不正确");
            }
            existing.setPhone(phone.trim());
        }
        if (orgId != null) {
            Organization org = organizationRepository.findById(orgId).orElse(null);
            if (org == null || org.getIsDelete() == 1) {
                return Result.paramError("所属组织不存在");
            }
            existing.setOrgId(orgId);
        }

        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        sysUserRepository.save(existing);
        return Result.success("修改成功", null);
    }

    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        SysUser existing = sysUserRepository.findActiveById(id).orElse(null);
        if (existing == null) {
            return Result.paramError("用户不存在");
        }
        existing.setIsDelete((byte) 1);
        existing.setUpdateTime(new Date());
        sysUserRepository.save(existing);
        return Result.success("删除成功", null);
    }

    @Override
    @Transactional
    public Result<Void> resetPassword(Long targetUserId, String newPassword, Long operatorId) {
        SysUser user = sysUserRepository.findActiveById(targetUserId).orElse(null);
        if (user == null) {
            return Result.paramError("用户不存在");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.paramError("密码长度不能少于6位");
        }
        user.setPassword(PasswordUtil.encrypt(newPassword));
        user.setUpdateUser(operatorId);
        user.setUpdateTime(new Date());
        sysUserRepository.save(user);
        return Result.success("密码重置成功", null);
    }

    @Override
    @Transactional
    public Result<List<Map<String, Object>>> importUsers(MultipartFile file, Long operatorId) {
        if (file == null || file.isEmpty()) {
            return Result.paramError("请上传文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.paramError("仅支持Excel文件格式");
        }

        List<Map<String, Object>> errors = new ArrayList<>();
        List<SysUser> toSave = new ArrayList<>();
        Set<String> existAccounts = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                int rowNum = i + 1;

                String loginAccount = getCellString(row, 0);
                String userName = getCellString(row, 1);
                String phone = getCellString(row, 2);
                String orgIdStr = getCellString(row, 3);

                if (loginAccount == null || loginAccount.trim().isEmpty()) {
                    errors.add(buildError(rowNum, "登录账号为空"));
                    continue;
                }
                loginAccount = loginAccount.trim();

                if (userName == null || userName.trim().isEmpty()) {
                    errors.add(buildError(rowNum, "用户姓名为空"));
                    continue;
                }

                if (existAccounts.contains(loginAccount)) {
                    errors.add(buildError(rowNum, "文件内账号重复：" + loginAccount));
                    continue;
                }

                if (sysUserRepository.existsByLoginAccountAndIsDelete(loginAccount, (byte) 0)) {
                    errors.add(buildError(rowNum, "账号已存在：" + loginAccount));
                    continue;
                }

                if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
                    errors.add(buildError(rowNum, "手机号格式错误：" + phone));
                    continue;
                }

                Long orgId = null;
                if (orgIdStr != null && !orgIdStr.trim().isEmpty()) {
                    try {
                        orgId = Long.valueOf(orgIdStr.trim());
                    } catch (NumberFormatException e) {
                        errors.add(buildError(rowNum, "组织ID格式错误：" + orgIdStr));
                        continue;
                    }
                    Organization org = organizationRepository.findById(orgId).orElse(null);
                    if (org == null || org.getIsDelete() == 1) {
                        errors.add(buildError(rowNum, "组织不存在：" + orgId));
                        continue;
                    }
                }

                SysUser user = new SysUser();
                user.setLoginAccount(loginAccount);
                user.setPassword(PasswordUtil.encrypt("123456"));
                user.setUserName(userName.trim());
                user.setPhone(phone != null ? phone.trim() : null);
                user.setOrgId(orgId != null ? orgId : 1L);
                user.setUserStatus((byte) 0);
                user.setCreateUser(operatorId);
                user.setCreateTime(new Date());
                user.setUpdateUser(operatorId);
                user.setUpdateTime(new Date());
                user.setIsDelete((byte) 0);

                toSave.add(user);
                existAccounts.add(loginAccount);

            }
        } catch (Exception e) {
            return Result.serverError("文件解析失败：" + e.getMessage());
        }

        if (!errors.isEmpty()) {
            return Result.fail(400, "导入失败，共" + errors.size() + "行错误");
        }

        if (!toSave.isEmpty()) {
            sysUserRepository.saveAll(toSave);
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("successCount", toSave.size());
        resultData.put("errors", errors);
        return Result.success("导入成功", null);
    }

    @Override
    @Transactional
    public Result<Void> batchUpdateOrg(List<Long> userIds, Long orgId, Long operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.paramError("请选择用户");
        }
        if (orgId == null) {
            return Result.paramError("请选择目标组织");
        }
        Organization org = organizationRepository.findById(orgId).orElse(null);
        if (org == null || org.getIsDelete() == 1) {
            return Result.paramError("目标组织不存在");
        }
        for (Long uid : userIds) {
            SysUser user = sysUserRepository.findActiveById(uid).orElse(null);
            if (user != null) {
                user.setOrgId(orgId);
                user.setUpdateUser(operatorId);
                user.setUpdateTime(new Date());
                sysUserRepository.save(user);
            }
        }
        return Result.success("批量调整成功", null);
    }

    @Override
    @Transactional
    public Result<Void> batchDisable(List<Long> userIds, Long operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.paramError("请选择用户");
        }
        for (Long uid : userIds) {
            SysUser user = sysUserRepository.findActiveById(uid).orElse(null);
            if (user != null) {
                user.setUserStatus((byte) 1);
                user.setUpdateUser(operatorId);
                user.setUpdateTime(new Date());
                sysUserRepository.save(user);
            }
        }
        return Result.success("批量禁用成功", null);
    }

    @Override
    public Result<Map<String, Object>> selectUsers(Integer pageNum, Integer pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<SysUser> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = sysUserRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            page = sysUserRepository.findAllActive(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysUser u : page.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", u.getId());
            item.put("userName", u.getUserName());
            item.put("loginAccount", u.getLoginAccount());
            item.put("orgId", u.getOrgId());
            list.add(item);
        }
        data.put("list", list);
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    private String getCellString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private Map<String, Object> buildError(int rowNum, String reason) {
        Map<String, Object> error = new HashMap<>();
        error.put("row", rowNum);
        error.put("reason", reason);
        return error;
    }
}
