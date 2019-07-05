app.controller("baseController", function ($scope) {
    // 品牌分页开始
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10, // 总记录数
        itemsPerPage: 10,   // 每页记录数
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();
        }
    };

    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    /*$scope.findByPage = function (currentPage, pageSize) {
        $http.get("../brand/findByPage.do?currentPage=" + currentPage + "&pageSize=" + pageSize).success(function (pageInfo) {
            $scope.list = pageInfo.list;
            $scope.paginationConf.totalItems = pageInfo.total;
        });
    };*/
    // 品牌分页结束

    // 更新选中id
    $scope.selectIds = [];
    $scope.readId = function (id, $event) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1);
        }
    };

    // JSON内容精简提取
    $scope.jsonToString = function (jsonStr, key) {
        var json = JSON.parse(jsonStr);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ",";
            }
            value += json[i][key];
        }
        return value;
    };

    // 从集合中按照key查询对象
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }

});