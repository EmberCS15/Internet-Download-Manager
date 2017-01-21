import java.io.*;
import java.util.*;
import java.net.*;

class Download extends Observable implements Runnable{
	private static final int MAX_BUFFER_SIZE=1024;
	public static final String STATUS[]={"Downloading","Complete","Pause","ERROR","Cancel"};
	public static final int DOWNLOAD=0;
	public static final int COMPLETE=1;
	public static final int PAUSE=2;
	public static final int CANCEL=4;
	public static final int ERROR=3;
	//public static final int COMPLETE=5;
	private URL url;
	private int status;
	private int size;
	private int downloaded;
	public Download(URL url){
		this.url=url;
		size=-1;
		downloaded=0;
		status=DOWNLOAD;
		download();
	}
	public String getUrlName(){
		return url.toString();
	}
	public int getSize(){
		return size;
	}
	public float getProgress(){
		return ((float)downloaded/size)*100;
	}
	public int getStatus(){
		return status;
	}
	public void pause(){
		status=PAUSE;
		stateChanged();
	}
	public void resume(){
		status=DOWNLOAD;
		stateChanged();
		download();
	}
	public void cancel(){
		status=CANCEL;
		stateChanged();
	}
	private void error(){
		status=ERROR;
		stateChanged();
	}
	private void download(){
		Thread downloadThread=new Thread(this);
		downloadThread.start();
		//stateChanged();//Why this isnt used here
	}
	public String getFileName(URL url){
		String fileName=url.getFile();
		return fileName.substring(fileName.lastIndexOf('/')+1);
	}
	public void run(){
		RandomAccessFile file=null;
		InputStream stream=null;
		try{
		HttpURLConnection connection=(HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Range", "bytes="+downloaded+"-");
		connection.connect();
		if(connection.getResponseCode()/100!=2){
			System.out.println("1");
			error();
		}
		int contentLength=connection.getContentLength();
		if(contentLength<1){
			System.out.println("2");
			error();
		}
		if(size==-1){
			size=contentLength;
			stateChanged();//forgot this
		}
		file=new RandomAccessFile(getFileName(url),"rw");
		file.seek(downloaded);
		stream=connection.getInputStream();
		while(status==DOWNLOAD){
			byte buffer[];
			if((size-downloaded)>=MAX_BUFFER_SIZE){
				buffer=new byte[MAX_BUFFER_SIZE];
			}
			else buffer=new byte[size-downloaded];
			int read=stream.read(buffer);
			if(read==-1)
				break;
			file.write(buffer, 0, read);;
			downloaded+=read;
			stateChanged();//forgot this
		}
		if(status==DOWNLOAD){
			status=COMPLETE;
			stateChanged();
		}
		}catch(Exception e){
			e.printStackTrace();
			error();
		}finally{
			if(file!=null){
				try{
					file.close();
			}catch(Exception e){}
			}
			if(stream!=null){
			try{
				stream.close();
			}catch(Exception e){}
			}
		}
	}
	private void stateChanged(){
		setChanged();
		notifyObservers();
	}
}
