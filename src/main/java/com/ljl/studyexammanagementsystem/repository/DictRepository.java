// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/dao/repository/DictRepository.java
package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.Dict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictRepository extends JpaRepository<Dict, Long> {

    Page<Dict> findByDictTypeAndIsDelete(String dictType, Byte isDelete, Pageable pageable);

    List<Dict> findByDictTypeAndIsDeleteOrderBySortAsc(String dictType, Byte isDelete);

    Page<Dict> findByIsDelete(Byte isDelete, Pageable pageable);
}
