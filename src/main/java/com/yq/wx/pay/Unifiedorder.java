package com.yq.wx.pay;

import com.yq.wx.common.BaseResult;
import com.yq.wx.common.StatusCode;
import com.yq.wx.util.PayToolUtil;
import com.yq.wx.wxpay.sdk.MyWXPayConfig;
import com.yq.wx.wxpay.sdk.WXPay;
import com.yq.wx.wxpay.sdk.WXPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName :  Unifiedorder
 * @Author :  Yanqinag
 * @Date :  2019-08-07 11:03
 * @Description :  微信支付
 */
public class Unifiedorder {

    private static Logger log = LoggerFactory.getLogger(Unifiedorder.class);

    /**
     * @Author : Yanqiang
     * @Date : 2019/08/07
     * @Param : [afterSale, refundFee]
     * @return : java.util.Map<java.lang.String,java.lang.String>
     * @Description : 微信小程序支付 URL地址：https://api.mch.weixin.qq.com/pay/unifiedorder
     *
     *  参数 ：
     *       appId             微信分配的小程序ID
     *       mchId             微信支付分配的商户号
     *       nonce_str         随机字符串，长度要求在32位以内
     *       body              商品简单描述(支付弹窗上显示的内容)，该字段请按照规范传递,详见第七条-->https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=4_2
     *       out_trade_no      商户系统内部订单号，在同一个商户号下唯一
     *       total_fee         订单总金额，单位为分
     *       spbill_create_ip  终端IP --> 支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
     *       notify_url        异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
     *       trade_type        小程序取值如下：JSAPI，详细说明见
     *
     *       sign              通过签名算法计算得出的签名值(签名本身不参与签名)
     */
    public BaseResult unifiedorder(HttpServletRequest request, String appId, String mchId, String body, String outTradeNo, String totalFee, String openId, String notifyUrl)  {

        BaseResult baseResult = new BaseResult();
        String total = new BigDecimal(totalFee).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        //构造请求参数 不需要此时设置sign签名，wxPay.unifiedOrder()接口 通过配置类自动配置
        ConcurrentHashMap<String, String> requestMap = new ConcurrentHashMap<>(20);
        requestMap.put("appid",appId);
        requestMap.put("mch_id", mchId);
        requestMap.put("nonce_str", WXPayUtil.generateNonceStr());
        requestMap.put("body", body);
        requestMap.put("out_trade_no", outTradeNo);
        requestMap.put("total_fee", total);
        requestMap.put("spbill_create_ip",WXPayUtil.getIp2(request));
        requestMap.put("notify_url", notifyUrl);
        requestMap.put("openid",openId);
        requestMap.put("trade_type", "JSAPI");
        try {
            //MyWXPayConfig 配置类；useSandbox 沙箱环境 （false为正式/true为沙箱环境）
            WXPay wxPay = new WXPay(new MyWXPayConfig("online"), notifyUrl, true, false);
            Map<String, String> refund = wxPay.unifiedOrder(requestMap, 6 * 1000, 8 * 1000);
            if(refund.get("return_code").equals("FAIL") || refund.get("result_code").equals("FAIL")) {
                PayToolUtil.setStatus(baseResult, StatusCode.UNIFIEDORDER_ERROR);
                baseResult.setData(refund);
                return baseResult;
            }
            baseResult.setData(refund);
            return baseResult;

        }catch (Exception e){
            PayToolUtil.setStatus(baseResult, StatusCode.UNIFIEDORDER_ERROR);
        }
        return  baseResult;
    }


    /**
     * @Author : Yanqiang
     * @Date : 2019-08-07
     * @Params : [request, response]
     * @Return : void
     * @Description : 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
     *
     */
    public void notice(HttpServletRequest request, HttpServletResponse response) {
        try {
            //这块我是在拦截器里面使用了 request，又放到了Attribute里面，你们没有处理的话就可以直接取request.getAttribute()
            Map<String,String> map = WXPayUtil.xmlToMap((String) request.getAttribute("param"));
            if (map != null) {
                // 支付成功，商户处理后同步返回给微信参数
                PrintWriter writer = response.getWriter();
                // 此处调用订单查询接口验证是否交易成功
                String outTradeNo = map.get("out_trade_no");

                if (map.get("result_code").equals("SUCCESS") && map.get("return_code").equals("SUCCESS")) {
                    log.info("===============付款成功，执行以下业务处理==============");

                }else {
                    log.info("===============付款失败，删除订单==============");

                }
                // 通知微信已经收到消息，不要再给我发消息了，否则微信会8连击调用本接口
                String noticeStr = setXML("SUCCESS", "");
                writer.write(noticeStr);
                writer.flush();
                System.out.println("结束");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Author : Yanqiang
     * @Date : 2019-08-07
     * @Params : [return_code, return_msg]
     * @Return : java.lang.String
     * @Description : 回复微信回调通知的 xml 参数构造
     */
    public String setXML(String return_code, String return_msg) {
        return "<xml><return_code><![CDATA[" + return_code + "]]></return_code><return_msg><![CDATA[" + return_msg + "]]></return_msg></xml>";
    }
}
