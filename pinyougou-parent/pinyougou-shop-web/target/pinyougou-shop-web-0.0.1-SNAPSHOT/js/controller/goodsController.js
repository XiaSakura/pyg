//控制层 
app.controller('goodsController', function($scope, $controller, goodsService,
		uploadService, itemCatService, typeTemplateService,$location) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		goodsService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, rows) {
		goodsService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询实体
	$scope.findOne = function(id) {
		goodsService.findOne(id).success(function(response) {
			$scope.entity = response;
		});
	}
	
	// 保存
	$scope.save = function() {
		//提取文本编辑器的值
		$scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;// 服务层对象
		if ($scope.entity.id != null) {// 如果有ID
			serviceObject = goodsService.update($scope.entity); // 修改
		} else {
			serviceObject = goodsService.add($scope.entity);// 增加
		}
		serviceObject.success(function(response) {
			if (response.success) {
				alert('保存成功');
				$scope.entity = {};
				editor.html('');// 清空富文本编辑器
				location.href="goods.html";//跳转到商品列表页
			} else {
				alert(response.message);
			}
		});
	}

	// 批量删除
	$scope.dele = function() {
		// 获取选中的复选框
		goodsService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}
		});
	}

	$scope.searchEntity = {};// 定义搜索对象

	// 搜索
	$scope.search = function(page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(
				function(response) {
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;// 更新总记录数
				});
	}

	$scope.uploadFile = function() {
		uploadService.uploadFile().success(function(response) {
			if (response.success) {// 如果上传成功，取出url
				$scope.image_entity.url = response.message;// 设置文件地址,上传之后我们可以预览图片

			} else {
				alert(response.message);
			}
		}).error(function() {
			alert("上传发生错误");
		});
	}

	$scope.entity = {
		goods : {
			category1Id : null,
			category2Id : null,
			category3Id : null
		},
		goodsDesc : {
			itemImages : [],
			specificationItems:[]
		}
	}; // 定义页面实体结构
	$scope.image_entity = {}; // 保存页面图片内容实体 就是上传框里面的内容 我们需要把这个实体存放到 itemImages
	// 里面
	$scope.add_image_entity = function() {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	$scope.remove_image_entity = function(id, index) {
		$scope.entity.goodsDesc.itemImages.splice(index, 1);
	}
	// 定义三个变量 用于 保存下拉选择框
	$scope.itemCat1List = {};
	$scope.itemCat2List = {};
	$scope.itemCat3List = {};
	// 读取一级分类
	$scope.selectItemCat1List = function() {
		itemCatService.search(1, 100, {
			parentId : 0
		}).success(function(response) {
			$scope.itemCat1List = response.rows;
		});
	}
	// 监控 category1Id 发生改变时
	$scope.$watch('entity.goods.category1Id', function(newValue, oldValue) {
		// 根据选择的值，查询二级分类
		if ($scope.entity.goods.category1Id != null) {
			itemCatService.search(1, 100, {
				parentId : newValue
			}).success(function(response) {
				$scope.itemCat2List = response.rows;
			});
		}
	})

	// 监控 category2Id 发生改变时
	$scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
		// 根据选择的值，查询二级分类
		if ($scope.entity.goods.category2Id != null) {
			itemCatService.search(1, 100, {
				parentId : newValue
			}).success(function(response) {
				$scope.itemCat3List = response.rows;
			});
		}

	})
	// 监控 category3Id 发生改变时 生成模板id
	$scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
		// 根据选择的值，查询模板id
		if ($scope.entity.goods.category3Id != null) {
			itemCatService.findOne(newValue).success(function(response) {
				$scope.entity.goods.typeTemplateId = response.typeId
			});
		}
	})
	
	$scope.typeTemplate={brandIds:[],specList:[],customAttributeItems:[]};
	
	// 监控 typeTemplateId 发生改变时 获取规格列表 品牌列表 和扩展列表
	$scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
		if ($scope.entity.goods.typeTemplateId != null) {
			typeTemplateService.findOne(newValue).success(
					function(response) {
						$scope.typeTemplate = response;// 获取类型模板
						$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);// 品牌列表
						//如果没有ID，则加载模板中的扩展数据
						if($location.search()['id']==null){
							$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性    
						}
					})
			typeTemplateService.findSpecListByTemplateId(newValue).success(
					function(response){
						$scope.typeTemplate.specList=response;
					}
			)
		}
	})
	
	$scope.updateSpecAttribute=function($event,name,value){
		//进行判断 $scope.entity.goodsDesc.specificationItems里面是否存在该规格名称
		var object= $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems ,'attributeName', name);
		//当集合里面存在该选项的时候
		if (object!=null) {
			//当被勾选的时候
			if ($event.target.checked) {
				object.attributeValue.push(value); 
			}else{ //当去掉勾选的时候
				//获取需要去掉的位置
				var index=object.attributeValue.indexOf(value);
				object.attributeValue.splice(index,1);
				//如果选项都取消了，将此条记录移除
				if(object.attributeValue.length==0){
					var index2=$scope.entity.goodsDesc.specificationItems.indexOf(object);
					$scope.entity.goodsDesc.specificationItems.splice(index2,1);
				}         
			}
		}else{
			//当集合里面不存在该选项的时候
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	//itemList这个来自组合实体类 itemList SKU列表
	//创建SKU列表 将用户选择的规格 深克隆到itemList里面
	//看懂这个需要 自己理解
	$scope.createItemList=function(){
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//初始值  然后对这个值进行层层的深克隆
		var items=$scope.entity.goodsDesc.specificationItems; //得到用户选择的规格
		for (var i = 0; i < items.length; i++) {
			$scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}
	
	//添加列值 
	addColumn=function(list,columnName,columnValues){
		var newList=[]; //新的集合
		for (var i = 0; i < list.length; i++) {
			//旧的一行 等下要用到深克隆 克隆这个
			var oldRow=list[i];
			//注意columnValues是一个数组 所以需要遍历 
			//而且每一个
			for (var j = 0; j < columnValues.length; j++) {
				var newRow= JSON.parse( JSON.stringify( oldRow )  );//深克隆
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		
		return newList;
		
	}
	
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	
	$scope.marketable=['已下架(暂停销售)','已上架(正常销售)'];//商品状态
	$scope.itemCatList=[];//商品分类列表
	
	//用ID去查询后端，异步返回商品分类名称。
	$scope.findItemCatList=function(){        
		itemCatService.findAll().success(
				function(response){                           
					for(var i=0;i<response.length;i++){
						$scope.itemCatList[response[i].id]=response[i].name;
					}
				}
		);
	}
	

	//这个是为了在goods.html跳转到goods_edit.html
	//或者地址栏的id 使用$location服务 回显数据
	$scope.findGoodsById=function(){
		//获取id
		var id=$location.search()['id'];
		//如果不是跳转过来的 
		if (id==null) {
			return ;
		}
		goodsService.findGoodsById(id).success(
				function(response){
						//回显用户修改数据
						$scope.entity = response;
						editor.html($scope.entity.goodsDesc.introduction); 	//向富文本编辑器添加商品介绍
						//回显 图片列表 先转成json
						$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
						//显示扩展属性
						$scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems); 
						//规格           
						$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
						//SKU列表规格列转换   
						for (var i = 0; i < $scope.entity.itemList.length; i++) {
							$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
						}
				}
		)	
		
	}
	
	//检测 该选项是否已经被勾选 从$scope.entity.goodsDesc.specificationItems 判断
	$scope.checkOption=function(specName,optionName){
		var items=$scope.entity.goodsDesc.specificationItems;
		//先判断规格是否存在
		var object=$scope.searchObjectByKey(items,"attributeName",specName);
		if (object==null) {
			return false;
		}else{
			//还需要判断选项是否存在
			if (object.attributeValue.indexOf(optionName)>=0) {
				return true;
			}else{
				return false;
			}
		}
	}
	
	// 批量上下架
	$scope.updateGoodsMarketable = function(status) {
		// 获取选中的复选框
		goodsService.updateGoodsMarketable($scope.selectIds,status).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}else{
				alert(response.message);
			}
		});
	}

	

	
	
	
	
	
	

});
