package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     *
     * @return map
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        TbPayLog payLog = orderService.searchPayLogFromRedis(SecurityContextHolder.getContext().getAuthentication().getName());
        if (payLog != null) {
            return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "", "001");
        }
        return null;
    }

    /**
     * 查询订单状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {


        Result result = null;

        int x = 0;
        while (true) {
            // 调用查询接口
            Map resultMap = weixinPayService.queryPayStatus(out_trade_no);
            // 查询不到结果，意味出错
            if (resultMap == null) {
                result = new Result(false, "支付出错");
                break;
            }
            // 交易状态：SUCCESS状态为成功
            if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                // 更新订单状态及日志记录
                orderService.updateOrderStatus(out_trade_no, (String) resultMap.get("transaction_id"));
                // 给前端返回支付结果
                result = new Result(true, "支付成功");
                break;
            }
            // 休眠3s，一分钟循环20次
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 5分钟后自动跳出循环
            if (x++ >= 100) {
                result = new Result(false, "二维码超时");
                break;
            }

        }
        return result;
    }

}
