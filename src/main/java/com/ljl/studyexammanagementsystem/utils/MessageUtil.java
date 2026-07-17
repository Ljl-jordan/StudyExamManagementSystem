package com.ljl.studyexammanagementsystem.utils;

import com.ljl.studyexammanagementsystem.entity.SysMessage;
import com.ljl.studyexammanagementsystem.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MessageUtil {

    @Autowired
    private MessageRepository messageRepository;

    public void pushSingle(Long receiveUserId, String businessType, Long businessId, String title, String content) {
        SysMessage msg = new SysMessage();
        msg.setReceiveUserId(receiveUserId);
        msg.setBusinessType(businessType);
        msg.setBusinessId(businessId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setReadFlag((byte) 0);
        msg.setCreateTime(new Date());
        msg.setIsDelete((byte) 0);
        messageRepository.save(msg);
    }

    public void pushBatch(List<Long> userIds, String businessType, Long businessId, String title, String content) {
        for (Long userId : userIds) {
            pushSingle(userId, businessType, businessId, title, content);
        }
    }
}
