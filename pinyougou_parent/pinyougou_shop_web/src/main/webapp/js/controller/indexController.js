app.controller("loginController", function ($scope, loginService) {

    // 登录用户
    $scope.getLoginName = function () {
        loginService.getLoginName().success(function (response) {
            $scope.loginName = response.loginName;
        })
    }

});