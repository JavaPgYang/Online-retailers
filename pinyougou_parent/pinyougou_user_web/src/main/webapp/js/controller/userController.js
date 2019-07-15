//控制层
app.controller('userController', function ($scope, userService) {

    // 注册用户
    $scope.registered = function () {
        // 比较两次输入的密码是否一致
        if ($scope.password != $scope.entity.password) {
            alert("您两次输入的密码不一致，请重新输入");
            $scope.password = "";
            $scope.entity.password = "";
            return;
        }

        userService.add($scope.entity, $scope.checkCode).success(function (response) {
            alert(response.message);
            $scope.entity = {};
            $scope.password = "";
            $scope.checkCode = "";
        })
    };

    // 发送验证码
    $scope.sendCode = function () {

        if ($scope.entity.phone == null || $scope.entity.phone == "") {
            alert("请填写手机号码！");
            return;
        }

        // 定义手机号码验证格式
        var res_telephone = new RegExp("^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$");

        if (!res_telephone.test($scope.entity.phone)) {
            alert("请填写正确的手机号码！");
            return;
        }

        userService.sendCode($scope.entity.phone).success(function (response) {
            alert(response.message);
        })
    }
});
