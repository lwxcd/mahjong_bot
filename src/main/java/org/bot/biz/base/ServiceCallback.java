package org.bot.biz.base;

import org.bot.biz.BizServiceException;


public interface ServiceCallback
        <T extends BizServiceBaseRequest, R extends BizServiceBaseResult> {

    /*
     * 预处理
     * @return 是否校验通过 true:通过 false:不通过
     */
    default void preHandle(T request) {
    }

    /*
     * 执行
     */
    default R doExecute(AbstractBizServiceHandler<T, R> handler, T request) {
        return handler.handle(request);
    }

    /*
     * 校验请求参数
     *
     */
    default T buildRequest() {
        return null;
    }

    /*
     * 失败处理
     */
    default void fail(T request, BizServiceException e) {

    }

    void success(R result);
}
