package com.yq.wx.common;

/**
 * @Author : Yanqiang
 * @Date : 2019-08-06
 * @Description : 状态码
 */
public enum StatusCode {

    NONE_ERROR("1", "成功"),

    /**
     * 1 开头 业务执行错误
     */
    TRANSFERS_ERROR("10000", "提现失败，请稍后重试"),






    END_ERROR("100000000", "xxxxx");


    private String code;
    private String msg;

    StatusCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
