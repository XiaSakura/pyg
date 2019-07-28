app.service('uploadService', function($http) {
	/*
	 * anjularjs对于post和get请求默认的Content-Type header
	 * 是application/json。通过设置‘Content-Type’: undefined，这样浏览器会帮我们把Content-Type
	 * 设置为 multipart/form-data. 通过设置
	 * 
	 * transformRequest: angular.identity ，anjularjs transformRequest function
	 * 将序列化我们的formdata object. 对表单进行二进制序列化
	 */
	this.uploadFile = function() {
		// 先定义formData对象 获取文件上传对象
		var formData = new FormData();
		formData.append("file", file.files[0]);
		return $http({
			method : "post",
			url : "../upload.do",
			data : formData,
			headers : {
				'Content-Type' : undefined
			},
			transformRequest : angular.identity
		});
	}

});
