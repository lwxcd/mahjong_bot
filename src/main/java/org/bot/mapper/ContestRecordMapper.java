package org.bot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.bot.model.domain.ContestRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.bot.model.vo.UserRecordVO;

import java.util.List;

@Mapper
public interface ContestRecordMapper extends BaseMapper<ContestRecord> {

    @Select("select count(*) from contest_record where contest_id = #{contestId}")
    Integer getContestRecordCount(Integer contestId);

    List<UserRecordVO> getUserRecord(Integer userId, Integer limit);
}
