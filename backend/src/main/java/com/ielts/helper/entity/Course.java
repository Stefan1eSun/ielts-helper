package com.ielts.helper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("courses")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long courseId;
    
    private String title;
    
    private String type;
    
    private Long teacherId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private BigDecimal price;
    
    private String description;
    
    private Integer isOpen;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
