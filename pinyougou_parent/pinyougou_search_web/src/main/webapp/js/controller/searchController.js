app.controller("searchController", function ($scope, searchService) {

    // 搜索条件的构建
    $scope.searchMap = {keywords: "", category: "", brand: "", spec: {}};

    // 搜索方法
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
        })
    };

    // 添加搜索条件
    $scope.addSearchItem = function (key, value) {
        if (key == "category" || key == "brand") {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    };

    // 删除搜索条件
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand") {
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

});