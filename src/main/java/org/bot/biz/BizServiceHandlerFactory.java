package org.bot.biz;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.bot.biz.base.AbstractBizServiceHandler;
import org.bot.biz.base.BizServiceBaseRequest;
import org.bot.biz.base.BizServiceBaseResult;
import org.bot.biz.base.BizServiceTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Slf4j
@Component
public class BizServiceHandlerFactory {

    private static final Map<String, AbstractBizServiceHandler<?, ?>> HANDLER_MAP = new HashMap<>();


    @Autowired
    public BizServiceHandlerFactory(List<AbstractBizServiceHandler<?, ?>> list) {
        for (AbstractBizServiceHandler<?, ?> handler : list) {
            // 从反射注解获取类型
            BizServiceHandleInterface annotation = AnnotationUtils.findAnnotation(handler.getClass()
                    , BizServiceHandleInterface.class);

            if (annotation == null) {
                log.error("{} 没有注解", handler.getClass());
                throw new RuntimeException("{} 没有注解");
            }

            HANDLER_MAP.put(annotation.type().getType(), handler);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends BizServiceBaseRequest, R extends BizServiceBaseResult>
    AbstractBizServiceHandler<T, R> getHandler(BizServiceTypeEnum type) {

        Assert.notNull(type, "type 不能为空");

        Assert.isTrue(HANDLER_MAP.containsKey(type.getType()), "没有找到对应的处理器");

        return (AbstractBizServiceHandler<T, R>) HANDLER_MAP.get(type.getType());
    }
}
