package org.bot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bot.model.domain.ContestRecord;
import org.bot.model.type.DirectionType;
import org.bot.model.vo.UserRecordVO;

import java.util.List;

public interface ContestRecordService extends IService<ContestRecord> {

    void calculateScore(Integer contestId);

    List<UserRecordVO> getRecentRecord(Integer userId, int limit);

    ContestRecord queryRecordByCUD(Integer contestId, Integer userId, DirectionType direction);
}
