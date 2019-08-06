package com.yq.wx.pay;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yq.wx.common.BaseResult;
import com.yq.wx.common.StatusCode;
import com.yq.wx.util.PayToolUtil;
import com.yq.wx.wxpay.sdk.MyWXPayConfig;
import com.yq.wx.wxpay.sdk.WXPay;
import com.yq.wx.wxpay.sdk.WXPayConstants;
import com.yq.wx.wxpay.sdk.WXPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName :  Transfers
 * @Author :  Yanqinag
 * @Date :  2019-08-06 14:51
 * @Description :  企业付款到用户零钱
 */
public class Transfers {

    private static Logger logger = LoggerFactory.getLogger(Transfers.class);

    /**
     * @Author : Yanqiang
     * @Date : 2019-08-06
     * @Param : appId       公众账号appid
     * @Param : mchId       商户号
     * @Param : amount      转账金额（元）
     * @Param : openId      用户openid
     * @Param : realName    用户真实姓名
     * @Return : com.yq.wx.common.BaseResult
     * @Description :
     * 注意：企业付款到用户零钱，sign 加密只支持 MD5 !!!
     */
    public BaseResult transfers(String appId, String mchId, String amount,String openId,String realName) {
        BaseResult baseResult = new BaseResult();
        //将传过来的金额 amount 转换成以分为单位
        amount = new BigDecimal(amount).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        //构造请求参数 不需要此时设置sign签名，wxPay.transfers()接口 通过配置类自动配置
        ConcurrentHashMap<String, String> requestMap = new ConcurrentHashMap<>(20);
        //商户唯一单号
        String partnerTradeNo = WXPayUtil.generateIntegerStr();
        String nonceStr = WXPayUtil.generateNonceStr();
        //公众账号appid
        requestMap.put("mch_appid", appId);
        //商户号
        requestMap.put("mchid", mchId);
        //随机字符串
        requestMap.put("nonce_str", nonceStr);
        //商户订单号
        requestMap.put("partner_trade_no", partnerTradeNo);
        //用户openid
        requestMap.put("openid", openId);
        //校验用户姓名选项 NO_CHECK
        requestMap.put("check_name", "FORCE_CHECK");
        //FORCE_CHECK，则必填
        requestMap.put("re_user_name", realName);
        //转账金额
        requestMap.put("amount", amount);
        //企业付款描述信息
        requestMap.put("desc", "集乐多提现到用户零钱");
        //服务器Ip地址,ip随便
        requestMap.put("spbill_create_ip", "39.106.54.252");
        try {
            //MyWXPayConfig 配置类；-- 传递启动环境
            //notifyUrl ：微信回调通知
            //autoReport ：自动报告
            //useSandbox ：沙箱环境 （false为正式/true为沙箱环境）
            WXPay wxPay = new WXPay(new MyWXPayConfig("online"), "", true, false);
            Map<String, String> transfersMap = wxPay.transfers(requestMap, 6 * 1000, 8 * 1000);

            //以下为微信返回逻辑判断处理
            if (transfersMap.get("return_code").equals(WXPayConstants.SUCCESS)) {
                //以下字段不论失败还是成功，都会返回
                transfersMap.get("result_code");
                transfersMap.get("mch_appid");
                transfersMap.get("mchid");
                //只有return_code和result_code都为success时，才代表提现成功
                if (transfersMap.get("result_code").equals(WXPayConstants.SUCCESS)) {

                    transfersMap.get("nonce_str");
                    transfersMap.get("partner_trade_no");
                    transfersMap.get("payment_no");
                    transfersMap.get("payment_time");

                } else {
                    //以下为失败时返回，将失败信息记录
                    transfersMap.get("err_code");
                    transfersMap.get("err_code_des");
                }
            } else {
                PayToolUtil.setStatus(baseResult, StatusCode.TRANSFERS_ERROR);
            }
        } catch (Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PayToolUtil.setStatus(baseResult, StatusCode.TRANSFERS_ERROR);
        }
        return baseResult;
    }

}
