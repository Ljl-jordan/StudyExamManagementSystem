package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.*;
import com.ljl.studyexammanagementsystem.repository.*;
import com.ljl.studyexammanagementsystem.service.QbQuestionService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class QbQuestionServiceImpl implements QbQuestionService {

    @Autowired
    private QbQuestionRepository qbQuestionRepository;

    @Autowired
    private QbQuestionSingleRepository qbQuestionSingleRepository;

    @Autowired
    private QbQuestionMultipleRepository qbQuestionMultipleRepository;

    @Autowired
    private QbQuestionJudgeRepository qbQuestionJudgeRepository;

    @Autowired
    private QbQuestionBlankRepository qbQuestionBlankRepository;

    @Autowired
    private QbQuestionEssayRepository qbQuestionEssayRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    /**
     * 试题分页列表（带数据权限）
     */
    @Override
    public Result<Page<QbQuestion>> page(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Long userId, Long orgId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Byte dataScope = dataPermissionUtil.getDataScope(userId);

        Specification<QbQuestion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDelete"), (byte) 0));
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(root.get("questionTitle"), "%" + keyword.trim() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (dataScope == 2) {
                predicates.add(cb.equal(root.get("createUser"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<QbQuestion> page = qbQuestionRepository.findAll(spec, pageable);
        return Result.success(page);
    }

    /**
     * 试题详情
     */
    @Override
    public Result<QbQuestion> detail(Long id) {
        QbQuestion question = qbQuestionRepository.findByIdAndNotDeleted(id);
        if (question == null) {
            return Result.paramError("试题不存在");
        }
        return Result.success(question);
    }

    /**
     * 新增试题
     */
    @Override
    @Transactional
    public Result<Void> add(QbQuestion question, Object extraData, Long userId) {
        // 参数校验
        if (question.getQuestionTitle() == null || question.getQuestionTitle().trim().isEmpty()) {
            return Result.paramError("题干不能为空");
        }
        if (question.getQuestionType() == null) {
            return Result.paramError("题型不能为空");
        }
        if (question.getDifficultyLevel() == null) {
            return Result.paramError("难度等级不能为空");
        }
        if (question.getScore() == null || question.getScore() <= 0) {
            return Result.paramError("题目分值必须大于0");
        }
        if (question.getCategoryId() == null) {
            return Result.paramError("所属分类不能为空");
        }

        // 设置基本属性
        question.setCreateUser(userId);
        question.setCreateTime(new Date());
        question.setIsDelete((byte) 0);

        // 保存主表
        QbQuestion savedQuestion = qbQuestionRepository.save(question);

        // 根据题型保存附表数据
        Long questionId = savedQuestion.getId();
        Result<Void> result = saveExtraDataByType(questionId, question.getQuestionType(), extraData);
        if (result != null) {
            return result;
        }

        return Result.success("新增成功", null);
    }

    /**
     * 编辑试题
     */
    @Override
    @Transactional
    public Result<Void> update(Long id, QbQuestion question, Object extraData, Long userId) {
        QbQuestion existing = qbQuestionRepository.findByIdAndNotDeleted(id);
        if (existing == null) {
            return Result.paramError("试题不存在");
        }

        // 参数校验
        if (question.getQuestionTitle() != null && question.getQuestionTitle().trim().isEmpty()) {
            return Result.paramError("题干不能为空");
        }
        if (question.getScore() != null && question.getScore() <= 0) {
            return Result.paramError("题目分值必须大于0");
        }

        // 更新字段
        if (question.getQuestionTitle() != null && !question.getQuestionTitle().trim().isEmpty()) {
            existing.setQuestionTitle(question.getQuestionTitle().trim());
        }
        if (question.getDifficultyLevel() != null) {
            existing.setDifficultyLevel(question.getDifficultyLevel());
        }
        if (question.getScore() != null && question.getScore() > 0) {
            existing.setScore(question.getScore());
        }
        if (question.getCategoryId() != null) {
            existing.setCategoryId(question.getCategoryId());
        }

        // 如果题型发生变化，则需要清理旧的附表数据
        Byte oldQuestionType = existing.getQuestionType();
        Byte newQuestionType = question.getQuestionType();
        if (newQuestionType != null && !newQuestionType.equals(oldQuestionType)) {
            existing.setQuestionType(newQuestionType);
            // 清理旧题型的附表数据
            cleanOldExtraData(id, oldQuestionType);
        }

        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        qbQuestionRepository.save(existing);

        // 保存新的附表数据
        if (newQuestionType != null) {
            Result<Void> result = saveExtraDataByType(id, newQuestionType, extraData);
            if (result != null) {
                return result;
            }
        } else {
            // 使用原题型保存附表数据
            Result<Void> result = saveExtraDataByType(id, existing.getQuestionType(), extraData);
            if (result != null) {
                return result;
            }
        }

        return Result.success("修改成功", null);
    }

    /**
     * 删除试题
     */
    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        QbQuestion existing = qbQuestionRepository.findByIdAndNotDeleted(id);
        if (existing == null) {
            return Result.paramError("试题不存在");
        }

        // 逻辑删除主表
        existing.setIsDelete((byte) 1);
        existing.setUpdateTime(new Date());
        qbQuestionRepository.save(existing);

        // 逻辑删除附表数据 - 这里我们直接物理删除附表数据
        cleanOldExtraData(id, existing.getQuestionType());

        return Result.success("删除成功", null);
    }

    /**
     * 根据题型保存附表数据
     */
    private Result<Void> saveExtraDataByType(Long questionId, Byte questionType, Object extraData) {
        if (extraData == null) {
            return null;
        }

        switch (questionType) {
            case 1: // 单选题
                if (!(extraData instanceof Map)) {
                    return Result.paramError("单选题附加数据格式错误");
                }
                Map<String, Object> singleMap = (Map<String, Object>) extraData;

                // 检查必要字段
                if (!singleMap.containsKey("correctAnswer") || singleMap.get("correctAnswer") == null) {
                    return Result.paramError("单选题正确答案不能为空");
                }

                QbQuestionSingle single = qbQuestionSingleRepository.findByQuestionId(questionId);
                if (single == null) {
                    single = new QbQuestionSingle();
                    single.setQuestionId(questionId);
                }

                if (singleMap.get("optionA") != null) single.setOptionA(singleMap.get("optionA").toString());
                if (singleMap.get("optionB") != null) single.setOptionB(singleMap.get("optionB").toString());
                if (singleMap.get("optionC") != null) single.setOptionC(singleMap.get("optionC").toString());
                if (singleMap.get("optionD") != null) single.setOptionD(singleMap.get("optionD").toString());
                if (singleMap.get("correctAnswer") != null) single.setCorrectAnswer(singleMap.get("correctAnswer").toString());

                qbQuestionSingleRepository.save(single);
                break;

            case 2: // 多选题
                if (!(extraData instanceof Map)) {
                    return Result.paramError("多选题附加数据格式错误");
                }
                Map<String, Object> multipleMap = (Map<String, Object>) extraData;

                // 检查必要字段
                if (!multipleMap.containsKey("correctAnswer") || multipleMap.get("correctAnswer") == null) {
                    return Result.paramError("多选题正确答案不能为空");
                }

                QbQuestionMultiple multiple = qbQuestionMultipleRepository.findByQuestionId(questionId);
                if (multiple == null) {
                    multiple = new QbQuestionMultiple();
                    multiple.setQuestionId(questionId);
                }

                if (multipleMap.get("optionA") != null) multiple.setOptionA(multipleMap.get("optionA").toString());
                if (multipleMap.get("optionB") != null) multiple.setOptionB(multipleMap.get("optionB").toString());
                if (multipleMap.get("optionC") != null) multiple.setOptionC(multipleMap.get("optionC").toString());
                if (multipleMap.get("optionD") != null) multiple.setOptionD(multipleMap.get("optionD").toString());
                if (multipleMap.get("optionE") != null) multiple.setOptionE(multipleMap.get("optionE").toString());
                if (multipleMap.get("optionF") != null) multiple.setOptionF(multipleMap.get("optionF").toString());
                if (multipleMap.get("correctAnswer") != null) multiple.setCorrectAnswer(multipleMap.get("correctAnswer").toString());

                qbQuestionMultipleRepository.save(multiple);
                break;

            case 3: // 判断题
                if (!(extraData instanceof Map)) {
                    return Result.paramError("判断题附加数据格式错误");
                }
                Map<String, Object> judgeMap = (Map<String, Object>) extraData;

                // 检查必要字段
                if (!judgeMap.containsKey("correctAnswer")) {
                    return Result.paramError("判断题正确答案不能为空");
                }

                QbQuestionJudge judge = qbQuestionJudgeRepository.findByQuestionId(questionId);
                if (judge == null) {
                    judge = new QbQuestionJudge();
                    judge.setQuestionId(questionId);
                }

                if (judgeMap.get("correctAnswer") != null) {
                    judge.setCorrectAnswer(Boolean.valueOf(judgeMap.get("correctAnswer").toString()));
                }

                qbQuestionJudgeRepository.save(judge);
                break;

            case 4: // 填空题
                if (!(extraData instanceof Map)) {
                    return Result.paramError("填空题附加数据格式错误");
                }
                Map<String, Object> blankMap = (Map<String, Object>) extraData;

                // 检查必要字段
                if (!blankMap.containsKey("answerJson") || blankMap.get("answerJson") == null) {
                    return Result.paramError("填空题答案不能为空");
                }

                QbQuestionBlank blank = qbQuestionBlankRepository.findByQuestionId(questionId);
                if (blank == null) {
                    blank = new QbQuestionBlank();
                    blank.setQuestionId(questionId);
                }

                if (blankMap.get("blankCount") != null) blank.setBlankCount(Integer.parseInt(blankMap.get("blankCount").toString()));
                if (blankMap.get("answerJson") != null) blank.setAnswerJson(blankMap.get("answerJson").toString());

                qbQuestionBlankRepository.save(blank);
                break;

            case 5: // 简答题
                if (!(extraData instanceof Map)) {
                    return Result.paramError("简答题附加数据格式错误");
                }
                Map<String, Object> essayMap = (Map<String, Object>) extraData;

                // 检查必要字段
                if (!essayMap.containsKey("referenceAnswer") || essayMap.get("referenceAnswer") == null) {
                    return Result.paramError("简答题参考答案不能为空");
                }

                QbQuestionEssay essay = qbQuestionEssayRepository.findByQuestionId(questionId);
                if (essay == null) {
                    essay = new QbQuestionEssay();
                    essay.setQuestionId(questionId);
                }

                if (essayMap.get("referenceAnswer") != null) essay.setReferenceAnswer(essayMap.get("referenceAnswer").toString());

                qbQuestionEssayRepository.save(essay);
                break;

            default:
                return Result.paramError("无效的题型");
        }

        return null;
    }

    /**
     * 切换题型时清理旧题型附表数据
     */
    @Override
    @Transactional
    public void cleanOldExtraData(Long questionId, Byte oldQuestionType) {
        if (oldQuestionType == null) {
            return;
        }

        switch (oldQuestionType) {
            case 1: // 单选题
                qbQuestionSingleRepository.deleteByQuestionId(questionId);
                break;
            case 2: // 多选题
                qbQuestionMultipleRepository.deleteByQuestionId(questionId);
                break;
            case 3: // 判断题
                qbQuestionJudgeRepository.deleteByQuestionId(questionId);
                break;
            case 4: // 填空题
                qbQuestionBlankRepository.deleteByQuestionId(questionId);
                break;
            case 5: // 简答题
                qbQuestionEssayRepository.deleteByQuestionId(questionId);
                break;
        }
    }
}
