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

    // 查询用户地址列表
    $scope.findAddressList = function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList = response;
            for (var i = 0; i < $scope.addressList.length; i++) {
                if ($scope.addressList[i].isDefault == '1') {
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }
        })
    };

    // 选择收获地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };
    // 判断address是否当前选中的地址
    $scope.isSelectedAddress = function (address) {
        if ($scope.address == address) {
            return true;
        } else {
            return false;
        }
    };

    // 定义订单对象
    $scope.order = {paymentType: "1"};

    // 支付方式
    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    };

    // 提交订单
    $scope.submitOrder = function () {

        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver = $scope.address.contact;//收货人

        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success) {
                if ($scope.order.paymentType == '1') {
                    location.href = "pay.html";
                } else {
                    location.href = "paysuccess.html";
                }
            } else {
                alert(response.message);
            }
        })
    }

});