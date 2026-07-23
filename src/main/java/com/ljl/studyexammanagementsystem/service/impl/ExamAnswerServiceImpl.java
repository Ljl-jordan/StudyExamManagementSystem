package com.ljl.studyexammanagementsystem.service.impl;
import com.ljl.studyexammanagementsystem.entity.*;
import com.ljl.studyexammanagementsystem.repository.*;
import com.ljl.studyexammanagementsystem.service.ExamAnswerService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class ExamAnswerServiceImpl implements ExamAnswerService {

    @Autowired
    private ExamAnswerSheetRepository examAnswerSheetRepository;

    @Autowired
    private ExamAnswerRecordRepository examAnswerRecordRepository;

    @Autowired
    private ExamPaperRepository examPaperRepository;

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
    private SysUserRepository sysUserRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    @Autowired
    private ExamPaperQuestionRepository examPaperQuestionRepository;
    @Autowired
    private AsyncExportRepository asyncExportRepository;
    @Override
    public Result<?> getExamQuestions(Long paperId, HttpServletRequest request) {
        try {
            // 获取当前用户信息
            Long userId = (Long) request.getAttribute("userId");

            // 校验试卷是否存在且已发布
            ExamPaper examPaper = examPaperRepository.findById(paperId).orElse(null);
            if (examPaper == null || examPaper.getPaperStatus() != 1) { // 1-已发布
                return Result.error("试卷不存在或未发布");
            }

            // 校验考试时间
            Date now = new Date();
            if (now.before(examPaper.getStartTime())) {
                return Result.error("考试尚未开始");
            }
            if (examPaper.getEndTime() != null && now.after(examPaper.getEndTime())) {
                return Result.error("考试已结束");
            }

            // 检查是否已存在答卷（防止重复进入）
            ExamAnswerSheet existingSheet = examAnswerSheetRepository.findByPaperIdAndUserIdAndIsDelete(paperId, userId, (byte) 0);
            if (existingSheet != null && existingSheet.getStatus() == 0) { // 进行中的试卷
                return Result.error("您已在答题中，请勿重复进入");
            }

            // 创建答卷
            ExamAnswerSheet answerSheet = new ExamAnswerSheet();
            answerSheet.setPaperId(paperId);
            answerSheet.setUserId(userId);
            answerSheet.setStatus((byte) 0); // 0-进行中
            answerSheet.setCreateTime(new Date());
            answerSheet.setUpdateTime(new Date());
            answerSheet.setIsDelete((byte) 0);

            // 设置用户信息
            SysUser user = sysUserRepository.findById(userId).orElse(null);
            if (user != null) {
                answerSheet.setUserName(user.getUserName());
                answerSheet.setOrganizationId(user.getOrgId());
            }

            answerSheet.setStartTime(new Date());
            answerSheet = examAnswerSheetRepository.save(answerSheet);

            // 获取试卷题目
            List<ExamPaperQuestion> paperQuestions = examPaperQuestionRepository.findByPaperIdOrderByQuestionOrder(paperId);
            List<Map<String, Object>> questions = new ArrayList<>();

            for (ExamPaperQuestion paperQuestion : paperQuestions) {
                QbQuestion question = qbQuestionRepository.findById(paperQuestion.getQuestionId()).orElse(null);
                if (question != null) {
                    Map<String, Object> questionInfo = new HashMap<>();
                    questionInfo.put("id", question.getId());
                    questionInfo.put("title", question.getQuestionTitle());
                    questionInfo.put("type", question.getQuestionType());
                    questionInfo.put("score", paperQuestion.getScore());
                    questionInfo.put("order", paperQuestion.getQuestionOrder());

                    // 根据题目类型获取具体选项信息
                    switch (question.getQuestionType()) {
                        case 1: // 单选题
                            QbQuestionSingle single = qbQuestionSingleRepository.findByQuestionId(question.getId());
                            if (single != null) {
                                questionInfo.put("options", Arrays.asList(
                                        single.getOptionA(),
                                        single.getOptionB(),
                                        single.getOptionC(),
                                        single.getOptionD()
                                ));
                            }
                            break;
                        case 2: // 多选题
                            QbQuestionMultiple multiple = qbQuestionMultipleRepository.findByQuestionId(question.getId());
                            if (multiple != null) {
                                questionInfo.put("options", Arrays.asList(
                                        multiple.getOptionA(),
                                        multiple.getOptionB(),
                                        multiple.getOptionC(),
                                        multiple.getOptionD(),
                                        multiple.getOptionE(),
                                        multiple.getOptionF()
                                ));
                            }
                            break;
                        case 3: // 判断题
                            questionInfo.put("options", Arrays.asList("正确", "错误"));
                            break;
                        case 4: // 填空题
                            QbQuestionBlank blank = qbQuestionBlankRepository.findByQuestionId(question.getId());
                            if (blank != null) {
                                questionInfo.put("blanksCount", blank.getBlankCount());
                            }
                            break;
                        case 5: // 简答题
                            questionInfo.put("wordLimit", 500); // 假设字数限制为500
                            break;
                    }

                    questions.add(questionInfo);
                }
            }

            // 返回试卷信息和题目列表
            Map<String, Object> result = new HashMap<>();
            result.put("answerSheetId", answerSheet.getId());
            result.put("paperInfo", examPaper);
            result.put("questions", questions);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取试题失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> saveAnswer(Long answerSheetId, Long questionId, String userAnswer, HttpServletRequest request) {
        try {
            // 校验答卷是否存在且进行中
            ExamAnswerSheet answerSheet = examAnswerSheetRepository.findById(answerSheetId).orElse(null);
            if (answerSheet == null || answerSheet.getStatus() != 0) { // 0-进行中
                return Result.error("答卷不存在或已完成");
            }

            // 校验用户权限
            Long userId = (Long) request.getAttribute("userId");
            if (!Objects.equals(answerSheet.getUserId(), userId)) {
                return Result.error("无权限操作他人答卷");
            }

            // 获取题目信息
            QbQuestion question = qbQuestionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return Result.error("题目不存在");
            }

            // 查找或创建答题记录
            ExamAnswerRecord answerRecord = examAnswerRecordRepository.findByAnswerSheetIdAndQuestionId(answerSheetId, questionId);
            if (answerRecord == null) {
                answerRecord = new ExamAnswerRecord();
                answerRecord.setAnswerSheetId(answerSheetId);
                answerRecord.setQuestionId(questionId);
                answerRecord.setQuestionType(question.getQuestionType());
                answerRecord.setScore(question.getScore());

                // 根据题型获取正确答案
                String correctAnswer = getCorrectAnswerByQuestionIdAndType(questionId, question.getQuestionType());
                answerRecord.setCorrectAnswer(correctAnswer);

                answerRecord.setCreateTime(new Date());
            }

            answerRecord.setUserAnswer(userAnswer);
            answerRecord.setUpdateTime(new Date());

            examAnswerRecordRepository.save(answerRecord);

            return Result.success("答案保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存答案失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> submitPaper(Long answerSheetId, HttpServletRequest request) {
        try {
            // 校验答卷
            ExamAnswerSheet answerSheet = examAnswerSheetRepository.findById(answerSheetId).orElse(null);
            if (answerSheet == null || answerSheet.getStatus() != 0) { // 0-进行中
                return Result.error("答卷不存在或已完成");
            }

            // 校验用户权限
            Long userId = (Long) request.getAttribute("userId");
            if (!Objects.equals(answerSheet.getUserId(), userId)) {
                return Result.error("无权限操作他人答卷");
            }

            // 校验考试时间（如果已超过截止时间则不允许手动交卷）
            ExamPaper examPaper = examPaperRepository.findById(answerSheet.getPaperId()).orElse(null);
            if (examPaper != null && examPaper.getEndTime() != null && new Date().after(examPaper.getEndTime())) {
                return Result.error("考试时间已结束，无法手动交卷");
            }

            // 更新答卷状态
            answerSheet.setStatus((byte) 1); // 1-已提交
            answerSheet.setSubmitTime(new Date());
            answerSheet.setUpdateTime(new Date());

            // 进行客观题自动判分
            autoGradeObjectiveQuestions(answerSheetId);

            examAnswerSheetRepository.save(answerSheet);

            return Result.success("交卷成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("交卷失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> createAnswerSheet(Long paperId, HttpServletRequest request) {
        try {
            // 检查试卷是否有效
            ExamPaper examPaper = examPaperRepository.findById(paperId).orElse(null);
            if (examPaper == null || examPaper.getPaperStatus() != 1) { // 1-已发布
                return Result.error("试卷不存在或未发布");
            }

            Long userId = (Long) request.getAttribute("userId");

            // 检查是否已存在答卷
            ExamAnswerSheet existingSheet = examAnswerSheetRepository.findByPaperIdAndUserIdAndIsDelete(paperId, userId, (byte) 0);
            if (existingSheet != null && existingSheet.getStatus() == 0) {
                return Result.error("您已在答题中，请勿重复创建答卷");
            }

            // 创建答卷
            ExamAnswerSheet answerSheet = new ExamAnswerSheet();
            answerSheet.setPaperId(paperId);
            answerSheet.setUserId(userId);
            answerSheet.setStatus((byte) 0); // 0-进行中
            answerSheet.setCreateTime(new Date());
            answerSheet.setUpdateTime(new Date());
            answerSheet.setIsDelete((byte) 0);

            SysUser user = sysUserRepository.findById(userId).orElse(null);
            if (user != null) {
                answerSheet.setUserName(user.getUserName());
                answerSheet.setOrganizationId(user.getOrgId());
            }

            answerSheet.setStartTime(new Date());
            examAnswerSheetRepository.save(answerSheet);

            return Result.success("答卷创建成功", String.valueOf(answerSheet.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建答卷失败：" + e.getMessage());
        }
    }

    @Override
    public void autoSubmitExpiredPapers() {
        try {
            // 查询所有进行中且已超时的答卷
            Date now = new Date();
            List<ExamAnswerSheet> sheets = examAnswerSheetRepository.findAll((root, query, criteriaBuilder) -> {
                Predicate predicate = criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("status"), 0), // 0-进行中
                        criteriaBuilder.equal(root.get("isDelete"), 0)
                );

                return predicate;
            });

            int processedCount = 0;
            for (ExamAnswerSheet sheet : sheets) {
                ExamPaper examPaper = examPaperRepository.findById(sheet.getPaperId()).orElse(null);
                if (examPaper != null && examPaper.getEndTime() != null && now.after(examPaper.getEndTime())) {
                    // 自动交卷
                    sheet.setStatus((byte) 1); // 1-已提交
                    sheet.setSubmitTime(now);
                    sheet.setUpdateTime(now);

                    // 自动判分
                    autoGradeObjectiveQuestions(sheet.getId());

                    examAnswerSheetRepository.save(sheet);
                    processedCount++;
                }
            }

            if (processedCount > 0) {
                System.out.println("自动交卷了 " + processedCount + " 份试卷");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void autoGradeObjectiveQuestions() {
        try {
            // 查询所有已提交但未完成客观题判分的答卷
            List<ExamAnswerSheet> sheets = examAnswerSheetRepository.findAll((root, query, criteriaBuilder) -> {
                Predicate predicate = criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("status"), 1), // 1-已提交
                        criteriaBuilder.equal(root.get("isDelete"), 0)
                );

                return predicate;
            });

            int processedCount = 0;
            for (ExamAnswerSheet sheet : sheets) {
                // 检查是否已经进行了客观题判分
                List<ExamAnswerRecord> records = examAnswerRecordRepository.findByAnswerSheetId(sheet.getId());
                boolean needAutoGrade = false;
                for (ExamAnswerRecord record : records) {
                    if (record.getQuestionType() <= 4 && record.getAutoScore() == null) { // 客观题未评分
                        needAutoGrade = true;
                        break;
                    }
                }

                if (needAutoGrade) {
                    autoGradeObjectiveQuestions(sheet.getId());
                    processedCount++;
                }
            }

            if (processedCount > 0) {
                System.out.println("自动判分了 " + processedCount + " 份试卷");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoGradeObjectiveQuestions(Long answerSheetId) {
        try {
            List<ExamAnswerRecord> records = examAnswerRecordRepository.findByAnswerSheetId(answerSheetId);

            double autoScore = 0.0;
            double totalScore = 0.0;

            for (ExamAnswerRecord record : records) {
                // 只对客观题进行自动判分（单选、多选、判断、填空）
                if (record.getQuestionType() <= 4) { // 1-单选，2-多选，3-判断，4-填空
                    if (record.getUserAnswer() != null && record.getCorrectAnswer() != null) {
                        if (record.getUserAnswer().equals(record.getCorrectAnswer())) {
                            record.setAutoScore(record.getScore());
                            record.setIsRight((byte) 1);
                            autoScore += record.getScore();
                        } else {
                            record.setAutoScore(0.0);
                            record.setIsRight((byte) 0);
                        }
                    } else {
                        record.setAutoScore(0.0);
                        record.setIsRight((byte) 0);
                    }
                } else { // 主观题
                    record.setAutoScore(0.0);
                    record.setIsRight((byte) 0);
                }

                record.setUpdateTime(new Date());
                examAnswerRecordRepository.save(record);

                totalScore += record.getScore();
            }

            // 更新答卷分数
            ExamAnswerSheet answerSheet = examAnswerSheetRepository.findById(answerSheetId).orElse(null);
            if (answerSheet != null) {
                answerSheet.setAutoScore(BigDecimal.valueOf(autoScore));
                answerSheet.setTotalScore(BigDecimal.valueOf(autoScore)); // 初始总分为客观题得分

                // 检查是否及格
                ExamPaper examPaper = examPaperRepository.findById(answerSheet.getPaperId()).orElse(null);
                if (examPaper != null && answerSheet.getTotalScore().compareTo(examPaper.getPassingScore()) >= 0) {
                    answerSheet.setPassFlag((byte) 1); // 及格
                } else {
                    answerSheet.setPassFlag((byte) 0); // 不及格
                }

                answerSheet.setUpdateTime(new Date());
                examAnswerSheetRepository.save(answerSheet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result<?> getAnswerSheetDetail(Long answerSheetId, HttpServletRequest request) {
        try {
            ExamAnswerSheet answerSheet = examAnswerSheetRepository.findById(answerSheetId).orElse(null);
            if (answerSheet == null) {
                return Result.error("答卷不存在");
            }

            // 检查权限
            Long userId = (Long) request.getAttribute("userId");
            Long orgId = (Long) request.getAttribute("orgId");

            // 获取用户数据权限范围
            Byte dataScope = dataPermissionUtil.getDataScope(userId);

            boolean hasPermission = false;
            if (!hasPermission && !Objects.equals(answerSheet.getUserId(), userId)) {
                return Result.error("无权限查看此答卷");
            }

            // 获取答题记录
            List<ExamAnswerRecord> answerRecords = examAnswerRecordRepository.findByAnswerSheetId(answerSheetId);

            Map<String, Object> result = new HashMap<>();
            result.put("answerSheet", answerSheet);
            result.put("answers", answerRecords);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取答卷详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> gradeEssayQuestion(Long answerSheetId, Long questionId, Double score, HttpServletRequest request) {
        try {
            // 校验参数
            if (score == null || score < 0) {
                return Result.error("评分不能小于0");
            }

            ExamAnswerRecord answerRecord = examAnswerRecordRepository.findByAnswerSheetIdAndQuestionId(answerSheetId, questionId);
            if (answerRecord == null) {
                return Result.error("答题记录不存在");
            }

            // 检查是否为主观题
            if (answerRecord.getQuestionType() != 5) { // 5-简答
                return Result.error("该题目不是简答题，无法人工评分");
            }

            // 设置人工评分
            answerRecord.setManualScore(score);
            answerRecord.setUpdateTime(new Date());
            examAnswerRecordRepository.save(answerRecord);

            // 更新答卷总分和及格状态
            updateAnswerSheetScore(answerSheetId);

            return Result.success("评分成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("评分失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> batchGradeEssayQuestions(Map<String, Object> params, HttpServletRequest request) {
        try {
            // 参数解析
            List<Map<String, Object>> grades = (List<Map<String, Object>>) params.get("grades");

            if (grades == null || grades.isEmpty()) {
                return Result.error("评分数据不能为空");
            }

            Set<Long> answerSheetIds = new HashSet<>();

            for (Map<String, Object> gradeItem : grades) {
                Long answerSheetId = ((Integer) gradeItem.get("answerSheetId")).longValue();
                Long questionId = ((Integer) gradeItem.get("questionId")).longValue();
                Double score = (Double) gradeItem.get("score");

                ExamAnswerRecord answerRecord = examAnswerRecordRepository.findByAnswerSheetIdAndQuestionId(answerSheetId, questionId);
                if (answerRecord == null) {
                    return Result.error("答题记录不存在：" + answerSheetId + "-" + questionId);
                }

                if (answerRecord.getQuestionType() != 5) { // 5-简答
                    return Result.error("题目不是简答题，无法人工评分：" + questionId);
                }

                answerRecord.setManualScore(score);
                answerRecord.setUpdateTime(new Date());
                examAnswerRecordRepository.save(answerRecord);

                answerSheetIds.add(answerSheetId);
            }

            // 批量更新答卷分数
            for (Long answerSheetId : answerSheetIds) {
                updateAnswerSheetScore(answerSheetId);
            }

            return Result.success("批量评分成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量评分失败：" + e.getMessage());
        }
    }

    private void updateAnswerSheetScore(Long answerSheetId) {
        try {
            // 计算主观题总分
            List<ExamAnswerRecord> records = examAnswerRecordRepository.findByAnswerSheetId(answerSheetId);

            double autoScore = 0.0;
            double manualScore = 0.0;

            for (ExamAnswerRecord record : records) {
                if (record.getQuestionType() <= 4) { // 客观题
                    if (record.getAutoScore() != null) {
                        autoScore += record.getAutoScore();
                    }
                } else { // 主观题
                    if (record.getManualScore() != null) {
                        manualScore += record.getManualScore();
                    }
                }
            }

            // 更新答卷分数
            ExamAnswerSheet answerSheet = examAnswerSheetRepository.findById(answerSheetId).orElse(null);
            if (answerSheet != null) {
                answerSheet.setAutoScore(BigDecimal.valueOf(autoScore));
                answerSheet.setManualScore(BigDecimal.valueOf(manualScore));
                answerSheet.setTotalScore(BigDecimal.valueOf(autoScore + manualScore));

                // 检查是否及格
                ExamPaper examPaper = examPaperRepository.findById(answerSheet.getPaperId()).orElse(null);
                if (examPaper != null && answerSheet.getTotalScore().compareTo(examPaper.getPassingScore()) >= 0) {
                    answerSheet.setPassFlag((byte) 1); // 及格
                } else {
                    answerSheet.setPassFlag((byte) 0); // 不及格
                }

                answerSheet.setUpdateTime(new Date());

                // 如果所有主观题都已评分，则更新状态为已阅卷
                boolean allGraded = true;
                for (ExamAnswerRecord record : records) {
                    if (record.getQuestionType() == 5 && record.getManualScore() == null) { // 简答题未评分
                        allGraded = false;
                        break;
                    }
                }

                if (allGraded && answerSheet.getStatus() == 1) { // 已提交状态
                    answerSheet.setStatus((byte) 2); // 2-已阅卷
                }

                examAnswerSheetRepository.save(answerSheet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCorrectAnswerByQuestionIdAndType(Long questionId, Byte questionType) {
        switch (questionType) {
            case 1: // 单选题
                QbQuestionSingle single = qbQuestionSingleRepository.findByQuestionId(questionId);
                return single != null ? single.getCorrectAnswer() : null;
            case 2: // 多选题
                QbQuestionMultiple multiple = qbQuestionMultipleRepository.findByQuestionId(questionId);
                return multiple != null ? multiple.getCorrectAnswer() : null;
            case 3: // 判断题
                QbQuestionJudge judge = qbQuestionJudgeRepository.findByQuestionId(questionId);
                return judge != null ? judge.getCorrectAnswer().toString() : null;
            case 4: // 填空题
                QbQuestionBlank blank = qbQuestionBlankRepository.findByQuestionId(questionId);
                return blank != null ? blank.getAnswerJson() : null;
            case 5: // 简答题
                QbQuestionEssay essay = qbQuestionEssayRepository.findByQuestionId(questionId);
                return essay != null ? essay.getReferenceAnswer() : null;
            default:
                return null;
        }
    }

    @Override
    public Result<String> getScoreList(Integer pageNum, Integer pageSize, Map<String, Object> filters, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long orgId = (Long) request.getAttribute("orgId");

            Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

            Specification<ExamAnswerSheet> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // 添加基本过滤条件
                predicates.add(criteriaBuilder.equal(root.get("isDelete"), 0));

                // 根据数据权限过滤
                Byte dataScope = dataPermissionUtil.getDataScope(userId);
                if (dataScope == 2) { // 普通用户只能看自己的
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                } else if (dataScope == 1) { // 组织管理员看本组织及下级组织
                    List<Long> visibleOrgIds = dataPermissionUtil.getVisibleOrgIds(orgId);
                    predicates.add(criteriaBuilder.in(root.get("organizationId")).value(visibleOrgIds));
                }
                // dataScope == 0 时，超级管理员可以看到全部

                // 根据过滤条件添加查询条件
                if (filters != null) {
                    if (filters.containsKey("paperId") && filters.get("paperId") != null) {
                        predicates.add(criteriaBuilder.equal(root.get("paperId"), Long.parseLong(filters.get("paperId").toString())));
                    }

                    if (filters.containsKey("userId") && filters.get("userId") != null) {
                        predicates.add(criteriaBuilder.equal(root.get("userId"), Long.parseLong(filters.get("userId").toString())));
                    }

                    if (filters.containsKey("startTime") && filters.get("startTime") != null) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), (Date) filters.get("startTime")));
                    }

                    if (filters.containsKey("endTime") && filters.get("endTime") != null) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), (Date) filters.get("endTime")));
                    }

                    if (filters.containsKey("status") && filters.get("status") != null) {
                        predicates.add(criteriaBuilder.equal(root.get("status"), Byte.parseByte(filters.get("status").toString())));
                    }
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            Page<ExamAnswerSheet> page = examAnswerSheetRepository.findAll(spec, pageable);
            return Result.success(page.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询成绩列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> exportScoresSync(Map<String, Object> filters, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long orgId = (Long) request.getAttribute("orgId");

            // 构建查询条件
            Specification<ExamAnswerSheet> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // 添加基本过滤条件
                predicates.add(criteriaBuilder.equal(root.get("isDelete"), 0));

                // 根据数据权限过滤
                Byte dataScope = dataPermissionUtil.getDataScope(userId);
                if (dataScope == 2) { // 普通用户只能看自己的
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                } else if (dataScope == 1) { // 组织管理员看本组织及下级组织
                    List<Long> visibleOrgIds = dataPermissionUtil.getVisibleOrgIds(orgId);
                    predicates.add(criteriaBuilder.in(root.get("organizationId")).value(visibleOrgIds));
                }
                // dataScope == 0 时，超级管理员可以看到全部

                // 根据过滤条件添加查询条件
                if (filters != null) {
                    if (filters.containsKey("paperId") && filters.get("paperId") != null) {
                        predicates.add(criteriaBuilder.equal(root.get("paperId"), Long.parseLong(filters.get("paperId").toString())));
                    }

                    if (filters.containsKey("userId") && filters.get("userId") != null) {
                        predicates.add(criteriaBuilder.equal(root.get("userId"), Long.parseLong(filters.get("userId").toString())));
                    }

                    if (filters.containsKey("startTime") && filters.get("startTime") != null) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), (Date) filters.get("startTime")));
                    }

                    if (filters.containsKey("endTime") && filters.get("endTime") != null) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), (Date) filters.get("endTime")));
                    }

                    if (filters.containsKey("status") && filters.get("status") != null) {
                        predicates.add(criteriaBuilder.equal(root.get("status"), Byte.parseByte(filters.get("status").toString())));
                    }
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            // 检查记录数量是否超过5000条限制
            long totalCount = examAnswerSheetRepository.count(spec);
            if (totalCount > 5000) {
                return Result.error("同步导出数据量超过5000条限制，请使用异步导出功能");
            }

            // 查询所有符合条件的数据
            List<ExamAnswerSheet> sheets = examAnswerSheetRepository.findAll(spec);

            // 在实际实现中，这里会生成Excel文件并返回文件下载链接
            // 为简化，这里只做逻辑演示
            System.out.println("正在生成同步导出文件，共 " + sheets.size() + " 条记录");

            return Result.success("同步导出文件已生成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("同步导出失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> exportScoresAsync(Map<String, Object> filters, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");

            // 创建异步导出任务记录
            AsyncExport asyncExport = new AsyncExport();
            asyncExport.setUserId(userId);
            asyncExport.setExportParams(filters.toString()); // 使用exportParams字段替代tableName
            asyncExport.setTaskStatus((byte) 0); // 0-处理中，替代status
            asyncExport.setCreateTime(new Date());
            asyncExport.setUpdateTime(new Date());
            asyncExport.setIsDelete((byte) 0);

            // 保存导出任务记录
            asyncExportRepository.save(asyncExport);

            System.out.println("已创建异步导出任务，任务ID: " + asyncExport.getId());

            return Result.success("异步导出任务已创建，稍后可在异步导出管理中查看进度");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("异步导出失败：" + e.getMessage());
        }
    }
}
