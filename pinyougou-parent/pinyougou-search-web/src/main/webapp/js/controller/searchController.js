app.controller('searchController', function($scope, $controller, itemService,$location) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	$scope.search = function() {
		$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
		itemService.search($scope.searchMap).success(function(response) {
			$scope.resultMap = response;// 搜索返回的结果
			buildPageLable(); // 构建分页条
		});
	}

	$scope.searchMap = {
		'keywords' : '',
		'category' : '',
		'brand' : '',
		'spec' : {},
		'price' : '',
		'pageNo' : 1,
		'pageSize' : 10,
		'sort':'',
		'sortField':''
	};// 搜索对象
	// 添加搜索项
	$scope.addSearchItem = function(key, value) {
		if (key == 'category' || key == 'brand' || key == 'price') {// 如果点击的是分类或者是品牌
			$scope.searchMap[key] = value;
		} else {
			// 因为规格可能有多个
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();// 执行搜索

	}
	// 移除复合搜索条件
	$scope.removeSearchItem = function(key) {
		if (key == "category" || key == "brand" || key == 'price') {// 如果是分类或品牌
			$scope.searchMap[key] = "";
		} else {// 否则是规格
			delete $scope.searchMap.spec[key];// 移除此属性
		}
		$scope.search();// 执行搜索
	}

	// 构建分页栏
	buildPageLable = function() {
		$scope.pageLabel = [];// 新增分页栏属性 就是1 2 3 4 页码
		var maxPageNo = $scope.resultMap.totalPages;// 得到最后页码
		var firstPage = 1;// 开始页码
		var lastPage = maxPageNo;// 截止页码
		$scope.firstDot = true;// 前面有点
		$scope.lastDot = true;// 后边有点
		// 如果总页数大于5页,显示部分页码 防止页数不足5页 但是如果小于5的话 可以不用管 直接显示就可以了
		if ($scope.resultMap.totalPages >= 5) {
			// 判断当前页是否小于等于3 则只显示前五页
			if ($scope.searchMap.pageNo <= 3) {
				lastPage = 5;
				$scope.firstDot = false;// 前面没点
			} else if ($scope.searchMap.pageNo >= maxPageNo - 2) { // 如果当前页大于等于最大页码-2
				// 显示后五页
				firstPage = maxPageNo - 4;
				$scope.lastDot = false;// 后边没点
			} else { // 显示当前页为中心的5页
				firstPage = $scope.searchMap.pageNo - 2;
				lastPage = $scope.searchMap.pageNo + 2;
			}
		} else {
			// 如果总页数小于5 只显示前五页
			$scope.firstDot = false;// 前面无点
			$scope.lastDot = false;// 后边无点
		}
		// 循环产生页码标签
		for (var i = firstPage; i <= lastPage; i++) {
			$scope.pageLabel.push(i);
		}

	}

	// 根据页面查询
	$scope.queryPage = function(pageNo) {
		// 页码验证
		if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
			return;
		}
		$scope.searchMap.pageNo = pageNo;
		$scope.search();
	}

	// 判断当前页为第一页
	$scope.isTopPage = function() {
		if ($scope.searchMap.pageNo == 1) {
			return true;
		} else {
			return false;
		}
	}
	// 判断当前页是否未最后一页
	$scope.isEndPage = function() {
		if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
			return true;
		} else {
			return false;
		}
	}
	
	//设置排序规则
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;    
		$scope.searchMap.sort=sort;    
		$scope.search();
	}
	
	//判断关键字是不是品牌 如果是的话 需要将品牌隐藏掉
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0) {
				return true;
			}
		}
		return false;
	}
	
	//加载查询字符串
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=  $location.search()['keywords'];
		$scope.search();
	}
	
	


	

});
