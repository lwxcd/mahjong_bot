package org.bot.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Shiro
@Component
@Slf4j
public class HelpPlugin {

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = "help", at = AtEnum.NEED)
    public void help(Bot bot, AnyMessageEvent event) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/help.jpg");
        byte[] img = resource.getContentAsByteArray();
        MsgUtils msg = MsgUtils.builder()
                .reply(event.getMessageId())
                .img(img)
                .text("欢迎使用 Mahjong Bot\n")
                .text("群聊命令：\n")
                .text("- 创建比赛 [规则]：创建比赛并提交分数（多行格式，详见下方示例）\n")
                .text("- 查询比赛 [比赛ID]：查看群内最近的比赛，或指定ID查看详情\n")
                .text("- 查看排名 [规则]：查看 Elo 排名（默认 M 规）\n")
                .text("规则类型：RCR | MCR | A | M（默认 M）\n")
                .text("\n示例：\n")
                .text("创建比赛 M\n")
                .text("南4 2本场\n")
                .text("bbb 46400\n")
                .text("ccc 4800\n")
                .text("aaa 20400\n")
                .text("ddd 27400\n");

        bot.sendMsg(event, msg.build(), false);
    }
}
