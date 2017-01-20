import java.io.*;
import java.util.*;
import java.net.*;

class Download extends Observable implements Runnable{
	public static final int MAX_BUFFER_SIZE=1024;
	public static final String STATUS[]={"Downloading","Resume","Pause","ERROR","Cancel","Complete"};
	public static final int DOWNLOAD=0;
	public static final int RESUME=1;
	public static final int PAUSE=2;
	public static final int CANCEL=4;
	public static final int ERROR=3;
	public static final int COMPLETE=5;
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
		return ((float)(downloaded/size)*100);
	}
	public int getStatus(){
		return status;
	}
	public void pause(){
		status=PAUSE;
		stateChanged();
	}
	public void resume(){
		status=RESUME;
		stateChanged();
	}
	public void cancel(){
		status=CANCEL;
		stateChanged();
	}
	public void complete(){
		status=COMPLETE;
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
	public String getFileName(){
		String fileName=url.getFile();
		return fileName.substring(fileName.lastIndexOf('/'));
	}
	public void run(){
		RandomAccessFile file=null;
		InputStream stream=null;
		try{
		HttpURLConnection connection=(HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Range", "byte="+downloaded+"-");
		connection.connect();
		if(connection.getResponseCode()/100!=2){
			error();
		}
		int contentLength=connection.getContentLength();
		if(contentLength<1){
			error();
		}
		if(size==-1){
			size=contentLength;
			stateChanged();//forgot this
		}
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
		}catch(Exception e){}
		finally{
			try{
			if(file!=null)
					file.close();
			}catch(Exception e){}
			try{
				if(stream!=null)
					stream.close();
			}catch(Exception e){}
		}
	}
	private void stateChanged(){
		setChanged();
		notifyObservers();
	}
}
