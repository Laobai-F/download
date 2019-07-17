<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> 
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>多线程下载文件和断点续传项目</title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
</head>
<body>
	<div class="htmleaf-container">
		<header class="clearfix">
			<h1 class="header-text fl">多线程下载文件和断点续传项目</h1>
			
			<div class="down-text fr">
				<div class="text fl">下载进度：</div>
				<div class="num fl" id="p">0.0%</div>
			</div>
		</header>
		<div class="htmleaf-content">
			<div class="progress">
			  <b class="progress__bar">
			    <span class="progress__text">
			      Progress: <em>0%</em>
			    </span>
			  </b>
			</div>
		</div>
		<br />
		<input class="but" type="button" id="subrequest" value="开始"  />
		<input class="but" type="button" id="stop" value="暂停" style="display: none;" />
		<input class="but" type="button" id="continue" value="继续" style="display: none;"/>
	</div>
	
	<!-- <input type="button" id="stop" value="暂停"/>
	<hr />
	<input type="button" id="continue" value="继续"/>
	<hr/>
	<input type="button" id="subrequest" value="开始下载,并请求" />
	<hr/>
	下载进度：<p id="p">0.0</p> -->
</body>
<script type="text/javascript" src="js/jquery-3.4.1.min.js"></script>
<script>window.jQuery || document.write('<script src="js/jquery-2.1.1.min.js"><\/script>')</script>

<script type="text/javascript">
	
	
	//--------------------------------------------------//
	//$progress进度、$bar条、$text文本
	var $progress = $('.progress'), 		//进度
		$bar = $('.progress__bar'), 		//条
		$text = $('.progress__text'), 		//文本
		percent = 0, 						//进度条百分比
		update, 							//开始
		resetColors, 						//重置颜色
		speed = 10, 						//速度
		orange = 30, 
		yellow = 55, 
		green = 85, 
		timer;								//计算器
		
	update = function(){
		timer =function(){}
	}
	//003循环发送请求
	function ref(){
		$.post("DownServlet?down=4",function(date){
			$("#p").text(date+"%");
			percent=date;
			subBox();
			if(date==100.0){
				window.clearInterval(sub);
				$("#stop").hide();
				$("#continue").hide();
			}
		});
	}
	//开始
	update = function () {
	    timer = setTimeout(function () {
			
	        $text.find('em').text(percent + '%');
	        //颜色判断
	        if (percent >= 100) {
	            percent = 100;
	            $progress.addClass('progress--complete');
	            $bar.addClass('progress__bar--blue');
	            $text.find('em').text('恭喜你下载完成');
	        } else {
	            if (percent >= green) {
	                $bar.addClass('progress__bar--green');
	            } else if (percent >= yellow) {
	                $bar.addClass('progress__bar--yellow');
	            } else if (percent >= orange) {
	                $bar.addClass('progress__bar--orange');
	            }
	        }
	        $bar.css({ width: percent + '%' });
	    }, speed);
	};
	//执行进度加载
	function subBox() {
		$progress.addClass('progress--active');
	    update();
	}
	//--------------------------------------------------//
	var sub;
	
	//AJAX请求下载
	$(function(){
		$("#subrequest").one("click", function(){
			$.get("DownServlet?down=1");
			//循环发送请求
			sub = window.setInterval(ref,100);
			$(this).hide();
			$("#stop").show();
		});
		
		$("#stop").click(function(){
			$.get("DownServlet?down=2");
			window.clearInterval(sub);
			$(this).hide();
			$("#continue").show();
		});
		$("#continue").click(function(){
			$.get("DownServlet?down=3");
			sub = window.setInterval(ref,100);
			$(this).hide();
			$("#stop").show();
		});
		
		
	});
	
	
	
</script>
</html>