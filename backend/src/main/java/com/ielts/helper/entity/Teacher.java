package com.ielts.helper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teachers")
public class Teacher {
    @TableId(type = IdType.AUTO)
    private Long teacherId;
    
    private String name;
    
    private String qualification;
    
    private String teachingStyle;
    
    private String bio;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
