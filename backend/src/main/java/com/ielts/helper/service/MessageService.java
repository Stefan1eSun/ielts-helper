package com.ielts.helper.service;

import com.ielts.helper.entity.Messages;

import java.util.List;

public interface MessageService {
    List<Messages> getConversationMessages(Long conversationId);
    Messages saveMessage(Long conversationId, String role, String content);
    void deleteConversationMessages(Long conversationId);
}