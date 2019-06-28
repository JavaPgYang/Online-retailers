app.service("loginService", function ($http) {
    // 登录用户
    this.getLoginName = function () {
        return $http.get("../login/name.do");
    }
});