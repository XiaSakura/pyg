//服务层
app.service('loginService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../login/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../login/findPage.do?page='+page+'&rows='+rows);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../login/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity,code){
		return  $http.post('../login/add.do?code='+code,entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../login/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../login/delete.do?ids='+ids);
	}
	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../login/search.do?page='+page+"&rows="+rows, searchEntity);
	}    	
	this.showName=function(){
		return $http.get('../login/showName.do');
	}

});
