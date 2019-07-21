package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    /**
     * 发起微信接口请求，返回支付链接生成二维码
     *
     * @param out_trade_no 商户订单号
     * @param total_fee    标价金额（分）
     * @return map
     */
    Map createNative(String out_trade_no, String total_fee, String itemID);

    /**
     * 查询订单支付状态
     *
     * @param out_trade_no 商户订单号
     * @return map
     */
    Map queryPayStatus(String out_trade_no);


}
