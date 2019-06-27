app.service("brandService", function ($http) {
    // 增加品牌
    this.add = function (entity) {
        return $http.post("../brand/save.do", entity);
    };

    // 修改品牌
    this.findById = function (id) {
        return $http.get("../brand/findById.do?id=" + id);
    };
    this.update = function (entity) {
        return $http.post("../brand/updateBrand.do", entity);
    };

    // 删除品牌
    this.dele = function (ids) {
        return $http.post("../brand/deleteBrand.do", ids);
    };

    // 模糊查询加分页请求
    this.search = function (currentPage, pageSize, searchEntity) {
        return $http.post("../brand/findPage.do?currentPage=" + currentPage + "&pageSize=" + pageSize, searchEntity);
    };

    // 返回品牌信息json字符串[{id:1,text:“华为”},{},...]
    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do");
    }

});