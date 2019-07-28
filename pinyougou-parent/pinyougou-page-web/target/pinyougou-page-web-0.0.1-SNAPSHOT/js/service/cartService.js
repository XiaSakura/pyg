//服务层
app.service('cartService',function($http){
	    	
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+itemId+"&num="+num,{'withCredentials':true});
	}

	

});
