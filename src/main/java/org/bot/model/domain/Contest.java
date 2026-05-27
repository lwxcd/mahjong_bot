package org.bot.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.bot.model.type.ContestStatus;
import org.bot.model.type.ContestType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@TableName(value = "contest")
@Data
public class Contest implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "type")
    private ContestType type;

    @TableField(value = "status")
    private ContestStatus status;

    @TableField(value = "create_group_id")
    private Long createGroupId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
