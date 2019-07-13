// 商品详细页（控制层）
app.controller("itemPageController", function ($scope) {

    // 下单数量初始化
    $scope.num = 1;
    // 规格选择变量初始化
    $scope.specificationItems = {};

    // 下单数量增减
    $scope.addNum = function (x) {
        $scope.num += x;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    };

    // 规格选择
    $scope.selectSpecification = function (key, value) {
        $scope.specificationItems[key] = value;
        searchSku();
    };

    // 规格是否选中
    $scope.isSelect = function (key, value) {
        if ($scope.specificationItems[key] == value) {
            return true;
        }
        return false;
    };

    // 加载默认SKU
    $scope.loadSku = function () {
        $scope.defaultSku = itemList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.defaultSku.spec));
    };

    // 比较两个对象是否相等
    matchObject = function (ob1, ob2) {
        for (var key in ob1) {
            if (ob1[key] != ob2[key]) {
                return false;
            }
        }
        for (var key in ob2) {
            if (ob2[key] != ob1[key]) {
                return false;
            }
        }
        return true;
    };

    // 查询SKU
    searchSku = function () {
        for (var i = 0; i < itemList.length; i++) {
            var spec = itemList[i].spec;
            if (matchObject(spec, $scope.specificationItems)) {
                $scope.defaultSku = itemList[i];
                return;
            }
        }
        $scope.defaultSku = {"id": "---", "title": "---", "price": "---", "spec": "---"};
    };

    // 购物车
    $scope.addToCart = function () {
        alert($scope.defaultSku.id);
    }

});