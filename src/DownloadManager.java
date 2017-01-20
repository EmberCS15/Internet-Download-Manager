import java.io.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;
public class DownloadManager extends JFrame implements Observer{
	private JTextField addText;
	private JButton resumeButton,pauseButton,cancelButton,clearButton;
	private DownloadTableModel tableModel;
	JTable table;
	private boolean clearing;
	private  Download selectedDownload;
	public DownloadManager(){
		setTitle("Personal Download Manager");
		setSize(600,1000);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				actionExit();
			}
		});
		JMenuBar menu=new JMenuBar();
		JMenu file_name=new JMenu();
		file_name.setMnemonic(KeyEvent.VK_F);
		JMenuItem exit=new JMenuItem("Exit",KeyEvent.VK_X);
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				actionExit();
			}
		});
		file_name.add(exit);
		menu.add(file_name);
		setJMenuBar(menu);
		JPanel addPanel=new JPanel();
		addText=new JTextField(40);
		JButton addButton=new JButton("DOWNLOAD");
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				actionAdd();
			}
		});
		addPanel.add(addText);
		addPanel.add(addButton);
		tableModel=new DownloadTableModel();
		table=new JTable(tableModel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent le){
				tableSelectionChanged();
			}
		});
		/*forgot this entire portion on table.Took A little Time to figure it out*/
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ProgressRenderer renderer=new ProgressRenderer(0,100);
		renderer.setStringPainted(true);
		table.setDefaultRenderer(JProgressBar.class,renderer);
		table.setRowHeight((int)renderer.getPreferredSize().getHeight());
		JPanel downloadPanel=new JPanel();
		downloadPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
		downloadPanel.setLayout(new BorderLayout());
		downloadPanel.add(new JScrollPane(table),BorderLayout.CENTER);
		JPanel buttonPanel=new JPanel();
		pauseButton=new JButton("PAUSE");
		pauseButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				actionPause();
			}
		});
		pauseButton.setEnabled(false);
		buttonPanel.add(pauseButton);
		resumeButton=new JButton("RESUME"); 
		resumeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				actionResume();
			}
		});
		resumeButton.setEnabled(false);
		buttonPanel.add(resumeButton);
		cancelButton=new JButton("CANCEL");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				actionCancel();
			}
		});
		cancelButton.setEnabled(false);
		buttonPanel.add(cancelButton);
		clearButton=new JButton("CLEAR");
		pauseButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				actionClear();
			}
		});
		pauseButton.setEnabled(false);
		buttonPanel.add(clearButton);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(addPanel,BorderLayout.NORTH);
		getContentPane().add(downloadPanel,BorderLayout.CENTER);
		getContentPane().add(buttonPanel,BorderLayout.SOUTH);
	}
		private void actionExit(){
			System.exit(0);
		}
		private void actionAdd(){
			URL verifiedURL=verifyUrl(addText.getText());
			if(verifiedURL!=null){
				tableModel.addDownload(new Download(verifiedURL));
				addText.setText("");
			}else{
				JOptionPane.showMessageDialog(this, "Invalid URL Entered", "Error",JOptionPane.ERROR_MESSAGE);
			}
		}
		private URL verifyUrl(String urlName){
			if((!urlName.toLowerCase().startsWith("https://"))&&(!urlName.toLowerCase().startsWith("http://")))
				return null;
			URL urlVerify=null;
			try{
				urlVerify=new URL(urlName);
			}catch(Exception e){
				return null;
			}
			if(urlVerify.getFile().length()<2)
				return null;
			return urlVerify;
		}
		private void tableSelectionChanged(){
			if(selectedDownload!=null)
				selectedDownload.deleteObserver(DownloadManager.this);
			if(clearing!=true&&table.getSelectedRow()>-1){
				selectedDownload=tableModel.getDownload(table.getSelectedRow());
				selectedDownload.addObserver(DownloadManager.this);
				updateButtons();
			}
		}
		private void actionPause(){
			selectedDownload.pause();
			updateButtons();
		}
		private void actionResume(){
			selectedDownload.resume();
			updateButtons();
		}
		private void actionCancel(){
			selectedDownload.cancel();
			updateButtons();
		}
		private void actionClear(){
			clearing=true;
			tableModel.clearDownload(table.getSelectedRow());
			clearing=false;
			selectedDownload=null;
			//selectedDownload.clear();
			updateButtons();
		}
		private void updateButtons(){
			if(selectedDownload!=null){
				int st=selectedDownload.getStatus();
				switch(st){
					case Download.DOWNLOAD:pauseButton.setEnabled(true);
											resumeButton.setEnabled(false);
											cancelButton.setEnabled(true);
											clearButton.setEnabled(false);
											break;
					case Download.PAUSE:pauseButton.setEnabled(false);
										resumeButton.setEnabled(true);
										cancelButton.setEnabled(true);
										clearButton.setEnabled(false);
										break;
					case Download.RESUME:pauseButton.setEnabled(true);
										resumeButton.setEnabled(false);
										cancelButton.setEnabled(true);
										clearButton.setEnabled(false);
										break;
								default:pauseButton.setEnabled(false);
										resumeButton.setEnabled(false);
										cancelButton.setEnabled(false);
										clearButton.setEnabled(true);
										break;
							
					}
				}
				else{
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(false);
				}
		}
		public void update(Observable o,Object obj){
			if(selectedDownload!=null&&selectedDownload.equals(o))
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						updateButtons();//Why this is done on a new Thread
					}
				});
		}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				DownloadManager dr=new DownloadManager();
				dr.setVisible(true);
			}
		});
	}
}
