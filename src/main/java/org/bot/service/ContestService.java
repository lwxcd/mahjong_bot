package org.bot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bot.model.domain.Contest;
import org.bot.model.type.ContestStatus;
import org.bot.model.type.ContestType;

import java.util.List;

public interface ContestService extends IService<Contest> {

    Contest createContest(Long groupId, ContestType type);

    List<Contest> queryLastContest4Group(Long groupId, Integer page, Integer size);

    boolean updateContestStatus(Integer contestId, ContestStatus contestStatus);
}
