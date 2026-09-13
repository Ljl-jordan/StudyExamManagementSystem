package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.cache.CacheKeys;
import com.ljl.studyexammanagementsystem.entity.*;
import com.ljl.studyexammanagementsystem.repository.*;
import com.ljl.studyexammanagementsystem.lock.DistributedLockService;
import com.ljl.studyexammanagementsystem.service.QbQuestionService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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
    private QbCategoryRepository qbCategoryRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    @Autowired
    private DistributedLockService distributedLockService;

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

    @Override
    public Result<QbQuestion> detail(Long id) {
        QbQuestion question = qbQuestionRepository.findByIdAndNotDeleted(id);
        if (question == null) {
            return Result.paramError("试题不存在");
        }
        return Result.success(question);
    }

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

        // 校验题型与答案匹配性
        Result<Void> validateResult = validateQuestionWithAnswer(question, extraData);
        if (validateResult != null) {
            return validateResult;
        }

        // 设置创建信息
        question.setCreateUser(userId);
        question.setCreateTime(new Date());
        question.setUpdateTime(new Date());
        question.setIsDelete((byte) 0);

        // 保存试题
        qbQuestionRepository.save(question);

        // 保存题型特有信息
        saveQuestionExtraData(question, extraData);

        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> update(Long id, QbQuestion question, Object extraData, Long userId) {
        QbQuestion existing = qbQuestionRepository.findByIdAndNotDeleted(id);
        if (existing == null) {
            return Result.paramError("试题不存在");
        }

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

        // 校验题型与答案匹配性
        Result<Void> validateResult = validateQuestionWithAnswer(question, extraData);
        if (validateResult != null) {
            return validateResult;
        }

        // 更新基本信息
        existing.setQuestionTitle(question.getQuestionTitle());
        existing.setQuestionType(question.getQuestionType());
        existing.setDifficultyLevel(question.getDifficultyLevel());
        existing.setScore(question.getScore());
        existing.setCategoryId(question.getCategoryId());
        existing.setUpdateTime(new Date());

        // 保存试题
        qbQuestionRepository.save(existing);

        // 保存题型特有信息
        saveQuestionExtraData(existing, extraData);

        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        QbQuestion question = qbQuestionRepository.findByIdAndNotDeleted(id);
        if (question == null) {
            return Result.paramError("试题不存在");
        }

        question.setIsDelete((byte) 1);
        question.setUpdateTime(new Date());
        qbQuestionRepository.save(question);

        return Result.success();
    }

    // Day 16 新增：批量导入试题
    @Override
    @Transactional
    public Result<String> batchImport(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            return Result.paramError("导入文件不能为空");
        }
        String batchId = userId + ":" + file.getOriginalFilename() + ":" + file.getSize();
        return distributedLockService.execute(
                CacheKeys.questionImportLock(batchId),
                () -> doBatchImport(file, userId));
    }

    private Result<String> doBatchImport(MultipartFile file, Long userId) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<String> errorMessages = new ArrayList<>();

            // 从第二行开始读取数据（第一行为标题行）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 解析Excel行数据
                    QbQuestion question = parseQuestionFromRow(row, i + 1); // i+1 是行号

                    // 校验重复题干
                    List<QbQuestion> duplicates = qbQuestionRepository.findByQuestionTitleAndCategoryId(
                            question.getQuestionTitle(), question.getCategoryId());
                    if (!duplicates.isEmpty()) {
                        errorMessages.add("第" + (i + 1) + "行：题干\"" + question.getQuestionTitle() + "\"已存在");
                        continue;
                    }



                    // 参数校验
                    if (question.getScore() == null || BigDecimal.valueOf(question.getScore()).compareTo(BigDecimal.valueOf(0)) <= 0) {
                        errorMessages.add("第" + (i + 1) + "行：分值必须大于0");
                        continue;
                    }


                    if (question.getQuestionType() == null ||
                            (question.getQuestionType() != 1 && question.getQuestionType() != 2 &&
                                    question.getQuestionType() != 3 && question.getQuestionType() != 4)) {
                        errorMessages.add("第" + (i + 1) + "行：题型不合法（1-单选，2-多选，3-判断，4-填空）");
                        continue;
                    }

                    // 设置创建信息
                    question.setCreateUser(userId);
                    question.setCreateTime(new Date());
                    question.setUpdateTime(new Date());
                    question.setIsDelete((byte) 0);

                    // 保存试题
                    qbQuestionRepository.save(question);

                } catch (Exception e) {
                    errorMessages.add("第" + (i + 1) + "行：解析数据错误 - " + e.getMessage());
                }
            }

            workbook.close();

            if (!errorMessages.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String msg : errorMessages) {
                    sb.append(msg).append("\n");
                }
                return Result.businessBlock("导入完成，但存在以下错误：\n" + sb.toString());
            }

            return Result.success("导入成功");
        } catch (IOException e) {
            return Result.businessBlock("文件读取失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.businessBlock("导入过程中发生错误：" + e.getMessage());
        }
    }

    // Day 16 新增：批量导出试题
    @Override
    public void batchExport(List<Long> ids, HttpServletResponse response) {
        try {
            // 查询指定ID的试题
            List<QbQuestion> questions = qbQuestionRepository.findByIdInAndIsDelete(ids, (byte) 0);

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("试题导出");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"题干", "题型", "难度", "分值", "分类ID", "A选项", "B选项", "C选项", "D选项", "标准答案", "解析"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据行
            int rowNum = 1;
            for (QbQuestion question : questions) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(question.getQuestionTitle());
                row.createCell(1).setCellValue(getQuestionTypeName(question.getQuestionType()));
                row.createCell(2).setCellValue(question.getDifficultyLevel());
                row.createCell(3).setCellValue(question.getScore().doubleValue());
                row.createCell(4).setCellValue(question.getCategoryId());

                // 根据题型获取选项和答案
                String[] options = getOptionsByQuestionId(question.getId());
                String answer = getAnswerByQuestionId(question.getId());

                for (int j = 0; j < Math.min(options.length, 4); j++) {
                    row.createCell(5 + j).setCellValue(options[j]);
                }

                row.createCell(9).setCellValue(answer);
                row.createCell(10).setCellValue(question.getAnalysis() != null ? question.getAnalysis() : "");
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=questions_export.xlsx");

            // 写入响应
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            // 记录错误，但不抛出异常，避免中断响应
            e.printStackTrace();
        }
    }

    // Day 16 新增：批量迁移试题分类
    @Override
    @Transactional
    public Result<String> batchTransferCategory(List<Long> ids, Long targetCategoryId, Long userId) {
        // 校验目标分类是否存在
        QbCategory targetCategory = qbCategoryRepository.findById(targetCategoryId).orElse(null);
        if (targetCategory == null) {
            return Result.paramError("目标分类不存在");
        }

        // 查询待迁移的试题
        List<QbQuestion> questions = qbQuestionRepository.findByIdInAndIsDelete(ids, (byte) 0);

        if (questions.isEmpty()) {
            return Result.paramError("没有找到需要迁移的试题");
        }

        // 批量更新分类
        for (QbQuestion question : questions) {
            question.setCategoryId(targetCategoryId);
            question.setUpdateTime(new Date());
        }

        qbQuestionRepository.saveAll(questions);

        return Result.success();
    }
    // 实现缺失的cleanOldExtraData方法
    @Override
    public void cleanOldExtraData(Long questionId, Byte oldQuestionType) {
        // 根据题型删除对应的额外数据
        if (oldQuestionType != null) {
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
    // 辅助方法：解析Excel行数据
    private QbQuestion parseQuestionFromRow(Row row, int lineNumber) throws Exception {
        QbQuestion question = new QbQuestion();

        // 题干
        String questionTitle = getCellValueAsString(row.getCell(0));
        if (questionTitle == null || questionTitle.trim().isEmpty()) {
            throw new Exception("题干不能为空");
        }
        question.setQuestionTitle(questionTitle.trim());

        // 题型
        String questionTypeStr = getCellValueAsString(row.getCell(1));
        if (questionTypeStr == null || questionTypeStr.trim().isEmpty()) {
            throw new Exception("题型不能为空");
        }
        try {
            question.setQuestionType(Byte.valueOf(questionTypeStr.trim()));
        } catch (NumberFormatException e) {
            throw new Exception("题型格式不正确");
        }

        // 难度
        String difficultyLevel = getCellValueAsString(row.getCell(2));
        if (difficultyLevel == null || difficultyLevel.trim().isEmpty()) {
            difficultyLevel = "1"; // 默认简单
        }
        try {
            question.setDifficultyLevel(Byte.valueOf(difficultyLevel.trim()));
        } catch (NumberFormatException e) {
            throw new Exception("难度等级格式不正确");
        }

        // 分值
        String scoreStr = getCellValueAsString(row.getCell(3));
        if (scoreStr == null || scoreStr.trim().isEmpty()) {
            throw new Exception("分值不能为空");
        }
        try {
            Double scoreValue = Double.valueOf(scoreStr.trim());
            question.setScore(scoreValue);
        } catch (NumberFormatException e) {
            throw new Exception("分值格式不正确");
        }

        // 分类ID
        String categoryIdStr = getCellValueAsString(row.getCell(4));
        if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
            throw new Exception("分类ID不能为空");
        }
        try {
            question.setCategoryId(Long.valueOf(categoryIdStr.trim()));
        } catch (NumberFormatException e) {
            throw new Exception("分类ID格式不正确");
        }

        // 解析选项
        String optionA = getCellValueAsString(row.getCell(5));
        String optionB = getCellValueAsString(row.getCell(6));
        String optionC = getCellValueAsString(row.getCell(7));
        String optionD = getCellValueAsString(row.getCell(8));

        // 答案
        String answer = getCellValueAsString(row.getCell(9));

        // 解析
        String analysis = getCellValueAsString(row.getCell(10));
        if (analysis != null) {

        }

        return question;
    }

    // 辅助方法：获取单元格字符串值
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0*$", "");
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    // 辅助方法：获取题型名称
    private String getQuestionTypeName(Byte type) {
        switch (type) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            default: return "未知题型";
        }
    }

    // 辅助方法：根据试题ID获取选项
    private String[] getOptionsByQuestionId(Long questionId) {
        // 根据题型返回相应选项
        QbQuestion question = qbQuestionRepository.findByIdAndNotDeleted(questionId);
        if (question != null) {
            // 这里需要根据具体的题型来获取选项，这里简化处理
            // 在实际项目中，需要根据questionType去对应的表查询选项
            return new String[]{"", "", "", ""}; // 返回4个选项，实际值需要从对应题型表获取
        }
        return new String[]{"", "", "", ""};
    }

    // 辅助方法：根据试题ID获取答案
    private String getAnswerByQuestionId(Long questionId) {
        // 根据题型返回相应答案
        // 在实际项目中，需要根据questionType去对应的表查询答案
        return "";
    }

    private Result<Void> validateQuestionWithAnswer(QbQuestion question, Object extraData) {
        // 实现题型与答案的校验逻辑
        return null; // 简化实现
    }

    private void saveQuestionExtraData(QbQuestion question, Object extraData) {
        // 实现保存题型特有信息的逻辑
    }
}