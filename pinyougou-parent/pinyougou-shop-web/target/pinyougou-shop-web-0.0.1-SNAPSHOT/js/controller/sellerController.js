//控制层 
app.controller('sellerController',
		function($scope, $controller, sellerService) {

			$controller('baseController', {
				$scope : $scope
			});// 继承

			// 读取列表数据绑定到表单中
			$scope.findAll = function() {
				sellerService.findAll().success(function(response) {
					$scope.list = response;
				});
			}

			// 分页
			$scope.findPage = function(page, rows) {
				sellerService.findPage(page, rows).success(function(response) {
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;// 更新总记录数
				});
			}

			// 查询实体
			$scope.findOne = function(id) {
				sellerService.findOne(id).success(function(response) {
					$scope.entity = response;
				});
			}

			// 保存
			$scope.save = function() {
				var serviceObject;// 服务层对象
				if ($scope.entity.sellerId != null) {// 如果有ID
					serviceObject = sellerService.update($scope.entity); // 修改
				} else {
					serviceObject = sellerService.add($scope.entity);// 增加
				}
				serviceObject.success(function(response) {
					if (response.success) {
						// 重新查询
						$scope.reloadList();// 重新加载
					} else {
						alert(response.message);
					}
				});
			}

			// 批量删除
			$scope.dele = function() {
				// 获取选中的复选框
				sellerService.dele($scope.selectIds).success(
						function(response) {
							if (response.success) {
								$scope.reloadList();// 刷新列表
								$scope.selectIds = [];
							}
						});
			}

			$scope.searchEntity = {};// 定义搜索对象

			// 搜索
			$scope.search = function(page, rows) {
				sellerService.search(page, rows, $scope.searchEntity).success(
						function(response) {
							$scope.list = response.rows;
							$scope.paginationConf.totalItems = response.total;// 更新总记录数
						});
			}

			// 添加一个 商家 和上面的save方法区别开来
			$scope.add = function() {
				sellerService.add($scope.entity).success(function(response) {
					if (response.success) {
						location.href = 'shoplogin.html';
					} else {
						alert(response.message);
					}
				})
			}

			$scope.getSellerInfo = function() {
				sellerService.getSellerInfo().success(function(response) {
					$scope.entity = response;
				})
			}

			$scope.updatePassword = function() {
				// 判断两次输入的密码是否相同
				if ($scope.newPassword != $scope.secPassword) {
					alert("两次输入的密码不一致");
					return;
				}
				sellerService.updatePassword($scope.password,$scope.newPassword).success(
					function(response) {
					alert(response.message);
				});
			}
		});
