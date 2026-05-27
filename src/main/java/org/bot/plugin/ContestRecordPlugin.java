package org.bot.plugin;

import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.service.ContestRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Shiro
@Component
@Slf4j
public class ContestRecordPlugin {

    @Autowired
    private ContestRecordService contestRecordService;

    @PrivateMessageHandler
    @MessageHandlerFilter(cmd = "更新比赛 (?<contestId>\\d+)")
    public void updateContest(Bot bot, PrivateMessageEvent event, Matcher matcher) {
        String contestIdStr = matcher.group("contestId");
        contestRecordService.calculateScore(Integer.valueOf(contestIdStr));
    }
}
