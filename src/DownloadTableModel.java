import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class DownloadTableModel extends AbstractTableModel implements Observer{
	private static final String columnNames[]={"URL","Size","Progress","Status"};
	private static final Class classNames[]={String.class,String.class,JProgressBar.class,String.class};
	private ArrayList<Download> downloadList=new ArrayList<Download>();
	public void addDownload(Download download){
		download.addObserver(this);
		downloadList.add(download);
		fireTableRowsInserted(getRowCount()-1,getRowCount()-1);
	}
	public void clearDownload(int row){
		downloadList.remove(row);
		fireTableRowsDeleted(row,row);
	}
	public String getColumnName(int col){
		return columnNames[col];
	}
	public Class<?> getColumnClass(int col){
		return classNames[col];
	}
	public Download getDownload(int row){
		return downloadList.get(row);
	}
	public int getRowCount(){
		return downloadList.size();
	}
	public int getColumnCount(){
		return columnNames.length;
	}
	public Object getValueAt(int row,int col){
		Download obj=downloadList.get(row);
		switch(col){
			case 0:return obj.getUrlName();
			case 1:int i=obj.getSize();
					return (i==-1)?"":Integer.toString(i);
			case 2:return new Float(obj.getProgress());
			case 3:return obj.STATUS[obj.getStatus()];
		}
		return "";
	}
	public void update(Observable o,Object arg){
		int index=downloadList.indexOf(o);
		fireTableRowsUpdated(index,index);
	}
}
