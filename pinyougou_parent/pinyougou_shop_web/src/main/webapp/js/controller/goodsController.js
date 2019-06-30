//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, uploadService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //查询实体
    $scope.findOne = function (id) {
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    };

    //保存
    $scope.add = function () {

        // 提取富文本编辑器中的内容
        $scope.entity.goodsDesc.introduction = editor.html();

        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    //重新查询
                    alert(response.message);
                    $scope.entity = {}; // 保存成功后清空组合对象
                    editor.html(""); // 保存成功后清空富文本编辑器
                } else {
                    alert(response.message);
                }
            }
        );
    };


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    // 上传文件
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {
                $scope.image_entity.url = response.message;
            } else {
                alert(response.message);
            }
        }).error(function () {
            alert("上传发生错误");
        });
    };

    $scope.entity = {goodsDesc: {itemImages: []}};
    // 添加图片列表数据
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    };
    // 删除图片列表记录
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    };

});


    NetSarang_AIO_8in1_Keygen_v1.5_DFoX