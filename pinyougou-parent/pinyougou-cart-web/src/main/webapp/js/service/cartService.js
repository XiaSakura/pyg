//服务层
app.service('cartService',function($http){
	    	
	//购物车列表
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');        
	}
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+"&num="+num);
	}
	
	this.submitOrder=function(order){
		return $http.post('order/add.do',order);
	}

});
