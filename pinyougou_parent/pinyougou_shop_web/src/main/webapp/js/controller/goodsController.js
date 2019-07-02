//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, uploadService, itemCatService, typeTemplateService) {

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

    $scope.entity = {goodsDesc: {itemImages: [], specificationItems: []}};
    // 添加图片列表数据
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    };
    // 删除图片列表记录
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    };

    // 一级分类下拉数据源
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        })
    };
    // 二级分类下拉数据源
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat2List = response;
        })
    });
    // 三级分类下拉数据源
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List = response;
        })
    });
    // 获取模板ID
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;
        })
    });

    // 获取品牌下拉列表数据源
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oleValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            $scope.typeTemplate = response; // 获取类型模板对象
            $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds); // 获取品牌列表
            // 获取扩展属性列表
            $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
        });

        // 获取所有规格及其选项
        typeTemplateService.findSpecList(newValue).success(function (response) {
            $scope.specList = response;
        })
    });

    // 更新entity.goodsDesc.specificationItems集合的内容
    $scope.updateSpecAttribute = function ($event, name, value) {

        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }

    };

    // 创建sku列表
    $scope.createItemList = function () {
        // 初始化itemList
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: 0, isDefault: 0}];

        // 获取数据集合（规格及其选项）
        var specificationItems = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < specificationItems.length; i++) {
            // 获得每个规格
            var column = specificationItems[i].attributeName;
            // 获得每个规格选项集合
            var columnValue = specificationItems[i].attributeValue;

            // 进行深克隆
            var newList = [];
            for (let j = 0; j < $scope.entity.itemList.length; j++) {
                var oldRow = $scope.entity.itemList[j];

                for (var k = 0; k < columnValue.length; k++) {
                    var newRow = JSON.parse(JSON.stringify(oldRow));
                    newRow.spec[column] = columnValue[k];
                    newList.push(newRow);
                }
            }
            $scope.entity.itemList = newList;
        }
    };

});
