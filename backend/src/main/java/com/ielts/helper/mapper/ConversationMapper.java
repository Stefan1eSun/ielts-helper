package com.ielts.helper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ielts.helper.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}