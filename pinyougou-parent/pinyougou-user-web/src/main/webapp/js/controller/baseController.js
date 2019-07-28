app.controller('baseController', function($scope) {

	// 重新加载列表 数据
	$scope.reloadList = function() {
		// 切换页码
		$scope.search($scope.paginationConf.currentPage,
				$scope.paginationConf.itemsPerPage);
	}

	// 分页控件配置
	$scope.paginationConf = {
		currentPage : 1, // 当前页
		totalItems : 10, // 总条数 需要从后台获得
		itemsPerPage : 10, // 每页显示条数
		perPageOptions : [ 10, 20, 30, 40, 50 ],
		onChange : function() { // 点击页码的时候 触发
			$scope.reloadList();// 重新加载
		}
	};

	/*
	 * 我们需要定义一个用于存储选中ID的数组，当我们点击复选框后判断是选择还是取消选择，
	 * 如果是选择就加到数组中，如果是取消选择就从数组中移除。在点击删除按钮时需要用到这个存储了ID的数组。
	 */
	$scope.selectIds = []; // 选中的id集合

	// 点击选项框 更新数组
	$scope.updateSelection = function($event, id) {
		// 判断该选项框是否是选中状态
		if ($event.target.checked) {
			$scope.selectIds.push(id);
		} else {
			var index = $scope.selectIds.indexOf(id); // 获取位置
			$scope.selectIds.splice(index, 1);
		}
	}

	// 提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
	$scope.jsonToString = function(jsonString, key) {
		// 将字符串转换成对象
		var json = JSON.parse(jsonString);
		var value = "";
		for (var i = 0; i < json.length; i++) {
			if (i > 0) {
				value += ","
			}
			value += json[i][key];
		}
		return value;
	}

	$scope.searchObjectByKey = function(list, key, keyValue) {
		for (var i = 0; i < list.length; i++) {
			if (list[i][key] == keyValue) {
				return list[i];
			}
		}
		return null;
	}

});
