package org.bot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bot.model.domain.Elo;
import org.bot.model.type.ContestType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* @author li hanyu
* @description 针对表【elo(elo表)】的数据库操作Service
* @createDate 2025-06-03 23:37:22
*/
public interface EloService extends IService<Elo> {

    BigDecimal getElo(Integer userId, ContestType type, Long groupId);

    List<Elo> updateElo(Map<Integer, BigDecimal> map, ContestType type, Long groupId);

    List<Elo> queryUserElo(Integer userId);

    List<Elo> getRankingByType(ContestType type, Long groupId, int limit);
}
