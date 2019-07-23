app.service("seckillGoodsService", function ($http) {
    // 正在参与秒杀的商品
    this.findList = function () {
        return $http.get("/seckillGoods/findList.do");
    };

    // 商品详细
    this.findOneFromRedis = function (id) {
        return $http.get("/seckillGoods/findOneFromRedis.do?id=" + id);
    };

    // 下单
    this.submitOrder = function (seckillGoodId) {
        return $http.get("/seckillOrder/submitOrder.do?seckillGoodId=" + seckillGoodId);
    }
});