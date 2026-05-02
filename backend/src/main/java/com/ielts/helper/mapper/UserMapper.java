package com.ielts.helper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ielts.helper.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
