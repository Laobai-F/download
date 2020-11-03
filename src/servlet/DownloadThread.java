package servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
*@类名Down.java
*@作者 Laobai
*@版本 V1.0
*@日期 2019年7月6日-上午8:39:54
*/
public class DownloadThread extends Thread{
	private static final String readLine = null;
	private int threadId;		//线程ID
	private int startThread;	//下载起始位置
	private int endThread;		//下载结束位置
	private String url;			//下载地址
	private int total;			//下载总大小
	private Download download;	//下载类
	private String tempSaveFileName;  //临时保存文件名
	private boolean tag=false;	//是否第一次启用
	
	//构造内容：线程ID、开始位置、结束位置、下载类、下载地址
	public DownloadThread(int threadId, int startThread, int endThread, Download download,String url) {
		this.threadId = threadId;
		this.startThread = startThread;
		this.endThread = endThread;
		this.download=download;
		this.url = url;
	}
	//构造内容：线程ID、开始位置、结束位置、下载地址、保存文件名称
	public DownloadThread(int threadId, int startThread, int endThread, String url,String tempSaveFileName) {
		this.threadId = threadId;
		this.startThread = startThread;
		this.endThread = endThread;
		this.url = url;
		this.tempSaveFileName=tempSaveFileName;
	}
	//构造内容：线程ID、开始位置、结束位置、下载地址、保存文件名称、是否第一次下载
	public DownloadThread(int threadId, int startThread, int endThread, String url,String tempSaveFileName,boolean tag) {
		this.threadId = threadId;
		this.startThread = startThread;
		this.endThread = endThread;
		this.url = url;
		this.tempSaveFileName=tempSaveFileName;
		this.tag=tag;
	}
	
	public void run() {
		System.out.println("线程："+threadId+"，开启");
		//同步锁(当前下载线程数+1)
		synchronized (Download.class) {
			this.download.currThreadCount +=1;
		}
		//运行线程，每个线程建立自己的连接
		try {
			URL url = new URL(this.url);
			//创建HTTP连接
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			//设定超时
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10 * 1000);
			//临时文件(文件名+线程id+后缀名)
			if(!tag) {
				tempSaveFileName=download.getSaveFileName();
			}else {
				tempSaveFileName=this.tempSaveFileName;
			}
			File tempfile = new File(tempSaveFileName+"."+threadId+".txt");
			//判断零时文件是否存在
			if(tempfile.exists()) {
				BufferedReader tempBr = new BufferedReader(new InputStreamReader(new FileInputStream(tempfile)));
				//获取文件长度字符串
				String readLine = tempBr.readLine();
				//字符串
				startThread = Integer.parseInt(readLine);
				tempBr.close();
			}
			
			//指示是否允许缓存(否)
			conn.setUseCaches(false);
			//设置分段下载的头信息，range做分段
			conn.setRequestProperty("Range","bytes="+startThread+"-"+endThread);
			conn.connect();
			//获取HTTP响应状态码
			int code = conn.getResponseCode();
			System.out.println("线程："+threadId+"，响应状态码："+code);
			if(code == 206 ){  //200：请求全部资源成功 、206：请求部分资源成功
				InputStream is = conn.getInputStream();
				//创建随机访问流,写入文件
				RandomAccessFile raf=new RandomAccessFile(new File(tempSaveFileName), "rw");
				raf.seek(startThread);
				byte[] b = new byte[1024];//1024*10*8
				int len=0;
				System.out.println("【线程ID："+threadId+",下载指针:"+startThread+"~"+endThread+"】");
				//读取文件长度
				while((len=is.read(b))!=-1) {
					//随机访问文件流（写入临时文件记录长度）
					raf.write(b,0,len);
					
					total += len;
					int currlen = startThread + total;
					RandomAccessFile raf2=new RandomAccessFile(tempfile, "rwd");
					raf2.write(String.valueOf(currlen).getBytes());
					raf2.close();
					//记录每个线程下载的信息
					synchronized(Download.class) {
						//记录下载大小
						download.downloadedSize.put(threadId, total);
						//记录下载信息
						download.downloadedInfo.put(threadId, new DownloadThread(threadId,startThread,endThread,this.url,tempSaveFileName));
					}
				}
				raf.close();
				is.close();
				System.out.println("线程:"+threadId+"下载完毕，共下载："+total);
				//删除临时文件
				synchronized (Download.class) {
					Download.currThreadCount -=1;
					if(Download.currThreadCount==0) {
						System.out.println("下载完成的文件大小："+new File(download.getSaveFileName()).length());
						int saveFileLength = (int)new File(download.getSaveFileName()).length();
						if(saveFileLength==download.getFileLength()) {
							System.out.println("下载文件完整，删除临时文件");
							for(int i=0;i<Download.threadCount;i++) {
								new File(download.getSaveFileName()+"."+i+".txt").delete();
							}
						}else {
							System.out.println("下载文件不完整");
							for(int i=0;i<Download.threadCount;i++) {
								//下载完成后，创建零时文件的流对象
								File file = new File(download.getSaveFileName()+"."+i+".txt");
								BufferedReader br = new BufferedReader(new FileReader(file));
								int tempLine = Integer.parseInt(br.readLine());
								System.out.println("临时文件记录位置："+tempLine);
								//获取对应线程的下载信息
								DownloadThread thread = download.downloadedInfo.get(i);
								//如果临时文件的长度不等于下载线程的下载总长，则开启新的线程，补救
								if((tempLine-1)!=thread.endThread) {
									new DownloadThread(i,thread.startThread,thread.endThread,thread.url,thread.tempSaveFileName,true).run();
								}
							}
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.run();
	}
	public int getThreadId() {
		return threadId;
	}
	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}
	public int getStartThread() {
		return startThread;
	}
	public void setStartThread(int startThread) {
		this.startThread = startThread;
	}
	public int getEndThread() {
		return endThread;
	}
	public void setEndThread(int endThread) {
		this.endThread = endThread;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public Download getDownload() {
		return download;
	}
	public void setDownload(Download download) {
		this.download = download;
	}
	public String getTempSaveFileName() {
		return tempSaveFileName;
	}
	public void setTempSaveFileName(String tempSaveFileName) {
		this.tempSaveFileName = tempSaveFileName;
	}
	public boolean isTag() {
		return tag;
	}
	public void setTag(boolean tag) {
		this.tag = tag;
	}
	public static String getReadline() {
		return readLine;
	}
	
}
