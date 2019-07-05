app.controller("contentController", function ($scope, contentService) {

    // 定义广告集合
    $scope.contentList = [];

    // 查询广告列表信息
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId] = response;
        })
    }

});