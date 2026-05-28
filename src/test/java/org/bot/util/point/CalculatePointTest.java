package org.bot.util.point;


import org.junit.jupiter.api.Test;
import org.bot.model.type.ContestType;
import org.bot.model.type.DirectionType;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatePointTest {

    @Test
    public void test4BaseRule() {
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 26000,
                DirectionType.SOUTH, 26000,
                DirectionType.WEST, 24000,
                DirectionType.NORTH, 24000
        );

        Map<DirectionType, BigDecimal> rmu = RuleCalculate.calculate(ContestType.RCR, context);

        assertEquals(new BigDecimal("16.00"), rmu.get(DirectionType.EAST));
        assertEquals(new BigDecimal("6.00"), rmu.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("-6.00"), rmu.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-16.00"), rmu.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleNormal() {
        // 东50000 南30000 西15000 北5000 = 100000
        // 排序: 东(50) > 南(30) > 西(15) > 北(5)
        // basePt: 东=20, 南=0, 西=-15, 北=-25
        // 马点: +50, +10, -10, -30
        // 结果: 东=70, 南=10, 西=-25, 北=-55
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 50000,
                DirectionType.SOUTH, 30000,
                DirectionType.WEST, 15000,
                DirectionType.NORTH, 5000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("70.00"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("10.00"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("-25.00"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-55.00"), result.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleWithRiichi() {
        // 总分 99000，立直棒 1000 点 -> 1.00 加到最高分
        // 东50000 南29000 西15000 北5000 = 99000
        // 排序: 东(50000) > 南(29000) > 西(15000) > 北(5000)
        // basePt: 东=20, 南=-1, 西=-15, 北=-25
        // 马点: +50, +10, -10, -30
        // 立直棒: 东 +1
        // 结果: 东=71, 南=9, 西=-25, 北=-55
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 50000,
                DirectionType.SOUTH, 29000,
                DirectionType.WEST, 15000,
                DirectionType.NORTH, 5000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("71.00"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("9.00"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("-25.00"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-55.00"), result.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleTwoTied() {
        // 东30000 南30000 西25000 北15000 = 100000
        // 排序: 东(30000) 南(30000) > 西(25000) > 北(15000)
        // 东南同分，平分马点 (50+10)/2 = 30
        // basePt: 东=0, 南=0, 西=-5, 北=-15
        // 结果: 东=30, 南=30, 西=-15, 北=-45
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 30000,
                DirectionType.SOUTH, 30000,
                DirectionType.WEST, 25000,
                DirectionType.NORTH, 15000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("30.00"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("30.00"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("-15.00"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-45.00"), result.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleThreeTied() {
        // 东30000 南30000 西30000 北10000 = 100000
        // 3人同分，马点总和 = 50+10+(-10) = 50
        // 按 4:3:3 分割: 东=20, 南=15, 西=15
        // basePt: 东=0, 南=0, 西=0, 北=-20
        // 北单独: -30 马点
        // 结果: 东=20, 南=15, 西=15, 北=-50
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 30000,
                DirectionType.SOUTH, 30000,
                DirectionType.WEST, 30000,
                DirectionType.NORTH, 10000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("20.00"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("15.00"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("15.00"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-50.00"), result.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleFourTied() {
        // 全员25000 = 100000
        // 4人同分: 全部 +0 码点
        // basePt: 全部 = -5
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 25000,
                DirectionType.SOUTH, 25000,
                DirectionType.WEST, 25000,
                DirectionType.NORTH, 25000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("-5.00"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("-5.00"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("-5.00"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-5.00"), result.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleTwoTiedWithRiichi() {
        // 东30000 南30000 西24000 北15000 = 99000, 立直棒 1000 -> 1.00
        // 东南同分且为最高分，平分马点 (50+10)/2=30，立直棒平分 1.00/2=0.50
        // basePt: 东=0, 南=0, 西=-6, 北=-15
        // 结果: 东=30.50, 南=30.50, 西=-16, 北=-45
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 30000,
                DirectionType.SOUTH, 30000,
                DirectionType.WEST, 24000,
                DirectionType.NORTH, 15000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("30.50"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("30.50"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("-16.00"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-45.00"), result.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleThreeTiedWithRiichi() {
        // 东30000 南30000 西30000 北9000 = 99000, 立直棒 1000 -> 1.00
        // 3人同分且为最高分，马点总和 = 50+10+(-10) = 50
        // 按 4:3:3: 东=20, 南=15, 西=15
        // 立直棒按 4:3:3: 东=0.40, 南=0.30, 西=0.30
        // basePt: 东=0, 南=0, 西=0, 北=-21
        // 北单独: -30 马点
        // 结果: 东=20.40, 南=15.30, 西=15.30, 北=-51
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 30000,
                DirectionType.SOUTH, 30000,
                DirectionType.WEST, 30000,
                DirectionType.NORTH, 9000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("20.40"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("15.30"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("15.30"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-51.00"), result.get(DirectionType.NORTH));
    }

    @Test
    public void testMRuleTwoPairsTied() {
        // 东35000 南35000 西15000 北15000 = 100000
        // 东南同分(1-2位)：平分马点 (50+10)/2 = 30
        // 西北同分(3-4位)：平分马点 (-10+-30)/2 = -20
        // basePt: 东=5, 南=5, 西=-15, 北=-15
        // 结果: 东=35, 南=35, 西=-35, 北=-35
        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 35000,
                DirectionType.SOUTH, 35000,
                DirectionType.WEST, 15000,
                DirectionType.NORTH, 15000
        );

        Map<DirectionType, BigDecimal> result = RuleCalculate.calculate(ContestType.M, context);

        assertEquals(new BigDecimal("35.00"), result.get(DirectionType.EAST));
        assertEquals(new BigDecimal("35.00"), result.get(DirectionType.SOUTH));
        assertEquals(new BigDecimal("-35.00"), result.get(DirectionType.WEST));
        assertEquals(new BigDecimal("-35.00"), result.get(DirectionType.NORTH));
    }
}
