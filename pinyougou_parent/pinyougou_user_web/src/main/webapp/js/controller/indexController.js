app.controller('indexController', function ($scope, loginService) {

    // 获取登录用户名
    $scope.showName = function () {
        loginService.login().success(function (respose) {
            $scope.loginName = respose.loginName;
        })
    }

});