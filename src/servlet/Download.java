package servlet;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*@类名Download.java
*@作者 Laobai
*@日期 2019年7月2日-下午10:25:22
*@描述
*		1.计算文件大小
*/
public class Download {
	public static int threadCount=3;			//下载线程数量
	private static int blockSize=0;				//每个线程要下载的文件大小
	//下载的地址
	private String downloadUrl="http://cir.cmeplaza.com:8866/download/mh/images/index_banner03.jpg";
	//http://cir.cmeplaza.com:8866/resources/cme/3D_weather/images/3Dimg.jpg
	//http://cir.cmeplaza.com:8866/video/zjshmh.mp4
	//保存的文件名
	private String saveFileName="C:\\Users\\LaoBai\\Desktop\\Q.jpg";			
	private static int fileLength=0;			//下载的文件大小
	private String errorMsg="";					//错误消息
	public Map<Integer,DownloadThread> downloadedInfo = new HashMap<Integer,DownloadThread>();  //下载信息(为了恢复线程意外失效)
	public Map<Integer, Integer> downloadedSize = new HashMap<Integer,Integer>();//下载大小HashMap集合
	public List<DownloadThread> threadList = new ArrayList<DownloadThread>(); //线程ArrayList集合
	public static int currThreadCount=0;		//当前下载线程数
	public float currDownloadplan=0;				//当前下载总进度
	
	public static void main(String[] args) throws Exception {
//		System.out.print("开始下载");
//		Download d=new Download();
//		d.startDownload();//开始下载
//		d.count(d);
		
//		//定时器1
//		Robot  r   =   new   Robot(); 
//			System.out.println("开始下载");
//			Download d=new Download();
//			d.startDownload();//开始下载
//        r.delay(   5000   );
//        	//定时器2
//        	Robot  r2   =   new   Robot();
//	        	d.stopThread();
//	        	System.out.println("暂停下载");
//        	r2.delay(   5000   );
//        		d.startThread();
//        		System.out.println("继续下载");
		
	}
	
	public void startDownload() {
		//1.请求http
		try {
			URL url = new URL(downloadUrl);
			HttpURLConnection huc =(HttpURLConnection) url.openConnection();
			//设定超时(10秒)
			huc.setRequestMethod("GET");
			huc.setConnectTimeout(10*1000);
			huc.setRequestProperty("Charset", "utf-8");
			int code = huc.getResponseCode();
			System.out.println(downloadUrl+"【http响应码："+code+"】");
			if(code==200) {
				System.out.println("地址有效");
				//获取文件长度
				fileLength = huc.getContentLength();
				System.out.println("下载文件总大小："+fileLength);
				
			}else {
				System.out.println("网址有效，但服务器返回错误，错误代码："+code);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//2.计算文件分块数
		blockSize = fileLength / threadCount;
		//开启对应数量的线程
		for(int i=0;i<threadCount;i++) {
			//获取每个线程要下载文件的【开始位置】及【结束位置】
			int startThread = i * blockSize;
			int endThread = (i+1) * blockSize -1;
			//计算最后一个线程要下载文件的长度
			if(i+1 == threadCount) {
				endThread = endThread + (fileLength%threadCount);
			}
			//开启每个线程
			//---------------------------------------------//
			//DownloadThread(int threadId, int startThread, int endThread, String url)
			threadList.add(new DownloadThread(i,startThread,endThread,this,downloadUrl));
			threadList.get(i).start();
			
		}
		
	}
	//计算总进度
	public float count(Download d) {
		Map<Integer, Integer> map = d.downloadedSize;
		
		int allLength=0;
        for(int key:map.keySet()) {
	    	allLength+=map.get(key);
        }
        return (float)((int)(((float)allLength/Download.fileLength)*10000))/100;
	}
	
	//暂停线程(sleep相对安全)
	public void stopThread() {
		for(DownloadThread dt:threadList) {
//			try {
//				System.out.println("睡3");
//				//线程休眠(999999999)
////				dt.sleep(999999999);
//				
//				System.out.println(dt.getState());
//				System.out.println("暂停下载");
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			dt.suspend();
		}
	}
	//唤起线程
	public void startThread() {
		for(DownloadThread dt:threadList) {
//			try {
//				//interrupt()方法打断线程的暂停状态
//				dt.interrupt();
//				System.out.println("继续下载");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			dt.resume();
		}
	}

	public static int getThreadCount() {
		return threadCount;
	}

	public static void setThreadCount(int threadCount) {
		Download.threadCount = threadCount;
	}

	public static int getBlockSize() {
		return blockSize;
	}

	public static void setBlockSize(int blockSize) {
		Download.blockSize = blockSize;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getSaveFileName() {
		return saveFileName;
	}

	public void setSaveFileName(String saveFileName) {
		this.saveFileName = saveFileName;
	}

	public static int getFileLength() {
		return fileLength;
	}

	public static void setFileLength(int fileLength) {
		Download.fileLength = fileLength;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Map<Integer, DownloadThread> getDownloadedInfo() {
		return downloadedInfo;
	}

	public void setDownloadedInfo(Map<Integer, DownloadThread> downloadedInfo) {
		this.downloadedInfo = downloadedInfo;
	}

	public Map<Integer, Integer> getDownloadedSize() {
		return downloadedSize;
	}
	
	public void setDownloadedSize(Map<Integer, Integer> downloadedSize) {
		this.downloadedSize = downloadedSize;
	}
	public List<DownloadThread> getThreadList() {
		return threadList;
	}

	public void setThreadList(List<DownloadThread> threadList) {
		this.threadList = threadList;
	}

	public static int getCurrThreadCount() {
		return currThreadCount;
	}

	public static void setCurrThreadCount(int currThreadCount) {
		Download.currThreadCount = currThreadCount;
	}

	public float getCurrDownloadplan() {
		return currDownloadplan;
	}
	public void setCurrDownloadplan(float currDownloadplan) {
		this.currDownloadplan = currDownloadplan;
	}

}









