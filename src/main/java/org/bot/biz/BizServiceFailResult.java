package org.bot.biz;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bot.biz.base.BizServiceBaseRequest;
import org.bot.biz.base.BizServiceBaseResult;
import org.bot.biz.base.BizServiceTypeEnum;

@EqualsAndHashCode(callSuper = true)
@Data
public class BizServiceFailResult extends BizServiceBaseResult {

    /*
     * 失败请求
     */
    private BizServiceBaseRequest failRequest;

    private String type;

    private String message;

    private Integer exceptionCode;

    private String exceptionMessage;


    public BizServiceFailResult(BizServiceBaseRequest failRequest, BizServiceTypeEnum type, BizServiceException e) {
        this.type = type.getType();
        this.message = e.getMessage();
        this.exceptionMessage = e.getType().description;
        this.exceptionCode = e.getType().code;
        this.failRequest = failRequest;
    }
}
