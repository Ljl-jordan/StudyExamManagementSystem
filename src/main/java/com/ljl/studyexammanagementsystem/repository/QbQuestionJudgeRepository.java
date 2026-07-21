package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.QbQuestionJudge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QbQuestionJudgeRepository extends JpaRepository<QbQuestionJudge, Long> {

    QbQuestionJudge findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
