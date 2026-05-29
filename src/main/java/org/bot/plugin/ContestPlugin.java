package org.bot.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import lombok.extern.slf4j.Slf4j;
import org.bot.biz.BizServiceException;
import org.bot.biz.PluginBizServiceTemplate;
import org.bot.biz.base.BizServiceTypeEnum;
import org.bot.biz.base.ServiceCallback;
import org.bot.biz.request.contest.CreateContestBizServiceRequest;
import org.bot.biz.result.contest.CreateContestBizServiceResult;
import org.bot.model.domain.Contest;
import org.bot.model.domain.ContestEnd;
import org.bot.model.domain.ContestRecord;
import org.bot.model.domain.Elo;
import org.bot.model.domain.User;
import org.bot.model.type.ContestType;
import org.bot.model.type.DirectionType;
import org.bot.service.ContestEndService;
import org.bot.service.ContestRecordService;
import org.bot.service.ContestService;
import org.bot.service.EloService;
import org.bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Shiro
@Component
@Slf4j
public class ContestPlugin {

    @Autowired
    private ContestService contestService;

    @Autowired
    private ContestEndService contestEndService;

    @Autowired
    private ContestRecordService contestRecordService;

    @Autowired
    private EloService eloService;

    @Autowired
    private UserService userService;

    @Autowired
    private PluginBizServiceTemplate pluginBizServiceTemplate;

    private static final DirectionType[] DIRECTIONS = {
            DirectionType.EAST, DirectionType.SOUTH, DirectionType.WEST, DirectionType.NORTH
    };

    static List<CreateContestBizServiceRequest.PlayerRecord> parseRecords(String body) {
        List<String> lines = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (!line.trim().isEmpty()) {
                lines.add(line.trim());
            }
        }

        if (lines.size() < 5) {
            throw new BizServiceException(null, "格式错误：需要1行场况 + 4行玩家记录");
        }

        List<CreateContestBizServiceRequest.PlayerRecord> records = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String line = lines.get(i);
            int lastSpace = line.lastIndexOf(' ');
            if (lastSpace <= 0) {
                throw new BizServiceException(null, "格式错误：第" + (i + 1) + "行应为 '昵称 分数'");
            }
            String nickname = line.substring(0, lastSpace).trim();
            String scoreStr = line.substring(lastSpace + 1).trim();

