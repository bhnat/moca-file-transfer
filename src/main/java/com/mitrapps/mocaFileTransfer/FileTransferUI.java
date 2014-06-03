/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 MiTR Apps, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mitrapps.mocaFileTransfer;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.DirectConnection;
import com.redprairie.moca.client.HttpConnection;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.client.MocaConnection;

public class FileTransferUI extends JFrame {

	public static final String REMOTE_HOME_DIR = "$LESDIR";
	private static final long serialVersionUID = 1L;
	private static MocaConnection conn;
	private File currentLocalDirectory;
	private JPanel contentPane;
	private JTextField localPath;
	private JTextField remotePath;
	private JTextField mocaUrl;
	private JTextField mocaUid;
	private JPasswordField mocaPwd;
	private JTable localListing;
	private JTable remoteListing;
	private JButton uploadButton;
	private JButton downloadButton;
	private JButton loginButton;
	private JTextArea logArea;
	private String tempPathString; // used in FocusListener to see if the path has changed
	
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileTransferUI frame = new FileTransferUI();
					setLookAndFeel();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public FileTransferUI() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		setMinimumSize(new Dimension(600, 400));
		contentPane = new JPanel();
		
		SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
        
        JLabel localPathLabel = new JLabel("Local: ");
        JLabel remotePathLabel = new JLabel("Remote: ");
        
        this.localPath = new JTextField();
        this.remotePath = new JTextField();
        this.localPath.setEditable(true);
        this.remotePath.setEditable(true); 
        
