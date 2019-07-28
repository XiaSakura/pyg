app.controller('indexController',
		function($scope, contentService, indexService) {

			$scope.contentList = []; // 广告集合
			$scope.contentCategoryList = [ 1, 2, 3, 4, 5 ]; // 广告分类
			// 1 首页轮播广告
			// 2 今日推荐
			// 3 活动专区
			// 4 猜你喜欢
			// 5 服装楼层广告

			$scope.findAllCategory = function() {
				/*
				 * for (var i = 0; i < $scope.contentCategoryList.length; i++) {
				 * $scope.findByCategoryId($scope.contentCategoryList[i]); }
				 */
				$scope.findByCategoryId($scope.contentCategoryList[0]);
			}
			$scope.findByCategoryId = function(categoryId) {
				contentService.findByCategoryId(categoryId).success(
						function(response) {
							$scope.contentList[categoryId] = response;
						})
			}
			// 搜索跳转
			$scope.search = function() {
				if ($scope.keywords==undefined) {
					alert("请输入关键字");
					return ;
				}
				location.href = "http://localhost:9104/search.html#?keywords="
						+ $scope.keywords;
			}

			// 定义json集合 用于存放item分类
			$scope.itemCatList = [ {} ];

			$scope.findAllItemCat = function() {
				indexService.findAllItemCat().success(function(response) {
					$scope.itemCatList = response;
				})
			}
			
			$scope.addKeyWords=function(keywords){
				$scope.keywords=keywords;
				$scope.search();
			}

		});
