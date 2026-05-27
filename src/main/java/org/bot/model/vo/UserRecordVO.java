package org.bot.model.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.bot.model.type.ContestStatus;
import org.bot.model.type.ContestType;
import org.bot.model.type.DirectionType;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserRecordVO {

    private Integer contestId;
    /**
     * 类型
     */
    private ContestType type;

    /**
     * 群号
     */
    private Long groupId;

    /**
     * direction
     */
    private DirectionType direction;

    /**
     * 点数
     */
    private Integer point;

    /**
     * 结算点数
     */
    private BigDecimal endPoint;

    // 积分变化
    private BigDecimal eloChange;

    // 时间
    private Date time;
}
