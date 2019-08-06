package com.yq.wx.util;

import com.yq.wx.common.BaseResult;
import com.yq.wx.common.StatusCode;

/**
 * @ClassName :  WXPayUtil
 * @Author :  Yanqinag
 * @Date :  2019-08-06 15:03
 * @Description : 自定义工具类
 */
public class PayToolUtil {

    /**
     * @Author : Yanqiang
     * @Date : 2019/3/8
     * @Param : [baseResult, statusCode]
     * @return : void
     * @Description : 添加Code状态码和Msg状态值，并且返回baseResult
     */
    public static void setStatus(BaseResult baseResult, StatusCode statusCode) {
        baseResult.setCode(statusCode.getCode());
        baseResult.setMessage(statusCode.getMsg());
    }
}
