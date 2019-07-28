//控制层 
app.controller('seckillGoodsController', function($scope, seckillGoodsService,
		$location, $interval) {

	// 读取列表数据绑定到表单中
	$scope.findList = function() {
		seckillGoodsService.findList().success(function(response) {
			$scope.list = response;
		});
	}

	// 查询实体
	$scope.findOne = function() {
		seckillGoodsService.findOneFromRedis($location.search()['id']).success(
				function(response) {
					$scope.entity = response;
					// 获取总秒数
					allSecond=Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);
					time=$interval(function(){
						if (allSecond>0) {
							allSecond=allSecond-1;
							// 变更时间 在页面上
							$scope.timeString=convertTimeString(allSecond); // 将秒转换成
																			// 时间字符串
						}else{
							 $interval.cancel(time);           
							  alert("当前秒杀商品已结束活动");
						}
					},1000);
					
				});
	}
	
	// 转换为 XXX天 10:22:33
	convertTimeString=function(allSecond){
		var days= Math.floor(allSecond/(60*60*24));// 天数
		var hours=Math.floor((allSecond-days*(60*60*24))/(60*60));// 小时
		var minutes=Math.floor((allSecond-days*(60*60*24)-hours*(60*60))/60);// 分钟
		var seconds= allSecond -days*60*60*24 - hours*60*60 -minutes*60; // 秒数
		var timeString="";
		if (days>0) {
			timeString=days+"天 ";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}
	
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
				function(response){
					if(response.success){
						alert("下单成功，请在1分钟内完成支付");
						location.href="pay.html";
					}else{
						alert(response.message);
					}
				}
		);  
	}

});
