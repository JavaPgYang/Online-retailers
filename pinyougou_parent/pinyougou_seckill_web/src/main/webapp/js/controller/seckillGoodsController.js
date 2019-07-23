app.controller("seckillGoodsController", function ($scope, $location, $interval, seckillGoodsService) {

    // 正在参与秒杀的商品
    $scope.findList = function () {
        seckillGoodsService.findList().success(function (reponse) {
            $scope.list = reponse;
        })
    };

    // 商品详细
    $scope.findOneFromRedis = function () {
        seckillGoodsService.findOneFromRedis($location.search()["id"]).success(function (response) {
            $scope.good = response;
            // 秒杀剩余时间总秒数
            allSecond = Math.floor((new Date($scope.good.endTime).getTime() - new Date().getTime()) / 1000);

            time = $interval(function () {
                if (allSecond > 0) {
                    allSecond = allSecond - 1;
                    $scope.timeString = convertTimeString(allSecond);
                } else {
                    $interval.cancel(time);
                    alert("秒杀服务已经结束。");
                }
            }, 1000);

        })
    };

    // $interval服务测试
    // 在 AngularJS 中$interval 服务用来间歇性处理一些事情
    // 格式为：$interval(执行的函数,间隔的毫秒数,运行次数);
    /*$scope.second = 5;
    time = $interval(function () {
        if ($scope.second > 0) {
            $scope.second = $scope.second - 1;
        } else {
            $interval.cancel(time); // 取消执行
            alert("秒杀服务已经结束。");
        }
        /!*if ($scope.second == 13) {
            $interval.cancel(time);
            alert("秒杀服务已经结束。")
        }*!/
    }, 1000);*/

    // 格式化时间字符串
    convertTimeString = function (allSecond) {
        var day = Math.floor(allSecond / (60 * 60 * 24)); // 天数
        var hour = Math.floor((allSecond - (day * 60 * 60 * 24)) / (60 * 60));    // 小时数
        var min = Math.floor((allSecond - ((day * 60 * 60 * 24) + (hour * 60 * 60))) / 60);     // 分钟数
        var sec = allSecond - ((day * 60 * 60 * 24) + (hour * 60 * 60) + (min * 60));       // 秒数

        var dayStr = "";
        if (day > 0) {
            dayStr = day + "天 ";
        }
        if (hour < 10) {
            dayStr += "0";
        }
        var zero1 = "";
        if (min < 10) {
            zero1 = "0";
        }
        var zero2 = "";
        if (sec < 10) {
            zero2 = "0";
        }
        return dayStr + hour + ":" + zero1 + min + ":" + zero2 + sec;
    };

    // 下单
    $scope.submitOrder = function () {
        seckillGoodsService.submitOrder($scope.good.id).success(function (response) {
            if (response.success) {
                alert(response.message);
                location.href = "pay.html";
            } else {
                alert(response.message);
            }
        })
    }


});