            CreateContestBizServiceRequest.PlayerRecord pr = new CreateContestBizServiceRequest.PlayerRecord();
            pr.setNickname(nickname);
            pr.setScore(Integer.valueOf(scoreStr));
            pr.setDirection(DIRECTIONS[i - 1]);
            records.add(pr);
        }
        return records;
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED, cmd = "创建比赛\\s?(\\S*)\\n([\\s\\S]+)")
    public void createContest(Bot bot, GroupMessageEvent event, Matcher matcher) {
        pluginBizServiceTemplate.execute(
                BizServiceTypeEnum.CREATE_CONTEST, new ServiceCallback<CreateContestBizServiceRequest, CreateContestBizServiceResult>() {
                    @Override
                    public CreateContestBizServiceRequest buildRequest() {
                        String stringType = matcher.group(1);
                        ContestType type = (stringType == null || stringType.isEmpty()) ? ContestType.M : ContestType.valueOf(stringType);
                        String body = matcher.group(2);

                        CreateContestBizServiceRequest request = new CreateContestBizServiceRequest();
                        request.setGroupId(event.getGroupId());
                        request.setContestType(type);
                        request.setRecords(parseRecords(body));
                        return request;
                    }

                    @Override
                    public void success(CreateContestBizServiceResult result) {
                        MsgUtils builder = MsgUtils.builder();
                        builder.reply(event.getMessageId());
                        builder.text("✅ 比赛创建并结算完成\n");
                        builder.text("比赛ID: " + result.getContest().getId() + "\n");
                        builder.text("比赛类型: " + result.getContest().getType().getDescription() + "\n");
                        bot.sendGroupMsg(event.getGroupId(), builder.build(), false);
                    }

                    @Override
                    public void fail(CreateContestBizServiceRequest request, BizServiceException e) {
                        bot.sendGroupMsg(event.getGroupId(), "创建失败：" + e.getMessage(), false);
                    }
                });
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED, cmd = "查询比赛\\s*(\\d*)")
    public void getContest(Bot bot, GroupMessageEvent event, Matcher matcher) {
        String contestIdStr = matcher.group(1);

        if (contestIdStr != null && !contestIdStr.isEmpty()) {
            Integer contestId = Integer.valueOf(contestIdStr);
            Contest contest = contestService.getById(contestId);
            if (contest == null) {
                bot.sendGroupMsg(event.getGroupId(), "比赛不存在", false);
                return;
            }

            List<ContestEnd> endList = contestEndService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ContestEnd>().eq("contest_id", contestId));
            if (endList.isEmpty()) {
                bot.sendGroupMsg(event.getGroupId(), "该比赛尚未结算", false);
                return;
            }

            List<ContestRecord> records = contestRecordService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ContestRecord>().eq("contest_id", contestId));

            MsgUtils builder = MsgUtils.builder();
            builder.reply(event.getMessageId());
            builder.text("比赛ID: " + contestId + " | 类型: " + contest.getType().getDescription() + "\n");

            for (ContestEnd end : endList) {
                User user = userService.getById(end.getUserId());
                String name = (user != null) ? user.getNickname() : "未知";
                ContestRecord record = records.stream()
                        .filter(r -> r.getRecordUserId().equals(end.getUserId()))
                        .findFirst().orElse(null);

                String directionStr = (record != null) ? record.getDirection().getName() : "?";
                Integer point = (record != null) ? record.getPoint() : 0;

                String sign = end.getEloChange().compareTo(java.math.BigDecimal.ZERO) >= 0 ? "+" : "";
                builder.text("👤 " + name + " (" + directionStr + ") "
                        + (point >= 0 ? "+" : "") + point
                        + " → 精算 " + end.getEndPoint()
                        + " | ELO " + sign + end.getEloChange() + "\n");
            }
            bot.sendGroupMsg(event.getGroupId(), builder.build(), false);
        } else {
            Integer page = 1;
            Integer size = 10;
            List<Contest> contests = contestService.queryLastContest4Group(event.getGroupId(), page, size);
            if (contests.isEmpty()) {
                bot.sendGroupMsg(event.getGroupId(), "没有找到任何比赛", false);
                return;
            }

            MsgUtils builder = MsgUtils.builder();
            builder.reply(event.getMessageId());
            for (Contest contest : contests) {
                builder.text("比赛ID: " + contest.getId()
                        + " | 类型: " + contest.getType().getDescription()
                        + " | 状态: " + contest.getStatus() + "\n");
            }
            bot.sendGroupMsg(event.getGroupId(), builder.build(), false);
        }
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED, cmd = "查看排名\\s?(\\S*)")
    public void getRanking(Bot bot, GroupMessageEvent event, Matcher matcher) {
        String typeStr = matcher.group(1);
        ContestType type;
        try {
            type = (typeStr == null || typeStr.isEmpty()) ? ContestType.M : ContestType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            bot.sendGroupMsg(event.getGroupId(), "未知的比赛类型：" + typeStr, false);
            return;
        }

        List<Elo> ranking = eloService.getRankingByType(type, event.getGroupId(), 20);
        if (ranking.isEmpty()) {
            bot.sendGroupMsg(event.getGroupId(), "暂无排名数据", false);
            return;
        }

        MsgUtils builder = MsgUtils.builder();
        builder.reply(event.getMessageId());
        builder.text("🏆 " + type.getDescription() + " Elo 排名\n");
        for (int i = 0; i < ranking.size(); i++) {
            Elo elo = ranking.get(i);
            User user = userService.getById(elo.getUserId());
            String name = (user != null) ? user.getNickname() : "未知用户";
            builder.text((i + 1) + ". " + name + " - " + elo.getElo() + "\n");
        }
        bot.sendGroupMsg(event.getGroupId(), builder.build(), false);
    }
}
