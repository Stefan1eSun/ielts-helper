package com.ielts.helper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("institutions")
public class Institution {
    @TableId(type = IdType.AUTO)
    private Long institutionId;
    
    private String name;
    
    private String introduction;
    
    private String contactPhone;
    
    private String address;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
