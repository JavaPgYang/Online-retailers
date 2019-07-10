app.controller("searchController", function ($scope, $location, searchService) {

    // 搜索条件的构建
    $scope.searchMap = {
        keywords: "",
        category: "",
        brand: "",
        spec: {},
        price: "",
        pageNo: 1,
        pageSize: 40,
        sort: "",   // 排序方式（升序或降序）
        sortFiled: ""   // 排序字段（按照哪个字段排序）
    };

    // 搜索方法
    $scope.search = function () {
        // 查询前将当前页转换为int型
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            buildPageLabel();
        })
    };

    // 添加搜索条件
    $scope.addSearchItem = function (key, value) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    };

    // 删除搜索条件
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    };

    // 构建分页栏
    buildPageLabel = function () {
        // 定义分页页码集合
        $scope.pageLabel = [];
        // 拿到总页码
        var maxPageNo = $scope.resultMap.totalPages;
        // 定义开始页码
        var startPage = 1;
        // 定义结束页码
        var stopPage = maxPageNo;
        // 显示前置省略号
        $scope.firstDot = true;
        // 显示后置省略号
        $scope.lastDot = true;
        // 当总页码大于5页走以下逻辑
        if (maxPageNo > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                stopPage = 5;
                // 当前页小于3时，隐藏前置省略号
                $scope.firstDot = false;
            } else if ($scope.searchMap.pageNo >= maxPageNo - 2) {
                startPage = maxPageNo - 4;
                // 当前页大于总页码-2时，隐藏后置省略号
                $scope.lastDot = false;
            } else {
                startPage = $scope.searchMap.pageNo - 2;
                stopPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            // 隐藏前置省略号
            $scope.firstDot = false;
            // 隐藏后置省略号
            $scope.lastDot = false;
        }
        for (var i = startPage; i <= stopPage; i++) {
            $scope.pageLabel.push(i);
        }
    };

    // 提交查询
    $scope.queryByPage = function (num) {
        if (num < 1) {
            num = 1;
        }
        if (num > $scope.resultMap.totalPages) {
            num = $scope.resultMap.totalPages;
        }
        $scope.searchMap.pageNo = num;
        $scope.search();
    };

    // 排序
    $scope.sortSearch = function (sortFiled, sort) {
        $scope.searchMap.sortFiled = sortFiled;
        $scope.searchMap.sort = sort;
        $scope.search();
    };

    // 当用户搜索的品牌时，隐藏品牌选项
    $scope.keywordsIsBrand = function () {
        var index = -1;
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            // 关键词在品牌列表某个品牌字符串中的索引
            index = $scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text);
            // alert(index);
            if (index >= 0) {
                // 索引存在
                return true;
            }
        }
        return false;
    };

    // 接收首页传递的keywords
    $scope.loadKeywords = function () {
        $scope.searchMap.keywords = $location.search()["keywords"];
        $scope.search();
    }

});