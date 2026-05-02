package com.ielts.helper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ielts.helper.entity.Institution;
import com.ielts.helper.mapper.InstitutionMapper;
import com.ielts.helper.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstitutionServiceImpl implements InstitutionService {
    
    @Autowired
    private InstitutionMapper institutionMapper;
    
    @Override
    public Institution getInstitutionInfo() {
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        return institutionMapper.selectOne(wrapper);
    }
}
