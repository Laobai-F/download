package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/DownServlet")
public class DownServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    static Download d;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String down = request.getParameter("down");
		
		if("1".equals(down)) {
			System.out.println("开始下载");
			d=new Download();
			d.startDownload();
		}
		if("2".equals(down)) {
			System.out.println("暂停下载");
			d.stopThread();
		}
		if("3".equals(down)) {
			System.out.println("继续下载");
			d.startThread();
		}
		
		if("4".equals(down)) {
			PrintWriter out = response.getWriter();
			//System.out.println("收到请求");
			float currplan = d.count(d);
			out.print(currplan);
			out.flush();
		}
		
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
