package com.ielts.helper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("enrollments")
public class Enrollment {
    @TableId(type = IdType.AUTO)
    private Long enrollmentId;
    
    private Long userId;
    
    private Long courseId;
    
    private Integer status;
    
    private String orderId;
    
    private BigDecimal paidAmount;
    
    private LocalDateTime paidAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
