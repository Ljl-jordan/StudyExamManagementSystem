package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.QbQuestionBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QbQuestionBlankRepository extends JpaRepository<QbQuestionBlank, Long> {

    QbQuestionBlank findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
