//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

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
    $scope.findOne = function () {
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }

        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                // 读取富文本编辑器内容
                editor.html($scope.entity.goodsDesc.introduction);
                // 读取图片列表
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                // 读取扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                // 格式化规格
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }

            }
        );
    };

    //保存
    $scope.save = function () {

        // 提取富文本编辑器中的内容
        $scope.entity.goodsDesc.introduction = editor.html();

        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    location.href = "goods.html";
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
    $scope.image_entity = {};
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
        if (newValue) {
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat2List = response;
            })
        }
    });
    // 三级分类下拉数据源
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat3List = response;
            })
        }
    });
    // 获取模板ID
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findOne(newValue).success(function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            })
        }
    });

    // 获取品牌下拉列表数据源
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oleValue) {
        var id = $location.search()['id'];
        if (newValue != null) {
            typeTemplateService.findOne(newValue).success(function (response) {
                $scope.typeTemplate = response; // 获取类型模板对象
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds); // 获取品牌列表
                if (id == null) {
                    // 获取扩展属性列表
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }
            });

            // 获取所有规格及其选项
            typeTemplateService.findSpecList(newValue).success(function (response) {
                $scope.specList = response;
            })
        }
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

    // 商品状态
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];

    // 商品分类列表
    $scope.itemCatList = [];
    // 加载商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                $scope.itemCatList[response[i].id] = response[i].name;
            }
        })
    };

    // 加载规格属性
    $scope.checkAttributeValue = function (keyValue, value) {

        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", keyValue);
        if (object == null) {
            return false;
        }
        var number = object.attributeValue.indexOf(value);
        if (number < 0) {
            return false;
        }
        return true;
    }

});
