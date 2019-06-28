app.controller("indexController", function ($scope, loginService) {

    $scope.login = function () {
        loginService.login().success(function (data) {
            $scope.username = data.loginName;
        })
    }

});