package org.bot.biz.request.contest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bot.biz.base.BizServiceBaseRequest;
import org.bot.model.type.ContestType;
import org.bot.model.type.DirectionType;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateContestBizServiceRequest extends BizServiceBaseRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private ContestType contestType;

    @NotNull
    private List<PlayerRecord> records;

    @Data
    public static class PlayerRecord {
        private String nickname;
        private Integer score;
        private DirectionType direction;
    }
}
