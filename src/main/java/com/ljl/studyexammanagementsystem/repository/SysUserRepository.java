// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/dao/repository/SysUserRepository.java
package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// ... existing code ...
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByLoginAccountAndIsDelete(String loginAccount, Byte isDelete);

    java.util.List<SysUser> findByIsDelete(Byte isDelete);
}
