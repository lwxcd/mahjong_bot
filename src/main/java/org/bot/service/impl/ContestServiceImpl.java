package org.bot.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bot.mapper.ContestMapper;
import org.bot.model.domain.Contest;
import org.bot.model.type.ContestStatus;
import org.bot.model.type.ContestType;
import org.bot.service.ContestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest>
        implements ContestService {

    @Override
    public Contest createContest(Long groupId, ContestType type) {
        Contest contest = new Contest();
        contest.setCreateGroupId(groupId);
        contest.setType(type);
        contest.setStatus(ContestStatus.NOT_START);
        this.save(contest);
        return contest;
    }

    @Override
    public List<Contest> queryLastContest4Group(Long groupId, Integer page, Integer size) {
        Page<Contest> contestPage = new Page<>(page, size);
        LambdaQueryWrapper<Contest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Contest::getCreateGroupId, groupId);
        queryWrapper.orderByDesc(Contest::getCreateTime);
        return this.page(contestPage, queryWrapper).getRecords();
    }

    @Transactional
    @Override
    public boolean updateContestStatus(Integer contestId, ContestStatus contestStatus) {
        Contest contest = this.getById(contestId);
        Assert.notNull(contest, "比赛不存在");
        contest.setStatus(contestStatus);
        return this.updateById(contest);
    }
}
