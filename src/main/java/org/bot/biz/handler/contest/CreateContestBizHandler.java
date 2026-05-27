package org.bot.biz.handler.contest;

import org.bot.biz.BizServiceHandleInterface;
import org.bot.biz.base.AbstractBizServiceHandler;
import org.bot.biz.base.BizServiceTypeEnum;
import org.bot.biz.request.contest.CreateContestBizServiceRequest;
import org.bot.biz.result.contest.CreateContestBizServiceResult;
import org.bot.model.domain.Contest;
import org.bot.model.domain.ContestRecord;
import org.bot.model.domain.User;
import org.bot.model.type.ContestStatus;

import java.util.ArrayList;
import java.util.List;

@BizServiceHandleInterface(type = BizServiceTypeEnum.CREATE_CONTEST)
public class CreateContestBizHandler
        extends AbstractBizServiceHandler<CreateContestBizServiceRequest, CreateContestBizServiceResult> {

    @Override
    public CreateContestBizServiceResult handle(CreateContestBizServiceRequest request) {

        Contest contest = new Contest();
        contest.setCreateGroupId(request.getGroupId());
        contest.setType(request.getContestType());
        contest.setStatus(ContestStatus.START);
        contestService.save(contest);

        List<ContestRecord> recordList = new ArrayList<>();
        for (CreateContestBizServiceRequest.PlayerRecord pr : request.getRecords()) {
            User user = userService.queryOrCreateByNickname(pr.getNickname());

            ContestRecord record = new ContestRecord();
            record.setContestId(contest.getId());
            record.setDirection(pr.getDirection());
            record.setPoint(pr.getScore());
            record.setRecordUserId(user.getId());
            contestRecordService.save(record);
            recordList.add(record);
        }

        contestRecordService.calculateScore(contest.getId());

        CreateContestBizServiceResult result = new CreateContestBizServiceResult();
        result.setContest(contest);
        result.setRecords(recordList);
        return result;
    }
}
