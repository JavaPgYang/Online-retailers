//购物车控制层
app.controller('cartController', function ($scope, cartService) {
    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);
            }
        );
    };

    // 添加商品数量
    $scope.addGoodsToCartList = function (itemID, num) {
        cartService.addGoodsToCartList(itemID, num).success(function (response) {
            if (response.success) {
                $scope.findCartList();
            } else {
                alert(response.message);
            }
        })
    };
});