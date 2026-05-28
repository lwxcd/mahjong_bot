package org.bot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bot.mapper.EloMapper;
import org.bot.model.domain.Elo;
import org.bot.model.type.ContestType;
import org.bot.service.EloService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @author li hanyu
* @description 针对表【elo(elo表)】的数据库操作Service实现
* @createDate 2025-06-03 23:37:22
*/
@Service
public class EloServiceImpl extends ServiceImpl<EloMapper, Elo>
    implements EloService{

    private Elo createDefaultElo(Integer userId, ContestType type, Long groupId) {
        Elo elo = new Elo();
        elo.setUserId(userId);
        elo.setGroupId(groupId);
        elo.setElo(BigDecimal.ZERO);
        elo.setType(type.getParent());
        this.save(elo);
        return elo;
    }

    @Override
    public BigDecimal getElo(Integer userId, ContestType type, Long groupId) {

        Elo elo = this.getOne(new QueryWrapper<Elo>().eq("user_id", userId).eq("group_id", groupId));
        if(elo == null){
            elo = createDefaultElo(userId, type, groupId);
        }
        return elo.getElo();
    }

    @Override
    public List<Elo> updateElo(Map<Integer, BigDecimal> map, ContestType type, Long groupId) {
        List<Elo> ret = new  ArrayList<>();

        for (Map.Entry<Integer, BigDecimal> entry : map.entrySet()) {
            Elo elo = this.getOne(new QueryWrapper<Elo>().eq("user_id", entry.getKey()).eq("group_id", groupId));
            elo.setElo(entry.getValue());
            this.updateById(elo);

            ret.add(elo);
        }
        return ret;
    }

    @Override
    public List<Elo> queryUserElo(Integer userId) {
        LambdaQueryWrapper<Elo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Elo::getUserId, userId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Elo> getRankingByType(ContestType type, Long groupId, int limit) {
        LambdaQueryWrapper<Elo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Elo::getType, type.getParent());
        queryWrapper.eq(Elo::getGroupId, groupId);
        queryWrapper.orderByDesc(Elo::getElo);
        queryWrapper.last("limit " + limit);
        return this.list(queryWrapper);
    }
}




