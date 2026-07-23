package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.ExamPaperQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamPaperQuestionRepository extends JpaRepository<ExamPaperQuestion, Long> {

    List<ExamPaperQuestion> findByPaperIdOrderByQuestionOrder(Long paperId);

    List<ExamPaperQuestion> findByPaperIdIn(List<Long> paperIds);

    void deleteByPaperId(Long paperId);

}
