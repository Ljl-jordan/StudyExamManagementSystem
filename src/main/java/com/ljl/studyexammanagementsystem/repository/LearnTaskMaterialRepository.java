package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LearnTaskMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnTaskMaterialRepository extends JpaRepository<LearnTaskMaterial, Long> {

    List<LearnTaskMaterial> findByTaskIdAndIsDelete(Long taskId, Byte isDelete);

    long countByMaterialIdAndIsDelete(Long materialId, Byte isDelete);

    LearnTaskMaterial findByTaskIdAndMaterialIdAndIsDelete(Long taskId, Long materialId, Byte isDelete);
}
