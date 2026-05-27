package org.bot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bot.mapper.ContestRecordMapper;
import org.bot.model.domain.Contest;
import org.bot.model.domain.ContestRecord;
import org.bot.model.type.DirectionType;
import org.bot.model.vo.UserRecordVO;
import org.bot.service.ContestEndService;
import org.bot.service.ContestRecordService;
import org.bot.service.ContestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContestRecordServiceImpl extends ServiceImpl<ContestRecordMapper, ContestRecord>
        implements ContestRecordService {

    @Autowired
    private ContestRecordMapper mapper;

    @Autowired
    private ContestService contestService;

    @Autowired
    private ContestEndService contestEndService;

    @Override
    public void calculateScore(Integer contestId) {
        boolean b = checkThreshold(contestId);
        if (!b) {
            return;
        }
        List<ContestRecord> recordList = this.list(new QueryWrapper<ContestRecord>().eq("contest_id", contestId));
        contestEndService.calculateScore(contestId, recordList);
    }

    private boolean checkThreshold(Integer contestId) {
        Integer recordCount = mapper.getContestRecordCount(contestId);
        Contest contest = contestService.getById(contestId);

        if (contest == null) {
            throw new RuntimeException("比赛不存在");
        }

        return recordCount >= contest.getType().getPlayNum();
    }

    @Override
    public List<UserRecordVO> getRecentRecord(Integer userId, int limit) {
        return mapper.getUserRecord(userId, limit);
    }

    @Override
    public ContestRecord queryRecordByCUD(Integer contestId, Integer userId, DirectionType direction) {
        LambdaQueryWrapper<ContestRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContestRecord::getContestId, contestId);
        wrapper.eq(ContestRecord::getRecordUserId, userId);
        wrapper.eq(ContestRecord::getDirection, direction);
        return this.getOne(wrapper);
    }
}
