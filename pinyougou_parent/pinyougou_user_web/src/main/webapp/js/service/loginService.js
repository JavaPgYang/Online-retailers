app.service('loginService', function ($http) {

    // 获取登录用户名
    this.login = function () {
        return $http.get("../login/login.do");
    };

});