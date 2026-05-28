package org.bot.service.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import org.bot.mapper.ContestEndMapper;
import org.bot.model.domain.Contest;
import org.bot.model.domain.ContestEnd;
import org.bot.model.domain.ContestRecord;
import org.bot.model.domain.Elo;
import org.bot.model.domain.User;
import org.bot.model.type.ContestStatus;
import org.bot.model.type.ContestType;
import org.bot.model.type.DirectionType;
import org.bot.service.ContestEndService;
import org.bot.service.ContestService;
import org.bot.service.EloService;
import org.bot.service.UserService;
import org.bot.util.SignSendMessageComponent;
import org.bot.util.elo.EloCalculate;
import org.bot.util.elo.EloCalculateContext;
import org.bot.util.point.RuleCalculate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ContestEndServiceImpl extends ServiceImpl<ContestEndMapper, ContestEnd>
        implements ContestEndService {

    @Autowired
    private ContestService contestService;

    @Autowired
    private SignSendMessageComponent botContainer;

    @Autowired
    private EloService eloService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    @Async
    public void calculateScore(Integer contestId, List<ContestRecord> recordList) {
        Contest contest = contestService.getById(contestId);

        Map<DirectionType, Integer> context = new HashMap<>(recordList.size());
        recordList.forEach(record -> context.put(record.getDirection(), record.getPoint()));

        Map<DirectionType, BigDecimal> calculate = RuleCalculate.calculate(contest.getType(), context);

        calculate.forEach((direction, point) -> {
            ContestEnd contestEnd = new ContestEnd();
            contestEnd.setContestId(contestId);
            for (ContestRecord record : recordList) {
                if (record.getDirection() == direction) {
                    contestEnd.setUserId(record.getRecordUserId());
                }
            }
            contestEnd.setEndPoint(point);
            this.save(contestEnd);
        });

        contest.setStatus(ContestStatus.END);
        contestService.updateById(contest);

        Map<Integer, Pair<BigDecimal, BigDecimal>> calculateElo = calculateElo(contestId, contest.getType(), contest.getCreateGroupId());
        updateChangeElo(calculateElo, contest.getId());

        Bot bot = botContainer.getBot();

        MsgUtils msg = MsgUtils.builder();
        msg.text("🏆 比赛结束！以下是比赛结果：\n");
        msg.text("------------------------------\n");
        msg.text("比赛类型：" + contest.getType().getDescription() + "\n");
        msg.text("比赛ID：" + contest.getId() + "\n");
        msg.text("------------------------------\n");

        calculateElo.forEach((userId, elo) -> {
            BigDecimal oldElo = elo.getKey();
            BigDecimal newElo = elo.getValue();
            BigDecimal change = newElo.subtract(oldElo);
            String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";

            ContestRecord contestRecord = null;
            for (ContestRecord record : recordList) {
                if (Objects.equals(record.getRecordUserId(), userId)) {
                    contestRecord = record;
                    break;
                }
            }

            User user = userService.getById(userId);
            msg.text("👤 " + user.getNickname() + "\n");
            msg.text("📈 点数情况 " + (contestRecord.getPoint() >= 0 ? "+" : "") + contestRecord.getPoint() + "\n");
            msg.text("📈 ELO: " + oldElo + " → " + newElo + " (" + sign + change.toPlainString() + ")\n");
            msg.text("------------------------------\n");
        });

        bot.sendGroupMsg(contest.getCreateGroupId(), msg.build(), false);
    }

    private void updateChangeElo(Map<Integer, Pair<BigDecimal, BigDecimal>> calculateElo, Integer contestID) {
        calculateElo.forEach((userId, pair) -> {
            ContestEnd contestEnd = this.query().eq("contest_id", contestID).eq("user_id", userId).one();
            contestEnd.setEloChange(pair.getValue().subtract(pair.getKey()));
            this.updateById(contestEnd);
        });
    }

    public Map<Integer, Pair<BigDecimal, BigDecimal>> calculateElo(Integer contestId, ContestType contestType, Long groupId) {
        List<ContestEnd> contestEndList = this.list(new QueryWrapper<ContestEnd>().eq("contest_id", contestId));

        EloCalculateContext context = new EloCalculateContext();

        Map<Integer, BigDecimal> score = new HashMap<>();
        contestEndList.forEach(contestEnd -> score.put(contestEnd.getUserId(), contestEnd.getEndPoint()));
        context.setScores(score);

        Map<Integer, BigDecimal> originalElo = new HashMap<>();
        for (ContestEnd contestEnd : contestEndList) {
            BigDecimal elo = eloService.getElo(contestEnd.getUserId(), contestType, groupId);
            originalElo.put(contestEnd.getUserId(), elo);
        }
        context.setOriginalElo(originalElo);

        Map<Integer, BigDecimal> eloChange = EloCalculate.calculate(contestType, context);
        List<Elo> changeElo = eloService.updateElo(eloChange, contestType, groupId);

        Map<Integer, Pair<BigDecimal, BigDecimal>> ret = new HashMap<>();
        for (Elo elo : changeElo) {
            ret.put(elo.getUserId(), Pair.of(originalElo.get(elo.getUserId()), elo.getElo()));
        }
        return ret;
    }
}
