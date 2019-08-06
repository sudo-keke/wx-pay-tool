package com.yq.wx.wxpay.sdk;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : Yanqiang
 * @Date : 2019/8/5
 * @Description : 微信支付枚举判断
 *  可以不用这个，一个辅助枚举类
 */
public enum WXResultCode {
    /**
     * 状态
     */
    NO_AUTH("NO_AUTH", "没有该接口权限"),
    AMOUNT_LIMIT("AMOUNT_LIMIT", "金额超过微信限制"),
    PARAM_ERROR("PARAM_ERROR", "参数错误"),
    OPENID_ERROR("OPENID_ERROR", "您的账号Openid错误，请联系管理员"),
    SEND_FAILED("SEND_FAILED", "微信向您付款错误，请稍后再试"),
    NOTENOUGH("NOTENOUGH", "付款余额不足"),
    SYSTEMERROR("SYSTEMERROR", "微信内部接口调用发生错误"),
    NAME_MISMATCH("NAME_MISMATCH", "姓名校验失败，请先进行实名认证"),
    SIGN_ERROR("SIGN_ERROR", "签名错误"),
    XML_ERROR("XML_ERROR", "Post内容出错"),
    FREQ_LIMIT("FREQ_LIMIT", "调用接口过于频繁，请稍后再试"),
    MONEY_LIMIT("MONEY_LIMIT", "已经达到今日付款总额上限"),
    CA_ERROR("CA_ERROR", "商户API证书校验出错"),
    V2_ACCOUNT_SIMPLE_BAN("V2_ACCOUNT_SIMPLE_BAN", "微信不支持给非实名用户付款"),
    SENDNUM_LIMIT("NO_AUTH", "已经达到今日付款总次数上限");


    private String code;
    private String msg;

    WXResultCode(String code, String msg) {
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


    public static ConcurrentHashMap<String, String> WXCodeCache = new ConcurrentHashMap<>(100);

    static {
        for (WXResultCode resultCode : values()) {
            WXCodeCache.put(resultCode.code, resultCode.getMsg());
        }
    }


}
