package com.ljl.studyexammanagementsystem.configure;

import com.ljl.studyexammanagementsystem.entity.*;
import com.ljl.studyexammanagementsystem.repository.*;
import com.ljl.studyexammanagementsystem.service.ExamAnswerService;
import com.ljl.studyexammanagementsystem.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTask.class);

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private ExamAnswerService examAnswerService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private OperateLogRepository operateLogRepository;

    @Autowired
    private LearnTaskRepository learnTaskRepository;

    @Autowired
    private LearnTaskUserRepository learnTaskUserRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Autowired
    private MessageService messageService;

    /**
     * 检查定时任务是否开启的辅助方法
     */
    private boolean isTaskEnabled(String taskConfigKey) {
        try {
            SysConfig config = sysConfigRepository.findByConfigKeyAndIsDelete(taskConfigKey, (byte) 0).orElse(null);
            return config == null || "true".equalsIgnoreCase(config.getConfigValue());
        } catch (Exception e) {
            log.warn("检查定时任务开关失败，使用默认值(true)：" + taskConfigKey, e);
            return true; // 默认开启
        }
    }

    /**
     * 每小时执行一次：检查锁定超过24小时的账号，自动解锁
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void autoUnlockExpiredAccounts() {
        if (!isTaskEnabled("schedule.task.account.unlock")) {
            log.debug("账号自动解锁定时任务已关闭");
            return;
        }

        log.info("开始执行账号自动解锁定时任务");
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -24);
            Date threshold = cal.getTime();

            List<SysUser> lockedUsers = sysUserRepository.findAll((root, query, criteriaBuilder) -> {
                return criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("userStatus"), (byte) 2), // 2-锁定状态
                        criteriaBuilder.lessThan(root.get("lockTime"), threshold),
                        criteriaBuilder.equal(root.get("isDelete"), (byte) 0)
                );
            });
            int count = 0;
            for (SysUser user : lockedUsers) {
                if (user.getLockTime() != null && user.getLockTime().before(threshold) && user.getUserStatus() == 2) {
                    user.setUserStatus((byte) 0); // 0-正常
                    user.setLockTime(null); // 清除锁定时间
                    user.setUpdateTime(new Date());
                    sysUserRepository.save(user);
                    count++;
                    log.info("账号[{}]锁定超过24小时，已自动解锁", user.getLoginAccount());
                }
            }
            if (count > 0) {
                log.info("本次定时任务共自动解锁{}个账号", count);
            } else {
                log.debug("本次定时任务无需要解锁的账号");
            }
        } catch (Exception e) {
            log.error("账号自动解锁定时任务执行异常", e);
        }
    }

    /**
     * 每5分钟执行一次：自动交卷超时的试卷
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void autoSubmitExpiredPapers() {
        if (!isTaskEnabled("schedule.task.paper.autosubmit")) {
            log.debug("自动交卷定时任务已关闭");
            return;
        }

        log.info("开始执行自动交卷定时任务");
        try {
            examAnswerService.autoSubmitExpiredPapers();
            log.info("自动交卷定时任务执行完成");
        } catch (Exception e) {
            log.error("自动交卷定时任务执行异常", e);
        }
    }

    /**
     * 每10分钟执行一次：自动判分客观题
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void autoGradeObjectiveQuestions() {
        if (!isTaskEnabled("schedule.task.question.autograde")) {
            log.debug("客观题自动判分定时任务已关闭");
            return;
        }

        log.info("开始执行客观题自动判分定时任务");
        try {
            examAnswerService.autoGradeObjectiveQuestions();
            log.info("客观题自动判分定时任务执行完成");
        } catch (Exception e) {
            log.error("客观题自动判分定时任务执行异常", e);
        }
    }

    /**
     * 每天凌晨2点执行：清理90天前的过期消息
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void cleanupExpiredMessages() {
        if (!isTaskEnabled("schedule.task.message.cleanup")) {
            log.debug("过期消息清理定时任务已关闭");
            return;
        }

        log.info("开始执行过期消息清理定时任务");
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -90); // 90天前
            Date threshold = cal.getTime();

            List<SysMessage> allMessages = messageRepository.findAll();
            List<SysMessage> expiredMessages = new ArrayList<>();
            for (SysMessage message : allMessages) {
                if (message.getCreateTime() != null && message.getCreateTime().before(threshold) && message.getIsDelete() == 0) {
                    expiredMessages.add(message);
                }
            }

            int count = 0;
            for (SysMessage message : expiredMessages) {
                message.setIsDelete((byte) 1); // 软删除
                messageRepository.save(message);
                count++;
            }

            if (count > 0) {
                log.info("本次定时任务共清理{}条过期消息", count);
            } else {
                log.debug("本次定时任务无需要清理的过期消息");
            }
        } catch (Exception e) {
            log.error("过期消息清理定时任务执行异常", e);
        }
    }

    /**
     * 每天凌晨3点执行：归档180天前的日志数据
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点
    public void archiveOldLogs() {
        if (!isTaskEnabled("schedule.task.log.archive")) {
            log.debug("日志归档定时任务已关闭");
            return;
        }

        log.info("开始执行日志归档定时任务");
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -180); // 180天前
            Date threshold = cal.getTime();

            // 归档登录日志
            List<LoginLog> allLoginLogs = loginLogRepository.findAll();
            List<LoginLog> expiredLoginLogs = new ArrayList<>();
            for (LoginLog logEntry : allLoginLogs) {
                if (logEntry.getLoginTime() != null && logEntry.getLoginTime().before(threshold) && logEntry.getIsDelete() == 0) {
                    expiredLoginLogs.add(logEntry);
                }
            }

            int loginLogCount = 0;
            for (LoginLog logEntry : expiredLoginLogs) {
                logEntry.setIsDelete((byte) 1); // 软删除
                loginLogRepository.save(logEntry);
                loginLogCount++;
            }

            // 归档操作日志
            List<OperateLog> allOperateLogs = operateLogRepository.findAll();
            List<OperateLog> expiredOperateLogs = new ArrayList<>();
            for (OperateLog logEntry : allOperateLogs) {
                if (logEntry.getOperTime() != null && logEntry.getOperTime().before(threshold) && logEntry.getIsDelete() == 0) {
                    expiredOperateLogs.add(logEntry);
                }
            }

            int operateLogCount = 0;
            for (OperateLog logEntry : expiredOperateLogs) {
                logEntry.setIsDelete((byte) 1); // 软删除
                operateLogRepository.save(logEntry);
                operateLogCount++;
            }

            if (loginLogCount > 0 || operateLogCount > 0) {
                log.info("本次定时任务共归档{}条登录日志和{}条操作日志", loginLogCount, operateLogCount);
            } else {
                log.debug("本次定时任务无需要归档的日志数据");
            }
        } catch (Exception e) {
            log.error("日志归档定时任务执行异常", e);
        }
    }

    /**
     * 每天上午9点执行：发送学习任务截止前提醒
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天上午9点
    public void sendLearningReminders() {
        if (!isTaskEnabled("schedule.task.learning.reminder")) {
            log.debug("学习任务提醒定时任务已关闭");
            return;
        }

        log.info("开始执行学习任务截止前提醒定时任务");
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 3); // 3天后
            Date threeDaysLater = cal.getTime();

            // 查找3天内即将截止的学习任务
            List<LearnTask> upcomingTasks = learnTaskRepository.findAll((root, query, criteriaBuilder) -> {
                return criteriaBuilder.and(
                        criteriaBuilder.between(root.get("endTime"), new Date(), threeDaysLater),
                        criteriaBuilder.equal(root.get("taskStatus"), 1), // 1-已下发
                        criteriaBuilder.equal(root.get("isDelete"), 0)
                );
            });

            int reminderCount = 0;
            for (LearnTask task : upcomingTasks) {
                // 查找该任务中所有的用户分配记录
                List<LearnTaskUser> allTaskUsers = learnTaskUserRepository.findByTaskIdAndIsDelete(task.getId(), (byte) 0);

                // 过滤出未完成的用户（learnStatus != 2）
                List<LearnTaskUser> incompleteUsers = new ArrayList<>();
                for (LearnTaskUser taskUser : allTaskUsers) {
                    if (taskUser.getLearnStatus() != null && taskUser.getLearnStatus() != 2) { // 2-已完成
                        incompleteUsers.add(taskUser);
                    }
                }

                for (LearnTaskUser taskUser : incompleteUsers) {
                    // 发送提醒消息给未完成任务的用户
                    SysMessage reminderMessage = new SysMessage();
                    reminderMessage.setReceiveUserId(taskUser.getUserId());
                    reminderMessage.setBusinessType("learnTask");
                    reminderMessage.setBusinessId(task.getId());
                    reminderMessage.setTitle("学习任务提醒");
                    reminderMessage.setContent(String.format("您有一个学习任务《%s》将在3天内截止，请及时完成！", task.getTaskName()));
                    reminderMessage.setReadFlag((byte) 0); // 未读
                    reminderMessage.setCreateTime(new Date());
                    reminderMessage.setIsDelete((byte) 0);

                    messageRepository.save(reminderMessage);
                    reminderCount++;

                    log.debug("已向用户ID {} 发送学习任务提醒：{}", taskUser.getUserId(), task.getTaskName());
                }
            }

            if (reminderCount > 0) {
                log.info("本次定时任务共发送{}条学习任务截止前提醒", reminderCount);
            } else {
                log.debug("本次定时任务无需要提醒的用户");
            }
        } catch (Exception e) {
            log.error("学习任务提醒定时任务执行异常", e);
        }
    }
}