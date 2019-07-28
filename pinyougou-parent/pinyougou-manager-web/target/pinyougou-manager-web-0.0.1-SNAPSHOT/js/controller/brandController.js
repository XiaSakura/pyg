
app.controller('brandController', function($scope, $controller, brandService) {
	$controller('baseController', {
		$scope : $scope
	}); // 继承
	$scope.findAll = function() {
		brandService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	$scope.findPage = function(page, rows) {
		brandService.findPage(page, rows).success(function(res) {
			$scope.list = res.rows;
			$scope.paginationConf.totalItems = res.total;
		})
	}
	$scope.entity = {};

	// 更新或者保存
	$scope.save = function() {

		var methodName = "add"; // 默认运行方法
		if ($scope.entity.id != null) {
			methodName = "update";
		}

		brandService.save(methodName, $scope.entity).success(function(res) {
			if (res.success) {
				// 重新查询
				$scope.reloadList();// 重新加载
			} else {
				alert(res.message);
			}
		})
	}

	$scope.findOne = function(id) {
		brandService.findOne(id).success(function(res) {
			$scope.entity = res;
		})
	}

	$scope.dele = function() {
		brandService.dele($scope.selectIds).success(function(res) {
			if (res.success) {
				// 重新查询
				$scope.reloadList();// 重新加载
			} else {
				alert(res.message);
			}
		})
	}

	$scope.searchEntity = {};

	$scope.search = function(page, rows) {
		brandService.search(page, rows, $scope.searchEntity).success(
				function(response) {
					$scope.paginationConf.totalItems = response.total;// 总记录数
					$scope.list = response.rows;// 给列表变量赋值
				});
	}

});
