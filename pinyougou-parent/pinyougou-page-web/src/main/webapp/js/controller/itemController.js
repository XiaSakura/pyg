 //控制层 
app.controller('itemController' ,function($scope,cartService){	
	
	//数量操作
	$scope.addNum=function(x){
		$scope.num=$scope.num+x;
		if($scope.num<1){
			$scope.num=1;
		}
	}        
    
	$scope.specificationItems=[]; //记录用户选择的规格
	//用户选择规格
	$scope.selectSpecification=function(name,value){
		$scope.specificationItems[name]=value;
		searchSku();
	}
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}        
	}
	
	//加载默认的sku 页面初始化的时候加载
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec)); //使规格选项被选中 深克隆
	}
	
	//匹配两个对象 是否相同
	matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		//但是还有种情况 如果map1的键值对比map2少 那这两个不是同一个对象 因此还需要循环map2
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}            
		}
		return true; 
	}
	
	//查询SKU 对比skuList的spec 和specificationItems 当前选择的规格 是否有匹配的
	//如果一致的话 就可以获取到价格和标题 如果不同的话 就赋予一个默认的
	searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec,$scope.specificationItems)){
				$scope.sku=skuList[i];
				return ;
			}
		}
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的 
	}
	
	//添加商品到购物车
	$scope.addToCart=function(){
		cartService.addGoodsToCartList( $scope.sku.id,$scope.num).success(
				function(response){
					 if(response.success){
						 location.href='http://localhost:9107/cart.html';//跳转到购物车页面
					 }else{
						 alert(response.message);
					 }
				}
		)
	  
	}
	
	
	
});	
