package com.yq.wx.wxpay.sdk;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @Author : Yanqiang
 * @Date : 2019-08-06
 * @Description : 微信支付/退款配置类 继承abstract WXPayConfig ,自己实现微信配置
 */
public class MyWXPayConfig extends WXPayConfig {

    /**
     * 证书的字节流
     */
    private byte[] certData;

    /**
     * 配置类的实例
     */
    private static MyWXPayConfig INSTANCE;

    /**
     * 运行环境  dev是测试；online是正式
     */
    private String profiles;


    /**
     * @Author : Yanqiang
     * @Date : 2019-08-06
     * @Params : [profiles]
     * @Description : 主要是为了根据当前运行环境，区分使用的证书
     */
    public MyWXPayConfig(String profiles) throws Exception {
        this.profiles = profiles;
        String path;
        //不是沙箱环境要要下载证书，开出来,根据环境区分
        if (profiles.equals("online")) {
            path = "classpath:online.p12";
        } else {
            path = "classpath:dev.p12";
        }
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        InputStream certStream = resource.getInputStream();

        this.certData = new byte[(int) resource.contentLength()];
        certStream.read(this.certData);
        certStream.close();
        System.out.println(path);
    }


    /**
     * @Author : Yanqiang
     * @Date : 2019-08-06
     * @Params : []
     * @Return : java.lang.String
     * @Description : 获取 appid
     */
    @Override
    String getAppID() {
        return "wx12345678998765432";
    }

    /**
     * @Author : Yanqiang
     * @Date : 2019-08-06
     * @Params : []
     * @Return : java.lang.String
     * @Description : 主要是为了根据当前运行环境，区分使用的MchID
     */
    @Override
    String getMchID() {
        //0是测试；1是正式
        System.out.println(profiles);
        if (profiles.equals("online")) {
            return "12345678";
        } else {
            return "87654321";
        }
    }

    /**
     * @Author : Yanqiang
     * @Date : 2019-08-06
     * @Params : []
     * @Return : java.lang.String
     * @Description : 商户 key
     */
    @Override
    String getKey() {
        return "12345678900000abcdefghijklmn";
    }

    /**
     * @Author : Yanqiang
     * @Date : 2019-08-06
     * @Params : []
     * @Return : java.io.InputStream
     * @Description : 获取证书的字节流
     */
    @Override
    InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    /**
     * @Author : Yanqiang
     * @Date : 2019-08-06
     * @Params : []
     * @Return : com.yq.wx.wxpay.sdk.WXPayDomain
     * @Description : 获取配置类的实体
     */
    @Override
    WXPayDomain getWXPayDomain() {
        return MyWXPayDomainImpl.instance();
    }
}
