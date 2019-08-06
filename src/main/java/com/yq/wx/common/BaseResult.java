package com.yq.wx.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @Author : Yanqiang
 * @Date : 2019/2/14
 * @Description : 统一返回结果类
 */
public class BaseResult implements Serializable {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 状态码：1成功，其他为失败
     */
    public String code = "1";

    /**
     * 成功为success，其他为失败原因
     */
    public String message = "success";

    /**
     * token
     */
    public String token;

    /**
     * 数据结果集
     */
    public Object data;

    public BaseResult(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResult() {

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        if (!code.equals("1")) {
            logger.error(message);
        }
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "BaseResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", token='" + token + '\'' +
                ", data=" + data +
                '}';
    }


    public BaseResult setStatus(StatusCode statusCode) {
        this.setCode(statusCode.getCode());
        this.setMessage(statusCode.getMsg());
        if (!code.equals("1")) {
            logger.error(message);
        }
        return this;
    }
}
