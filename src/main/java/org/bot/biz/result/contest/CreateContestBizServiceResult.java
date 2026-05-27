package org.bot.biz.result.contest;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bot.biz.base.BizServiceBaseResult;
import org.bot.model.domain.Contest;
import org.bot.model.domain.ContestRecord;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateContestBizServiceResult extends BizServiceBaseResult {

    private Contest contest;

    private List<ContestRecord> records;
}
