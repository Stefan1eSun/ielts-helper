package com.ielts.helper.service;

import com.ielts.helper.common.Result;
import com.ielts.helper.entity.Conversation;

import java.util.List;

public interface ConversationService {
    List<Conversation> getUserConversations(Long userId);
    Conversation createConversation(Long userId, String title);
    void deleteConversation(Long conversationId, Long userId);
    Conversation getConversationById(Long conversationId);
    Result updateConversationTitle(Long conversationId, Long userId, String newTitle);
}