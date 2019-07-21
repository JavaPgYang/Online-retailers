package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;   // 公众账号ID

    @Value("${partner}")
    private String partner;     // 商户号

    @Value("${partnerkey}")
    private String partnerkey;      // 密钥

    @Value("${notifyurl}")
    private String notifyurl;   // 通知地址


    @Override
    public Map createNative(String out_trade_no, String total_fee, String itemID) {
        // 1.map封装请求的参数
        Map<String, String> param = new HashMap<String, String>();
        param.put("appid", appid);    // 公众账号ID
        param.put("mch_id", partner);     // 商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());     // 随机字符串
        param.put("body", "品优购");     // 商品描述
        param.put("out_trade_no", out_trade_no);     // 商户订单号
        param.put("total_fee", total_fee);    // 标价金额
        param.put("spbill_create_ip", "127.0.0.1");     // 终端IP
        param.put("notify_url", notifyurl);   // 通知地址
        param.put("trade_type", "NATIVE");   // 交易类型
        param.put("product_id", itemID);   // 商品ID

        // 定义一个用于返回的Map
        Map<String, String> resultMap = new HashMap<String, String>();

        try {
            // 2.对微信支付接口发起请求
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            // 分割线
            System.out.println("=========================================================================================");

            // 3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            // 封装返回结果
            resultMap.put("code_url", map.get("code_url"));      // 二维码链接
            resultMap.put("total_fee", total_fee);      // 标价金额（元）
            resultMap.put("out_trade_no", out_trade_no);    // 订单号
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return resultMap;
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        // 1.封装请求查询订单接口的参数
        Map<String, String> param = new HashMap<String, String>();
        param.put("appid", appid);    // 公众账号ID
        param.put("mch_id", partner);     // 商户号
        param.put("out_trade_no", out_trade_no);     // 商户订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());     // 随机字符串

        try {
            // 2.发送请求
            // 转换请求需要的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            // 3.返回结果
            String result = httpClient.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
