package org.bot.util.point.impl;

import org.bot.model.type.DirectionType;
import org.bot.util.point.CalculateService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class EmptyCalculateServiceImpl implements CalculateService {
    @Override
    public Map<DirectionType, BigDecimal> calculate(Map<DirectionType, Integer> context) {

        // 转换后直接输出
        Map<DirectionType,BigDecimal> ret = new HashMap<>(context.size());

        context.forEach(
                (k,v) ->{
                    ret.put(k,BigDecimal.valueOf(v));
                }
        );

        return ret;
    }
}
