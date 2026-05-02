package com.ielts.helper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ielts.helper.entity.Messages;
import com.ielts.helper.mapper.MessageMapper;
import com.ielts.helper.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Messages> implements MessageService {

    @Override
    public List<Messages> getConversationMessages(Long conversationId) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getConversationId, conversationId)
               .orderByAsc(Messages::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Messages saveMessage(Long conversationId, String role, String content) {
        Messages message = new Messages();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        save(message);
        return message;
    }

    @Override
    public void deleteConversationMessages(Long conversationId) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getConversationId, conversationId);
        // 使用 baseMapper.delete 进行物理删除，绕过逻辑删除配置
        baseMapper.delete(wrapper);
    }

}