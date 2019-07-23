package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
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
    private SeckillOrderService seckillOrderService;

    /**
     * 生成二维码
     *
     * @return map
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        TbSeckillOrder seckillOrder = seckillOrderService.findOderFromRedis(SecurityContextHolder.getContext().getAuthentication().getName());
        if (seckillOrder != null) {
            long money = (long) (seckillOrder.getMoney().doubleValue() * 100);  // 转化以分为单位的总金额
            return weixinPayService.createNative(seckillOrder.getId() + "", money + "", "001");
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
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

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
                // 保存订单到DB，并清除缓存中的订单信息
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.parseLong(out_trade_no), (String) resultMap.get("transaction_id"));
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
                // 关闭微信支付接口
                Map<String, String> closeMap = weixinPayService.closePay(out_trade_no);
                // 关闭接口返回的数据中有错误码err_code=“ORDERPAID”，表示已支付，需执行支付成功方法
                if ("SUCCESS".equals(closeMap.get("return_code"))) {
                    if ("FAIL".equals(closeMap.get("result_code"))) {
                        if ("ORDERPAID".equals(closeMap.get("err_code"))) {
                            // 保存订单到DB，并清除缓存中的订单信息
                            seckillOrderService.saveOrderFromRedisToDb(userId, Long.parseLong(out_trade_no), (String) resultMap.get("transaction_id"));
                            // 给前端返回支付结果
                            result = new Result(true, "支付成功");
                        }
                    }
                }
                if (!result.getSuccess()) {
                    // 支付超时，删除缓存中的订单并回退商品库存
                    seckillOrderService.deleteOrderFromRedis(userId, Long.parseLong(out_trade_no));
                }
                break;
            }

        }
        return result;
    }

}
