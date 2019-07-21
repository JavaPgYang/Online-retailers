//购物车服务层
app.service('cartService', function ($http) {
    //购物车列表
    this.findCartList = function () {
        return $http.get('/cart/findCartList.do');
    };

    // 添加商品数量
    this.addGoodsToCartList = function (itemID, num) {
        return $http.get("/cart/addGoodsToCartList.do?itemID=" + itemID + "&num=" + num);
    };

    // 计算合计（总商品数，总价钱）
    this.sum = function (cartList) {
        var totalValue = {totalNum: 0, totalMoney: 0};

        for (var i = 0; i < cartList.length; i++) {
            for (var j = 0; j < cartList[i].orderItemList.length; j++) {
                totalValue.totalNum += cartList[i].orderItemList[j].num;
                totalValue.totalMoney += cartList[i].orderItemList[j].totalFee;
            }
        }
        return totalValue;
    };

    // 查询用户地址列表
    this.findAddressList = function () {
        return $http.get("/address/findListByLoginUser.do");
    };

    // 提交订单
    this.submitOrder = function (order) {
        return $http.post("/order/add.do", order);
    }
});