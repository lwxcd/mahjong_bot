package org.bot.util.point;


import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;
import org.bot.model.type.ContestType;
import org.bot.model.type.DirectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

public class CalculatePointTest {

    private final static Logger logger = LoggerFactory.getLogger(CalculatePointTest.class);

    @Test
    public void test4BaseRule() {

        Map<DirectionType, Integer> context = Map.of(
                DirectionType.EAST, 26000,
                DirectionType.SOUTH, 26000,
                DirectionType.WEST, 24000,
                DirectionType.NORTH, 24000
        );

        Map<DirectionType, BigDecimal> rmu = RuleCalculate.calculate(ContestType.RCR, context);

        logger.info(rmu.toString());

        Map<DirectionType, BigDecimal> complete = Map.of(
                DirectionType.EAST, new BigDecimal("16.00"),
                DirectionType.SOUTH, new BigDecimal("6.00"),
                DirectionType.WEST, new BigDecimal("-6.00"),
                DirectionType.NORTH, new BigDecimal("-16.00")
        );

        Assert.equals(complete, rmu);

    }

}
