app.controller("payController", function ($scope, $location, payService) {

    // 本地支付
    $scope.createNative = function () {
        payService.createNative().success(function (response) {
            if (response != null) {
                $scope.money = (response.total_fee / 100).toFixed(2);   // 金额
                $scope.out_trade_no = response.out_trade_no;    // 订单号
                // 生成二维码
                var qr = new QRious({
                    element: document.getElementById("qrious"),
                    size: 300,
                    level: "H",
                    value: response.code_url
                });

                // 查询支付结果，并跳转页面
                queryPayStatus();
            }
        })
    };

    // 支付状态
    queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(function (response) {
            if (response.success) {
                location.href = "paysuccess.html#?money=" + $scope.money;
            } else {
                if (response.message == "二维码超时") {
                    alert(response.message);
                    location.href = "payfail.html";
                } else {
                    location.href = "payfail.html";
                }
            }
        })
    };

    // 支付成功页面显示支付金额
    $scope.getMoney = function () {
        return $location.search()["money"];
    }


});