        this.localPath.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            	tempPathString = localPath.getText();
            };
            public void focusLost(FocusEvent e) {
            	if (!e.isTemporary() && !tempPathString.equalsIgnoreCase(localPath.getText())) {
            		setLocalPathManually();
            	}
            }
        });
        
        this.remotePath.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            	tempPathString = remotePath.getText();
            };
            public void focusLost(FocusEvent e) {
            	if (!e.isTemporary() && !tempPathString.equalsIgnoreCase(remotePath.getText())) {
            		setRemotePathManually();
            	}
            }
        });
        
        this.mocaUrl = new JTextField();
        this.mocaUid = new JTextField();
        this.mocaPwd = new JPasswordField();
        
        this.currentLocalDirectory = new File(".");
        JLabel mocaUrlLabel = new JLabel("URL:  ", SwingConstants.RIGHT);
        JLabel mocaUidLabel = new JLabel("UID:  ", SwingConstants.RIGHT);
        JLabel mocaPwdLabel = new JLabel("PWD:  ", SwingConstants.RIGHT);
        
        this.loginButton = new JButton("Login");
        this.loginButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae){
    			login();
    		}
    	});
        
        this.localListing = new JTable(new LocalListingTableModel(this.currentLocalDirectory));
        this.remoteListing = new JTable(new RemoteListingTableModel(null, REMOTE_HOME_DIR));
        this.localListing.setAutoCreateRowSorter(true);
        this.remoteListing.setAutoCreateRowSorter(true);
        
        this.localListing.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2) {
                	String fileType = (String)table.getValueAt(row, AbstractListingTableModel.FILE_TYPE_COLUMN_INDEX);
                	if (fileType.equalsIgnoreCase(AbstractListingTableModel.DIRECTORY_TYPE)) {
                		performLocalDirectoryChange(row);
                	}
                }
            }
        });
        
        this.remoteListing.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2) {
                	String fileType = (String)table.getValueAt(row, AbstractListingTableModel.FILE_TYPE_COLUMN_INDEX);
                	if (fileType.equalsIgnoreCase(AbstractListingTableModel.DIRECTORY_TYPE)) {
                		performRemoteDirectoryChange(row);
                	}
                }
            }
        });
        
        JScrollPane localScrollPane = new JScrollPane(this.localListing);
        JScrollPane remoteScrollPane = new JScrollPane(this.remoteListing);
        
        this.uploadButton = new BasicArrowButton(BasicArrowButton.EAST);
        this.uploadButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae){
        		performUpload();
       		}
        });
        
        this.downloadButton = new BasicArrowButton(BasicArrowButton.WEST);
        this.downloadButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae){
    			performDownload();
    		}
    	});
        
        this.logArea = new JTextArea(80, 80);
        JScrollPane logScrollPane = new JScrollPane(this.logArea);
        
        contentPane.add(mocaUrlLabel);
        contentPane.add(this.mocaUrl);
        contentPane.add(mocaUidLabel);
        contentPane.add(this.mocaUid);
        contentPane.add(mocaPwdLabel);
        contentPane.add(this.mocaPwd);
        contentPane.add(this.loginButton);
        contentPane.add(localPathLabel);
        contentPane.add(this.localPath);
        contentPane.add(remotePathLabel);
        contentPane.add(this.remotePath);
        contentPane.add(localScrollPane);
        contentPane.add(remoteScrollPane);
        contentPane.add(this.uploadButton);
        contentPane.add(this.downloadButton);
        contentPane.add(logScrollPane);
        
        // pin all the connection info 5 from the top
        layout.putConstraint(SpringLayout.NORTH, mocaUrlLabel, 5, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, this.mocaUrl, 5, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, mocaUidLabel, 5, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, this.mocaUid, 5, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, mocaPwdLabel, 5, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, this.mocaPwd, 5, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, this.loginButton, 5, SpringLayout.NORTH, contentPane);
        
        // pin the right/left of the connection info and set the label widths
        SpringLayout.Constraints contraints = layout.getConstraints(mocaUrlLabel);
        contraints.setWidth(Spring.constant(40));
        layout.putConstraint(SpringLayout.WEST, mocaUrlLabel, 5, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, this.mocaUrl, 5, SpringLayout.EAST, mocaUrlLabel);
        // last element
        layout.putConstraint(SpringLayout.EAST, this.loginButton, -5, SpringLayout.EAST, contentPane); 
        
        layout.putConstraint(SpringLayout.WIDTH, mocaUidLabel, 0, SpringLayout.WIDTH, mocaUrlLabel);
        layout.putConstraint(SpringLayout.EAST, this.mocaUrl, -5, SpringLayout.WEST, mocaUidLabel);
        
        contraints = layout.getConstraints(this.mocaUid);
        contraints.setWidth(Spring.constant(100));
        layout.putConstraint(SpringLayout.EAST, mocaUidLabel, -5, SpringLayout.WEST, this.mocaUid);
        
        layout.putConstraint(SpringLayout.WIDTH, mocaPwdLabel, 0, SpringLayout.WIDTH, mocaUrlLabel);
        layout.putConstraint(SpringLayout.EAST, this.mocaUid, -5, SpringLayout.WEST, mocaPwdLabel);
        
        layout.putConstraint(SpringLayout.WIDTH, this.mocaPwd, 0, SpringLayout.WIDTH, this.mocaUid);
        layout.putConstraint(SpringLayout.EAST, mocaPwdLabel, -5, SpringLayout.WEST, this.mocaPwd);
        
        contraints = layout.getConstraints(this.loginButton);
        contraints.setWidth(Spring.constant(80));
        layout.putConstraint(SpringLayout.EAST, this.mocaPwd, -5, SpringLayout.WEST, this.loginButton);
        
        // File/Directory related field layouts     
        
        layout.putConstraint(SpringLayout.WEST, localPathLabel, 5, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.NORTH, localPathLabel, 50, SpringLayout.NORTH, contentPane);
        
        layout.putConstraint(SpringLayout.WEST, this.localPath, 5, SpringLayout.EAST, localPathLabel);
        layout.putConstraint(SpringLayout.NORTH, this.localPath, 50, SpringLayout.NORTH, contentPane);
        
        layout.putConstraint(SpringLayout.WEST, remotePathLabel, 5, SpringLayout.EAST, this.localPath);
        layout.putConstraint(SpringLayout.NORTH, remotePathLabel, 50, SpringLayout.NORTH, contentPane);
        
        layout.putConstraint(SpringLayout.WEST, this.remotePath, 5, SpringLayout.EAST, remotePathLabel);
        layout.putConstraint(SpringLayout.NORTH, this.remotePath, 50, SpringLayout.NORTH, contentPane);
        
        layout.putConstraint(SpringLayout.EAST, contentPane, 5, SpringLayout.EAST, remotePath);
        
        layout.putConstraint(SpringLayout.WIDTH, this.localPath, 0, SpringLayout.WIDTH, this.remotePath);
        
        layout.putConstraint(SpringLayout.WEST, localScrollPane, 0, SpringLayout.WEST, this.localPath);
        layout.putConstraint(SpringLayout.EAST, localScrollPane, 0, SpringLayout.EAST, this.localPath);
        layout.putConstraint(SpringLayout.NORTH, localScrollPane, 50, SpringLayout.NORTH, this.localPath);
        
        layout.putConstraint(SpringLayout.WEST, remoteScrollPane, 0, SpringLayout.WEST, this.remotePath);
        layout.putConstraint(SpringLayout.EAST, remoteScrollPane, 0, SpringLayout.EAST, this.remotePath);
        layout.putConstraint(SpringLayout.NORTH, remoteScrollPane, 50, SpringLayout.NORTH, this.localPath);
        
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, this.uploadButton, 25, SpringLayout.EAST, localScrollPane);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, this.downloadButton, 25, SpringLayout.EAST, localScrollPane);
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, this.uploadButton, -50, SpringLayout.VERTICAL_CENTER, localScrollPane);
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, this.downloadButton, -20, SpringLayout.VERTICAL_CENTER, localScrollPane);
        
        // add log scroll pane
        layout.putConstraint(SpringLayout.SOUTH, logScrollPane, -20, SpringLayout.SOUTH, contentPane);
        layout.putConstraint(SpringLayout.WEST, logScrollPane, 5, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.EAST, logScrollPane, -5, SpringLayout.EAST, contentPane);
        contraints = layout.getConstraints(logScrollPane);
        contraints.setHeight(Spring.constant(120));
        
        layout.putConstraint(SpringLayout.SOUTH, remoteScrollPane, -20, SpringLayout.NORTH, logScrollPane);
        layout.putConstraint(SpringLayout.SOUTH, localScrollPane, -20, SpringLayout.NORTH, logScrollPane);
        
        
		setContentPane(contentPane);
		
		Action refreshAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				refresh();
		    }
		};
		
		this.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "refresh");
		this.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.META_MASK), "refresh");
		this.contentPane.getActionMap().put("refresh", refreshAction);
		
		this.setTitle("moca File Transfer");
		this.setLocalPath(this.currentLocalDirectory.getAbsolutePath());
		resetColumnSizes();
	}

	private void resetColumnSizes() {
		this.resetLocalColumnSizes();
		this.resetRemoteColumnSizes();
	}
	
	private void resetLocalColumnSizes() {
		this.localListing.getColumnModel().getColumn(0).setMaxWidth(60);
		this.localListing.getColumnModel().getColumn(2).setPreferredWidth(40);
	}
	
	private void resetRemoteColumnSizes() {
		this.remoteListing.getColumnModel().getColumn(0).setMaxWidth(60);
		this.remoteListing.getColumnModel().getColumn(2).setPreferredWidth(40);
	}
	
	public static void setLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	
	private void refresh() {
		getLocalDirectoryListing(localPath.getText());
		if (this.remotePath.getText().length() > 0) {
			this.getRemoteDirectoryListing(this.remotePath.getText());
		}
	}
	
	private void login() {
		try {
			conn = makeConnections();
        	String remotePathString = REMOTE_HOME_DIR;
        	MocaResults rs = conn.executeCommand(String.format("find file where filnam = '%s'", REMOTE_HOME_DIR));
        	while (rs.next()) {
        		remotePathString = rs.getString("pathname");
        		this.remotePath.setText(remotePathString);
        	}
        	RemoteListingTableModel model = new RemoteListingTableModel(conn, REMOTE_HOME_DIR);
    		this.remoteListing.setModel(model);	
    		this.resetRemoteColumnSizes();
        } catch (MocaException e) {
        	displayException(e);
        }
	}

	private void displayException(Exception e) {
		StackTraceElement[] st = e.getStackTrace();
		for (StackTraceElement element : st) {
			this.logArea.append(element.toString());
			this.logArea.append("\n");
		}
	}

	private MocaConnection makeConnections() throws MocaException, LoginFailedException {
		MocaConnection newConn = null;
		if (this.mocaUrl.getText().startsWith("http")) {
			this.logArea.append("Connecting to " + this.mocaUrl.getText() + "\n");
			newConn = new HttpConnection(this.mocaUrl.getText());
		} else {
			String[] tokens = this.mocaUrl.getText().split(":");
			String host = tokens[0];
			int port = Integer.parseInt(tokens[1]);
			this.logArea.append("Connecting to " + host + ":" + port + "\n");
			newConn = new DirectConnection(host, port);
		}
		ConnectionUtils.login(newConn, this.mocaUid.getText(), new String(this.mocaPwd.getPassword()));
		return newConn;
	}
	
	private void setLocalPath(String path) {
		File f = new File(path);
		String s;
		try {
			s = f.getCanonicalPath();
			this.localPath.setText(s);
			this.currentLocalDirectory = new File(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setLocalPathManually() {
		File path = new File(this.localPath.getText());
		if (!path.exists()) {
			JOptionPane.showMessageDialog(this, 
					"Path is invalid.", 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
			SwingUtilities.invokeLater(new FocusGrabber(this.localPath));
		} else {
			getLocalDirectoryListing(this.localPath.getText());
		}
	}
	
	private void setRemotePathManually() {
		try {
			conn.executeCommand(String.format("find file where filnam = '%s'", this.remotePath.getText()));
			this.getRemoteDirectoryListing(this.remotePath.getText());
		} catch (MocaException e) {
			JOptionPane.showMessageDialog(this, 
					"Path is invalid.", 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
			SwingUtilities.invokeLater(new FocusGrabber(this.remotePath));
		}
	}
	
	private void performLocalDirectoryChange(int row) {
		String subdirectory = (String)this.localListing.getValueAt(row, AbstractListingTableModel.FILE_NAME_COLUMN_INDEX);
		String newdirectory = String.format("%s/%s", this.localPath.getText(), subdirectory);
		this.getLocalDirectoryListing(newdirectory);		
	}
	
	private void getLocalDirectoryListing(String path) {
		this.currentLocalDirectory = new File(path);
		this.setLocalPath(path);
		
		LocalListingTableModel model = new LocalListingTableModel(currentLocalDirectory);
		this.localListing.setModel(model);	
		this.resetLocalColumnSizes();
	}
	
	private void performRemoteDirectoryChange(int row) {
		
		String subdirectory = (String)this.remoteListing.getValueAt(row, AbstractListingTableModel.FILE_NAME_COLUMN_INDEX);
		String newdirectory = String.format("%s/%s", this.remotePath.getText(), subdirectory);
		this.getRemoteDirectoryListing(newdirectory);
	}
	
	private void getRemoteDirectoryListing(String path) {
		RemoteListingTableModel model = new RemoteListingTableModel(conn, path);
		this.remoteListing.setModel(model);	
		this.remotePath.setText(model.getCurrentPath());
		this.resetRemoteColumnSizes();
	}
	
	private void performUpload() {
		if (this.localListing.getSelectedRowCount() > 0) {
			final String destinationPath = this.remotePath.getText();
			String sourcePath = this.localPath.getText();
			int[] rows = this.localListing.getSelectedRows();
			for (int rowIndex : rows) {
				String fileName = this.localListing.getValueAt(rowIndex, AbstractListingTableModel.FILE_NAME_COLUMN_INDEX).toString();
				final String sourceFile = String.format("%s/%s", sourcePath, fileName);
				final String destinationFile = String.format("%s/%s", destinationPath, fileName);
				//this.logArea.append(String.format("Uploading %s to %s ..... \n", sourceFile, destinationFile));
				String logMessage = String.format("Uploading %s to %s ..... \n", sourceFile, destinationFile);
				this.logArea.append(logMessage);

				//this.logArea.repaint();
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				    @Override
				    public Void doInBackground() {
				    	FileUploader.transferFile(conn, sourceFile, destinationFile);
						return null;
				    }

				    @Override
				    public void done() {
				    	logArea.append("Done!\n");
				    	// TODO:  Refactor this and the download equivalent to their own methods
						RemoteListingTableModel model = new RemoteListingTableModel(conn, destinationPath);
						remoteListing.setModel(model);	
						remotePath.setText(model.getCurrentPath());
						resetRemoteColumnSizes();
				    }
				};
				worker.execute();
			}
			
		} else {
			this.logArea.append("Nothing to upload\n");
		}
	}
	
	private void performDownload() {
		if (this.remoteListing.getSelectedRowCount() > 0) {
			final String destinationPath = this.localPath.getText();
			final String sourcePath = this.remotePath.getText();
			int[] rows = this.remoteListing.getSelectedRows();
			for (int rowIndex : rows) {
				
				
				
				final String fileName = this.remoteListing.getValueAt(rowIndex, AbstractListingTableModel.FILE_NAME_COLUMN_INDEX).toString();
				final String sourceFile = String.format("%s/%s", sourcePath, fileName);
				final String destinationFile = String.format("%s/%s", destinationPath, fileName);
				this.logArea.append(String.format("Downloading %s to %s ..... \n", sourceFile, destinationFile));

				//this.logArea.repaint();
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				    @Override
				    public Void doInBackground() {
				    	// multi-threading, we'll need multiple connections
				    	try {
							MocaConnection connForDownload = makeConnections();
							FileDownloader.transferFile(sourceFile, destinationFile, connForDownload);
						} catch (LoginFailedException e) {
							displayException(e);
						} catch (MocaException e) {
							displayException(e);
						}
						return null;
				    }

				    @Override
				    public void done() {
				    	logArea.append("Done downloading " + fileName + "!\n");
				    	// TODO:  Refactor this and the download equivalent to their own methods
				    	currentLocalDirectory = new File(destinationPath);			
						LocalListingTableModel model = new LocalListingTableModel(currentLocalDirectory);
						localListing.setModel(model);	
						resetLocalColumnSizes();
				    }
				};
				worker.execute();
			}
			
			
			
		} else {
			this.logArea.append("Nothing to download\n");
		}
	}
	
	class FocusGrabber implements Runnable {
		private JComponent component;

		public FocusGrabber(JComponent component) {
			this.component = component;
		}

		public void run() {
			component.grabFocus();
		}
	}
}
