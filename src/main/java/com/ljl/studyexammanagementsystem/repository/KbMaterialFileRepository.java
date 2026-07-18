package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.KbMaterialFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbMaterialFileRepository extends JpaRepository<KbMaterialFile, Long> {

    List<KbMaterialFile> findByMaterialIdAndIsDelete(Long materialId, Byte isDelete);

    long countByMaterialIdAndIsDelete(Long materialId, Byte isDelete);
}
