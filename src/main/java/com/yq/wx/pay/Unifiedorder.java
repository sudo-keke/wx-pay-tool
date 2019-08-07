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
 *
 *  1.支付
 *  2.结果通知
 *  3.查询订单
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
        //total_fee单位为分,将元转为分，或者在外面就处理了
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


    /**
     * @Author : Yanqiang
     * @Date : 2019/7/22
     * @Param : [outTradeNo] 订单编号
     * @return : void
     * @Description : 查询订单
     *  在微信支付通知不到时，主动查询订单状态，以便及时更新用户支付信息
     *  建议在支付成功后调用此接口，不建议在查询用户订单时调用
     *  另，应该记录用户支付信息详细
     *
     * return_code 是用来判断通信状态的，在“结果通知”时必为 SUCCESS；
     * result_code 是用来判断业务结果的，指一次调用接口或回调的动作是否如愿执行成功。如“关闭订单”时关闭成功为 SUCCESS，因参数配置错误、找不到订单号、订单状态不允许关闭等其它关闭失败的情况为 FAIL；
     * trade_state 是用来判断交易状态的，“交易”是指微信支付订单。
     * 在调用“统一下单”时是未支付状态，根本没有支付成功的可能。
     * 在异步收到“结果通知”时，不要相信文档去判断 result_code，应调用“查询订单”，并判断 trade_state。
     */
    private void orderquery(String appId, String mchId, String outTradeNo){
        //构造请求参数
        ConcurrentHashMap<String, String> requestMap = new ConcurrentHashMap<>(8);
        requestMap.put("appid", appId);
        requestMap.put("mch_id", mchId);
        requestMap.put("nonce_str", WXPayUtil.generateNonceStr());
        requestMap.put("out_trade_no", outTradeNo);
        try {
            //MyWXPayConfig 配置类；useSandbox 沙箱环境 （false为正式/true为沙箱环境）
            WXPay wxPay = new WXPay(new MyWXPayConfig("online"), null, true, false);
            Map<String, String> refund = wxPay.orderQuery(requestMap, 6 * 1000, 8 * 1000);
            if (refund.get("return_code").equals("SUCCESS")) {
                //以下只在return_code为SUCCESS的时候有返回

                if (refund.get("result_code").equals("SUCCESS")){
                    //以下只在return_code为SUCCESS的时候有返回

                    if(refund.get("trade_state").equals("SUCCESS")){
                        //支付成功,说明该行数据为一笔支付成功的订单，更新用户订单信息

                    }else if (refund.get("trade_state").equals("REFUND")){
                        //转入退款，说明该行数据为一笔发起退款成功的退款单，说明退款成功，进行订单退款处理的状态变更

                    }else if(refund.get("trade_state").equals("NOTPAY")){
                        //未支付，删除订单，如果用户再次发起购买，重新下单

                    }else if(refund.get("trade_state").equals("CLOSED")){
                        //已关闭，删除订单，如果用户再次发起购买，重新下单

                    }else if(refund.get("trade_state").equals("USERPAYING")){
                        //用户支付中，延迟一会儿，再次查询

                    }else if(refund.get("trade_state").equals("PAYERROR")){
                        //支付失败(其他原因，如银行返回失败) 删除订单，如果用户再次发起购买，重新下单

                    }else {
                        //其他原因

                    }
                }else {
                    refund.get("err_code");
                    refund.get("err_code_des");
                }
            }else {
                //return_code为FAIL,只返回这个两个字段
                refund.get("return_code");
                refund.get("return_msg");
            }
        }catch (Exception e){
            e.getStackTrace();
        }
    }
}
