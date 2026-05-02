package com.ielts.helper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ielts.helper.entity.Messages;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Messages> {
}