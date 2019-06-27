app.controller("myController", function ($scope, $http, brandService, $controller) {

    // 继承baseController
    $controller("baseController", {$scope: $scope});

    // 展示所有品牌
    /*$scope.getBrandList = function () {
        $http.get("../brand/findAll.do").success(function (data) {
            $scope.brandList = data;
        });
    };*/

    // 添加及更新品牌信息
    $scope.save = function () {
        var serviceObject;
        if ($scope.entity.id != null) {
            serviceObject = brandService.update($scope.entity);
        } else {
            serviceObject = brandService.add($scope.entity);
        }
        serviceObject.success(function (data) {
            if (!data.success) {
                alert(data.message);
            } else {
                $scope.reloadList();
            }
        })
    };

    // 修改品牌信息
    $scope.findById = function (id) {
        brandService.findById(id).success(function (data) {
            $scope.entity = data;
        })
    };

    // 删除选中的品牌
    $scope.dele = function () {
        brandService.dele($scope.ids).success(function (data) {
            if (data.success) {
                $scope.reloadList();
            } else {
                alert(data.message)
            }
        })
    };

    // 模糊查询加分页
    $scope.searchEntity = {};
    $scope.search = function (currentPage, pageSize) {
        brandService.search(currentPage, pageSize, $scope.searchEntity).success(function (data) {
            $scope.list = data.list;
            $scope.paginationConf.totalItems = data.total;
        })
    }

});