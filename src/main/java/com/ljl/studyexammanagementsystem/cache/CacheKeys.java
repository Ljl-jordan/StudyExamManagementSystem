package com.ljl.studyexammanagementsystem.cache;

public final class CacheKeys {

    private static final String ROOT = "exam";

    private CacheKeys() {
    }

    public static String materialDetail(Long materialId) {
        return ROOT + ":kb:material:detail:" + materialId;
    }

    public static String materialLock(Long materialId) {
        return ROOT + ":kb:material:lock:" + materialId;
    }

    public static String categoryTree(Long userId, Long orgId) {
        return categoryTreePrefix() + userId + ':' + orgId;
    }

    public static String categoryTreePrefix() {
        return ROOT + ":kb:category:tree:";
    }

    public static String taskDetail(Long taskId) {
        return ROOT + ":task:detail:" + taskId;
    }

    public static String paperDetail(Long paperId) {
        return ROOT + ":paper:detail:" + paperId;
    }

    public static String paperPublishLock(Long paperId) {
        return ROOT + ":paper:publish:lock:" + paperId;
    }

    public static String answerSubmitLock(Long paperId, Long userId) {
        return ROOT + ":answer:submit:lock:" + paperId + ":" + userId;
    }

    public static String questionImportLock(String batchId) {
        return ROOT + ":question:import:lock:" + batchId;
    }
}