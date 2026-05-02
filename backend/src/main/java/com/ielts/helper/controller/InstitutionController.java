package com.ielts.helper.controller;

import com.ielts.helper.common.Result;
import com.ielts.helper.entity.Institution;
import com.ielts.helper.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InstitutionController {
    
    @Autowired
    private InstitutionService institutionService;
    
    @GetMapping("/institution")
    public Result<Institution> getInstitutionInfo() {
        Institution institution = institutionService.getInstitutionInfo();
        return Result.success(institution);
    }
}
