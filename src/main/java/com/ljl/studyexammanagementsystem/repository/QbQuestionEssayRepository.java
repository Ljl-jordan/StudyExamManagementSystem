package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.QbQuestionEssay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QbQuestionEssayRepository extends JpaRepository<QbQuestionEssay, Long> {

    QbQuestionEssay findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
