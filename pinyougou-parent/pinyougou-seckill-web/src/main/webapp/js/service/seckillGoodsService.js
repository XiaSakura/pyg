//服务层
app.service('seckillGoodsService',function($http){
	    	
	//购物车列表
	this.findList=function(){
		return $http.get('seckillGoods/findSeckillGoods.do');
	}
	
	this.findOneFromRedis=function(id){
		return $http.get('seckillGoods/findOneFromRedis.do?id='+id);
	}
	
	this.submitOrder=function(id){
		return $http.get('seckillGoods/submitOrder.do?id='+id);
	}
	
});
