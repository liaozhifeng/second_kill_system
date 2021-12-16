//展示loading
function g_showLoading(){
	var idx = layer.msg('处理中...', {icon: 16,shade: [0.5, '#f5f5f5'],scrollbar: false,offset: '0px', time:100000}) ;  
	return idx;
}
//salt
const g_password_salt="1a2b3c4d"

function g_getQueryString(name) {
	let reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	let r = window.location.search.substr(1).match(reg);
	if (r != null) return unescape(r[2]);
	return null;
}
