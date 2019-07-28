var app = angular.module('pinyougou', []);

/* $sce服务写成过滤器  trustHtml这个名称可以自己定义*/
app.filter('trustHtml', [ '$sce', function($sce) {
	return function(data) { // data 传入被过滤的内容
		return $sce.trustAsHtml(data);
	}
} ]);