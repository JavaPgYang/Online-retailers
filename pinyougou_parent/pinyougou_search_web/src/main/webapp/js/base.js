var app = angular.module("pinyougou", []);

// $sce服务写成过滤器
app.filter("trustHtml",function ($sce) {

    return function (data) {
        return $sce.trustAsHtml(data);
    }

});