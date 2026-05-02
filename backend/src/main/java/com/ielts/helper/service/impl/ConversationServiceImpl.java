package com.ielts.helper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ielts.helper.common.Result;
import com.ielts.helper.entity.Conversation;
import com.ielts.helper.mapper.ConversationMapper;
import com.ielts.helper.service.ConversationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Override
    public List<Conversation> getUserConversations(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId)
               .orderByDesc(Conversation::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Conversation createConversation(Long userId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setCreateTime(LocalDateTime.now());
        save(conversation);
        return conversation;
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getId, conversationId)
               .eq(Conversation::getUserId, userId);
        // 使用 baseMapper.delete 进行物理删除，绕过逻辑删除配置
        baseMapper.delete(wrapper);
    }

    @Override
    public Conversation getConversationById(Long conversationId) {
        return getById(conversationId);
    }

    @Override
    public Result updateConversationTitle(Long conversationId, Long userId, String newTitle) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getId, conversationId)
               .eq(Conversation::getUserId, userId);
        Conversation conversation = new Conversation();
        conversation.setTitle(newTitle);
        update(conversation, wrapper);
        return Result.success("更新成功");
    }
}