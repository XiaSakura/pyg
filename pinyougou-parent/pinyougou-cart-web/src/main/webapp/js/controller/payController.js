//控制层 
app.controller('payController', function($scope, payService,$location) {

	// 本地生成二维码
	$scope.createNative = function() {
		payService.createNative().success(function(response) {
			$scope.money=  (response.total_fee/100).toFixed(2);  // 金额
																	// 注意total_fee是分为单位
																	// 所以要/100
			$scope.out_trade_no= response.out_trade_no;// 订单号
			$scope.url="https://www.bilibili.com/";
			// response.code_url
			// 二维码
			var qr=new QRious({
				element:document.getElementById('qrious'),
				size:250,
				level:"H",
				value:$scope.url // 这里改成 response.code_url 如果签名正确了的话
			})
			queryPayStatus($scope.out_trade_no);
		})

	}
	
	// 查询支付状态
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
			function(response){
				if(response.success){
					location.href="paysuccess.html#?money="+$scope.money;
				}else{             
					if(response.message=='二维码超时'){
						$scope.createNative();// 重新生成二维码
					}else{
						location.href="payfail.html";
					}                         
				}                
			}
		);
	}
	
	//获取金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}


});
