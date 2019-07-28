//服务层
app.service('addressService',function($http){
	    	
	//获取地址列表
	this.findAddressList=function(){
		return $http.get('/address/findAddressListByUser.do');    
	}

});
