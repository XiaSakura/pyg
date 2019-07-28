//控制层 
app.controller('cartController', function($scope, cartService, addressService) {

	// 读取列表数据绑定到表单中
	$scope.findCartList = function() {
		cartService.findCartList().success(function(response) {
			$scope.cartList = response;
			sum();
		});
	}

	$scope.addGoodsToCartList = function(itemid, num) {
		cartService.addGoodsToCartList(itemid, num).success(function(response) {
			if (response.success) {
				$scope.findCartList();
			} else {
				alert(response.message);
			}
		});
	}

	$scope.totalValue = {
		totalNum : 0,
		totalMoney : 0.00
	};// 合计实体 总数量和总金额

	// 求合计
	sum = function() {
		for (var i = 0; i < $scope.cartList.length; i++) {
			var cart = $scope.cartList[i];
			for (var j = 0; j < cart.orderItemList.length; j++) {
				var orderItem = cart.orderItemList[j];// 购物车明细
				$scope.totalValue.totalNum += orderItem.num;
				$scope.totalValue.totalMoney += orderItem.totalFee;
			}
		}
	}

	// 获取地址列表
	$scope.findAddressList = function() {
		addressService.findAddressList().success(function(response) {
			$scope.addressList = response;
			// 设置默认地址
			for (var i = 0; i < $scope.addressList.length; i++) {
				if ($scope.addressList[i].isDefault == '1') {
					$scope.address = $scope.addressList[i];
					break;
				}
			}
		});
	}

	$scope.selectAddress = function(address) {
		$scope.address = address;
	}

	$scope.isSelectAddress = function(address) {
		if ($scope.address.address == address) {
			return true;
		} else {
			return false;
		}
	}

	$scope.order = {
		paymentType : '1'
	}; // 订单对象
	
	// 选择支付方式
	$scope.selectPayType = function(type) {
		$scope.order.paymentType = type;
	}

	// 保存订单
	$scope.submitOrder = function() {
		console.log($scope.address);
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		
		cartService.submitOrder($scope.order).success(function(response) {
			if (response.success) {
				// 页面跳转
				if ($scope.order.paymentType == '1') {// 如果是微信支付，跳转到支付页面
					location.href = "pay.html";
				} else {// 如果货到付款，跳转到提示页面 先用这个代替
					location.href = "paysuccess.html";
				}
			} else {
				alert(response.message); // 也可以跳转到提示页面
			}
		});
	}

});
