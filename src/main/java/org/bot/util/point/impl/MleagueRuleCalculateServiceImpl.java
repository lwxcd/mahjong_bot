package org.bot.util.point.impl;

import org.bot.model.type.DirectionType;
import org.bot.util.point.CalculateService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MleagueRuleCalculateServiceImpl implements CalculateService {

    private static final int[] MA_POINTS = {50, 10, -10, -30};
    private static final int ORIGIN = 30000;

    @Override
    public Map<DirectionType, BigDecimal> calculate(Map<DirectionType, Integer> context) {
        if (context == null || context.isEmpty()) {
            return new HashMap<>();
        }

        Set<DirectionType> directions = EnumSet.allOf(DirectionType.class);
        if (!context.keySet().containsAll(directions)) {
            throw new IllegalArgumentException("必须包含所有四个方向的得分信息");
        }

        int totalScore = context.values().stream().mapToInt(Integer::intValue).sum();
        if (totalScore > 100000) {
            throw new IllegalArgumentException("总分数不能超过100000");
        }

        BigDecimal riichiBonusPerThousand = BigDecimal.ZERO;
        if (totalScore < 100000) {
            riichiBonusPerThousand = new BigDecimal(100000 - totalScore)
                    .divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
        }

        // 按分数降序排序，同分按座位升序
        List<Map.Entry<DirectionType, Integer>> sorted = new ArrayList<>(context.entrySet());
        sorted.sort((e1, e2) -> {
            int cmp = e2.getValue().compareTo(e1.getValue());
            if (cmp == 0) {
                return Integer.compare(e1.getKey().getDirection(), e2.getKey().getDirection());
            }
            return cmp;
        });

        Map<DirectionType, BigDecimal> result = new HashMap<>();

        // 识别同分组
        int i = 0;
        while (i < 4) {
            int j = i;
            while (j < 4 && sorted.get(j).getValue().equals(sorted.get(i).getValue())) {
                j++;
            }
            int groupSize = j - i;

            if (groupSize == 4) {
                // 4人同分：全部 +0
                for (int k = i; k < j; k++) {
                    DirectionType dir = sorted.get(k).getKey();
                    BigDecimal basePt = new BigDecimal(sorted.get(k).getValue() - ORIGIN)
                            .divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
                    result.put(dir, basePt);
                }
            } else if (groupSize == 3) {
                // 3人同分：按 4:3:3 分割马点总和
                int maSum = 0;
                for (int k = i; k < j; k++) {
                    maSum += MA_POINTS[k];
                }
                BigDecimal totalMa = new BigDecimal(maSum);
                BigDecimal[] ratios = {
                        new BigDecimal(4).divide(new BigDecimal(10), 10, RoundingMode.HALF_UP),
                        new BigDecimal(3).divide(new BigDecimal(10), 10, RoundingMode.HALF_UP),
                        new BigDecimal(3).divide(new BigDecimal(10), 10, RoundingMode.HALF_UP)
                };

                // 立直棒分配：如果这组是最高分组（i==0），按 4:3:3 分割
                boolean getsRiichi = (i == 0 && riichiBonusPerThousand.compareTo(BigDecimal.ZERO) > 0);

                for (int k = 0; k < groupSize; k++) {
                    DirectionType dir = sorted.get(i + k).getKey();
                    BigDecimal basePt = new BigDecimal(sorted.get(i + k).getValue() - ORIGIN)
                            .divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
                    BigDecimal maBonus = totalMa.multiply(ratios[k]).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal riichiBonus = getsRiichi
                            ? riichiBonusPerThousand.multiply(ratios[k]).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    result.put(dir, basePt.add(maBonus).add(riichiBonus));
                }

                if (getsRiichi) {
                    riichiBonusPerThousand = BigDecimal.ZERO;
                }
            } else if (groupSize == 2) {
                // 2人同分：平分马点
                BigDecimal avgMa = new BigDecimal(MA_POINTS[i] + MA_POINTS[i + 1])
                        .divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);

                // 立直棒分配：如果这组包含最高分（i==0），平分
                boolean getsRiichi = (i == 0 && riichiBonusPerThousand.compareTo(BigDecimal.ZERO) > 0);
                BigDecimal riichiEach = getsRiichi
                        ? riichiBonusPerThousand.divide(new BigDecimal(2), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                for (int k = i; k < j; k++) {
                    DirectionType dir = sorted.get(k).getKey();
                    BigDecimal basePt = new BigDecimal(sorted.get(k).getValue() - ORIGIN)
                            .divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
                    result.put(dir, basePt.add(avgMa).add(riichiEach));
                }

                if (getsRiichi) {
                    riichiBonusPerThousand = BigDecimal.ZERO;
                }
            } else {
                // 单人
                DirectionType dir = sorted.get(i).getKey();
                BigDecimal basePt = new BigDecimal(sorted.get(i).getValue() - ORIGIN)
                        .divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
                BigDecimal maBonus = new BigDecimal(MA_POINTS[i]);

                BigDecimal riichiBonus = BigDecimal.ZERO;
                if (i == 0 && riichiBonusPerThousand.compareTo(BigDecimal.ZERO) > 0) {
                    riichiBonus = riichiBonusPerThousand;
                    riichiBonusPerThousand = BigDecimal.ZERO;
                }

                result.put(dir, basePt.add(maBonus).add(riichiBonus));
            }

            i = j;
        }

        return result;
    }
}
