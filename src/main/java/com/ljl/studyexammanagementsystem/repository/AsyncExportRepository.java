// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/repository/AsyncExportRepository.java
package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.AsyncExport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsyncExportRepository extends JpaRepository<AsyncExport, Long> {

    Page<AsyncExport> findByUserIdAndIsDeleteOrderByCreateTimeDesc(Long userId, Byte isDelete, Pageable pageable);
}
