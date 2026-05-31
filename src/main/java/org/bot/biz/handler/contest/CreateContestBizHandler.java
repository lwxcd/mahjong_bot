package org.bot.biz.handler.contest;

import org.bot.biz.BizServiceException;
import org.bot.biz.BizServiceHandleInterface;
import org.bot.biz.base.AbstractBizServiceHandler;
import org.bot.biz.base.BizServiceTypeEnum;
import org.bot.biz.request.contest.CreateContestBizServiceRequest;
import org.bot.biz.result.contest.CreateContestBizServiceResult;
import org.bot.model.domain.Contest;
import org.bot.model.domain.ContestRecord;
import org.bot.model.domain.User;
import org.bot.model.type.ContestStatus;
import org.bot.model.type.ContestType;
import org.bot.util.point.impl.BaseRCRCalculateRule;
import org.bot.util.point.impl.EmptyCalculateServiceImpl;
import org.bot.util.point.impl.MleagueRuleCalculateServiceImpl;

import java.util.ArrayList;
import java.util.List;

@BizServiceHandleInterface(type = BizServiceTypeEnum.CREATE_CONTEST)
public class CreateContestBizHandler
        extends AbstractBizServiceHandler<CreateContestBizServiceRequest, CreateContestBizServiceResult> {

    @Override
    public CreateContestBizServiceResult handle(CreateContestBizServiceRequest request) {

        // 验证总分
        validateTotalScore(request);

        Contest contest = new Contest();
        contest.setCreateGroupId(request.getGroupId());
        contest.setType(request.getContestType());
        contest.setStatus(ContestStatus.START);
        contestService.save(contest);

        List<ContestRecord> recordList = new ArrayList<>();
        for (CreateContestBizServiceRequest.PlayerRecord pr : request.getRecords()) {
            User user = userService.queryOrCreateByNickname(pr.getNickname(), request.getGroupId());

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

    /**
     * 验证四家总分是否符合当前规则的要求
     */
    private void validateTotalScore(CreateContestBizServiceRequest request) {
        ContestType type = request.getContestType();
        int total = request.getRecords().stream().mapToInt(CreateContestBizServiceRequest.PlayerRecord::getScore).sum();

        // 获取实际计算服务类
        Class<?> calcClass = type.getCalculateServiceClass();
        if (calcClass == null && type.getParent() != null) {
            calcClass = type.getParent().getCalculateServiceClass();
        }

        if (calcClass == null || calcClass == EmptyCalculateServiceImpl.class) {
            // 无规则或透传规则，不校验总分
            return;
        }

        if (calcClass == BaseRCRCalculateRule.class) {
            if (total != 100000) {
                throw new BizServiceException(null,
                        "总分校验失败：RCR/A规则要求四家分数之和为 100000，当前总和为 " + total + "（相差 " + (total - 100000) + "）");
            }
        } else if (calcClass == MleagueRuleCalculateServiceImpl.class) {
            if (total > 100000) {
                throw new BizServiceException(null,
                        "总分校验失败：M规则要求四家分数之和不超过 100000，当前总和为 " + total + "（超出 " + (total - 100000) + "）");
            }
        }
    }
}
