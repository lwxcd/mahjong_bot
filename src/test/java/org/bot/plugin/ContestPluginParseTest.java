package org.bot.plugin;

import org.bot.biz.BizServiceException;
import org.bot.biz.request.contest.CreateContestBizServiceRequest;
import org.bot.model.type.DirectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContestPluginParseTest {

    @Test
    public void testParseRecordsNormal() {
        String body = "南4 2本场\nbbb 46400\nccc 4800\naaa 20400\nddd 27400";
        List<CreateContestBizServiceRequest.PlayerRecord> records = ContestPlugin.parseRecords(body);

        assertEquals(4, records.size());
        assertEquals("bbb", records.get(0).getNickname());
        assertEquals(46400, records.get(0).getScore());
        assertEquals(DirectionType.EAST, records.get(0).getDirection());

        assertEquals("ccc", records.get(1).getNickname());
        assertEquals(4800, records.get(1).getScore());
        assertEquals(DirectionType.SOUTH, records.get(1).getDirection());

        assertEquals("aaa", records.get(2).getNickname());
        assertEquals(20400, records.get(2).getScore());
        assertEquals(DirectionType.WEST, records.get(2).getDirection());

        assertEquals("ddd", records.get(3).getNickname());
        assertEquals(27400, records.get(3).getScore());
        assertEquals(DirectionType.NORTH, records.get(3).getDirection());
    }

    @Test
    public void testParseRecordsIgnoresBlankLines() {
        String body = "南4 2本场\n\nbbb 46400\n\nccc 4800\naaa 20400\nddd 27400\n";
        List<CreateContestBizServiceRequest.PlayerRecord> records = ContestPlugin.parseRecords(body);
        assertEquals(4, records.size());
    }

    @Test
    public void testParseRecordsNicknameWithSpaces() {
        String body = "南4\nplayer one 30000\nplayer two 25000\nplayer three 25000\nplayer four 20000";
        List<CreateContestBizServiceRequest.PlayerRecord> records = ContestPlugin.parseRecords(body);
        assertEquals("player one", records.get(0).getNickname());
        assertEquals(30000, records.get(0).getScore());
    }

    @Test
    public void testParseRecordsTooFewLines() {
        String body = "南4\nbbb 46400\nccc 4800";
        BizServiceException ex = assertThrows(BizServiceException.class, () -> ContestPlugin.parseRecords(body));
        assertTrue(ex.getMessage().contains("格式错误"));
    }

    @Test
    public void testParseRecordsBadFormat() {
        String body = "南4\nbbb46400\nccc 4800\naaa 20400\nddd 27400";
        BizServiceException ex = assertThrows(BizServiceException.class, () -> ContestPlugin.parseRecords(body));
        assertTrue(ex.getMessage().contains("格式错误"));
    }

    @Test
    public void testParseRecordsNegativeScore() {
        String body = "南4\nbbb -5000\nccc 4800\naaa 20400\nddd 27400";
        List<CreateContestBizServiceRequest.PlayerRecord> records = ContestPlugin.parseRecords(body);
        assertEquals(-5000, records.get(0).getScore());
    }
}
