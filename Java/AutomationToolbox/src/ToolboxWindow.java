package AutomationToolbox.src;

import iZomateCore.UtilityCore.TimeUtils;
import iZomateRemoteServer.RemoteServerMain;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Timer;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * 
 * @author tskotz
 *
 */
public class ToolboxWindow extends JFrame implements ActionListener
{
	static int COL_STATUS= 		0;
	static int COL_USER= 		1;
	static int COL_TESTBED= 	2;
	static int COL_REQUEST= 	3;

	private static final long 	serialVersionUID = 1L;
	public  static final String	m_strVersion= "1.083";

	private JPanel 			m_pnlContentPane;
	private Thread 			m_threadRemoteServer= null;
	private JButton 		m_btnStartRemoteServer= null;
	private JTextField 		m_txtFldRemoteServerPort;
	private Timer			m_timerTestManager= null;
	private JTable 			m_tableTestManager= null;
	private JTextField 		m_txtfldStagingDir;
	private JButton 		m_btnStartManager= null;
	private JButton 		m_btnStartWebServer= null;
	private JButton 		m_btnOpenPrefsPage= null;
	private JTextField 		m_txtFldHttpPort= null;
	final 	JPanel 			m_TestManagerPanel = new JPanel();
	private ToolboxHTTPServer	m_ToolboxHTTPServer= null;

	private static File		sfStagingDir;
	private static File 	sfIncomingDir;
	private static File 	sfQueuedDir;
	private static File 	sfRunningDir;
	private static File 	sfCompletedDir;
	private static File 	sfRetiredDir;
	
	private	Map<String, Boolean>	m_ActiveTestbeds= new HashMap<String, Boolean>();
	private HttpServer 				m_httpserver= null;
	final 	JPanel 					m_WebServerPanel = new JPanel();


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ToolboxWindow frame = new ToolboxWindow();
					frame.setVisible(true);
					frame._startWebServer();
					frame._startRemoteServer();
					frame._StartTestManagerOnLaunch();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	private void _startWebServer() {
	    System.out.println( "\n/**********************************************\\" );
	    System.out.println( "|*               ToolboxHTTPServer             *|" );
	    System.out.println( "\\**********************************************/" );
	    System.out.println( "ToolboxHTTPServer Version: " + ToolboxHTTPServer.VERSION );

	    InetSocketAddress addr= new InetSocketAddress( Integer.parseInt(this.m_txtFldHttpPort.getText() ) );
	    try {
	    	if( this.m_ToolboxHTTPServer == null )
	    		this.m_ToolboxHTTPServer= new ToolboxHTTPServer( this.m_txtFldHttpPort.getText() );
	    	
	    	this.m_ToolboxHTTPServer._SetPort( this.m_txtFldHttpPort.getText() );
	    	this.m_txtFldHttpPort.setEnabled( false );
	    	this.m_btnOpenPrefsPage.setEnabled( true );
	    	
			this.m_httpserver= HttpServer.create( addr, 0 );
		    this.m_httpserver.createContext( "/", this.m_ToolboxHTTPServer );
		    this.m_httpserver.setExecutor( Executors.newCachedThreadPool() );
		    this.m_httpserver.start();
		    System.out.println( "ToolboxHTTPServer is listening on port " + this.m_txtFldHttpPort.getText() + "\n" );
			// Set button text to Stop
			this.m_btnStartWebServer.setText( "Stop Server" );		
			// Store the port
			DatabaseMgr._Preferences()._PutPref( Preferences.pvtWebServerPort, this.m_txtFldHttpPort.getText() );
		} catch (IOException e) {
		    System.out.println( "ToolboxHTTPServer failed to start:" );
			e.printStackTrace();
			this._stopWebServer();
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	private void _stopWebServer() {
		if( this.m_httpserver != null )
			this.m_httpserver.stop(2);
	    System.out.println( "Web server has been stopped" );
	    
		// Set button text to Start
		this.m_btnStartWebServer.setText( "Start Server" );
    	this.m_txtFldHttpPort.setEnabled( true );
    	this.m_btnOpenPrefsPage.setEnabled( false );
	}

	/**
	 * Create the frame.
	 */
	public ToolboxWindow() {
		this._setLookAndFeel(); //Default windows theme is LAME! so change it to nimbus

		setTitle("Test Automation Toolbox " + ToolboxWindow.m_strVersion );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 500);
		this.m_pnlContentPane= new JPanel();
		this.m_pnlContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(this.m_pnlContentPane);
		
		JTabbedPane tabbedPane= new JTabbedPane(JTabbedPane.TOP);	
		this._configureTestManagerPanel( tabbedPane );
		this._configureRemoteServerPanel( tabbedPane );
		this._configureWebServerPanel( tabbedPane );
		
		GroupLayout gl_m_pnlContentPane = new GroupLayout( this.m_pnlContentPane );
		gl_m_pnlContentPane.setHorizontalGroup(
			gl_m_pnlContentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_m_pnlContentPane.createSequentialGroup()
					.addGap(1)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
					.addGap(1))
		);
		gl_m_pnlContentPane.setVerticalGroup(
			gl_m_pnlContentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_m_pnlContentPane.createSequentialGroup()
					.addGap(1)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
					.addGap(1))
		);
		this.m_pnlContentPane.setLayout(gl_m_pnlContentPane);
	}
	
	/**
	 * 
	 * @param tabbedPane
	 */
	private void _configureTestManagerPanel( JTabbedPane tabbedPane ) {
        /*****************************************************
         * Test Manager Panel
         *****************************************************/
		tabbedPane.addTab("Test Manager", null, this.m_TestManagerPanel, null);
		
		JButton btnSet = new JButton("Staging Dir:");
		btnSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ToolboxWindow.this._setStagingDir();
			}
		});
		
		this.m_txtfldStagingDir = new JTextField();
		this.m_txtfldStagingDir.setEditable(false);

		if( DatabaseMgr._Preferences()._GetPref( Preferences.StagingDir ).isEmpty() )
			DatabaseMgr._Preferences()._PutPref( Preferences.StagingDir, "./AutomationToolbox/ManagerStagingDirs" );
		
		sfStagingDir= new File( DatabaseMgr._Preferences()._GetPref( Preferences.StagingDir ).replace( "./", System.getProperty("user.dir") + "/" ) );
		
		if( !sfStagingDir.exists() )
			sfStagingDir.mkdirs();
			
		if( sfStagingDir.exists() )
			this.m_txtfldStagingDir.setText( sfStagingDir.getAbsolutePath() );
		
        this.m_tableTestManager= new JTable( new DefaultTableModel() );
        // Create a couple of columns
        ((DefaultTableModel)this.m_tableTestManager.getModel()).addColumn("Status");
        ((DefaultTableModel)this.m_tableTestManager.getModel()).addColumn("User");
        ((DefaultTableModel)this.m_tableTestManager.getModel()).addColumn("Testbed");
        ((DefaultTableModel)this.m_tableTestManager.getModel()).addColumn("Test Request");
        
        if( System.getProperty("os.name").equals( "Mac OS X" ) ) {
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_STATUS  ).setMinWidth( 	  100 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_STATUS  ).setMaxWidth( 	  100 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_USER    ).setMaxWidth(		  250 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_USER    ).setPreferredWidth( 220 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_TESTBED ).setPreferredWidth( 150 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_REQUEST ).setPreferredWidth( 400 );
        }
        else {
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_STATUS  ).setMinWidth( 	  100 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_STATUS  ).setMaxWidth( 	  100 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_USER    ).setMaxWidth(		  240 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_USER    ).setPreferredWidth( 180 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_TESTBED ).setPreferredWidth( 100 );
	        this.m_tableTestManager.getTableHeader().getColumnModel().getColumn( COL_REQUEST ).setPreferredWidth( 400 );
        }
        this.m_tableTestManager.setFillsViewportHeight(true);
        		
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPaneTestManager = new JScrollPane( this.m_tableTestManager );
        
        // Create the Start Stop toggle button
        this.m_btnStartManager= new JButton("Start Manager");
        this.m_btnStartManager.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if( ((JButton)e.getSource()).getText().equals( "Start Manager" ) )
        			ToolboxWindow.this._startTestManagerTimer();
        		else 
        			ToolboxWindow.this._stopTestManagerTimer();
        	}
        });
        
        JButton btnJobStop = new JButton("Stop");
        btnJobStop.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		ToolboxWindow.this._stopJob( ToolboxWindow.this.m_tableTestManager.getSelectedRows() );
        	}
        });
        
        JButton btnJobRun = new JButton("Run");
        btnJobRun.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		ToolboxWindow.this._runJob( ToolboxWindow.this.m_tableTestManager.getSelectedRows() );
        	}
        });
        
        JButton btnJobInfo = new JButton("Info");
        btnJobInfo.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		ToolboxWindow.this._showJobInfo( ToolboxWindow.this.m_tableTestManager.getSelectedRows() );
        	}
        });
        
        JButton btnJobLog = new JButton("Log");
        btnJobLog.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		ToolboxWindow.this._showJobLog( ToolboxWindow.this.m_tableTestManager.getSelectedRows() );
        	}
        });
        
        JButton btnStatus = new JButton("Status");
        btnStatus.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		ToolboxWindow.this._showJobStatus( ToolboxWindow.this.m_tableTestManager.getSelectedRows() );
        	}
        });
        
        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		ToolboxWindow.this._deleteJob( ToolboxWindow.this.m_tableTestManager.getSelectedRows() );
        	}
        });
        
        GroupLayout gl_m_TestManagerPanel = new GroupLayout( this.m_TestManagerPanel );
        gl_m_TestManagerPanel.setHorizontalGroup(
        	gl_m_TestManagerPanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_m_TestManagerPanel.createSequentialGroup()
        			.addGroup(gl_m_TestManagerPanel.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_m_TestManagerPanel.createSequentialGroup()
        					.addGap(4)
        					.addComponent(btnSet)
        					.addGap(12)
        					.addComponent(this.m_txtfldStagingDir, GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE))
        				.addGroup(gl_m_TestManagerPanel.createSequentialGroup()
        					.addGap(10)
        					.addGroup(gl_m_TestManagerPanel.createParallelGroup(Alignment.TRAILING)
        						.addGroup(gl_m_TestManagerPanel.createSequentialGroup()
        							.addGap(6)
        							.addComponent(btnJobRun)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(btnJobStop)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(btnDelete)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(btnJobInfo)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(btnJobLog)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(btnStatus)
        							.addPreferredGap(ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
        							.addComponent(this.m_btnStartManager))
        						.addComponent(scrollPaneTestManager, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 673, Short.MAX_VALUE))))
        			.addGap(10))
        );
        gl_m_TestManagerPanel.setVerticalGroup(
        	gl_m_TestManagerPanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_m_TestManagerPanel.createSequentialGroup()
        			.addGroup(gl_m_TestManagerPanel.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_m_TestManagerPanel.createSequentialGroup()
        					.addGap(1)
        					.addComponent(btnSet))
        				.addComponent(this.m_txtfldStagingDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(12)
        			.addComponent(scrollPaneTestManager, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
        			.addGap(12)
        			.addGroup(gl_m_TestManagerPanel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(this.m_btnStartManager)
        				.addComponent(btnJobRun)
        				.addComponent(btnJobStop)
        				.addComponent(btnDelete)
        				.addComponent(btnJobInfo)
        				.addComponent(btnJobLog)
        				.addComponent(btnStatus))
        			.addGap(5))
        );
        this.m_TestManagerPanel.setLayout(gl_m_TestManagerPanel);
	}
	
	/**
	 * 
	 * @param tabbedPane
	 */
	private void _configureRemoteServerPanel( JTabbedPane tabbedPane ) {
        /*****************************************************
         * Remote Server Panel
         *****************************************************/
		JPanel remoteServerPanel = new JPanel();
		tabbedPane.addTab("Remote Server", null, remoteServerPanel, null);

		this.m_btnStartRemoteServer = new JButton("Start Remote Server");
		this.m_btnStartRemoteServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( ((JButton)e.getSource()).getText().equals( "Start Remote Server" ))
					ToolboxWindow.this._startRemoteServer();
				else
					ToolboxWindow.this._stopRemoteServer();
			}
		});
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(textArea);
				
		this._redirectSystemStreams( textArea );	
		
		m_txtFldRemoteServerPort = new JTextField();
		m_txtFldRemoteServerPort.setText( DatabaseMgr._Preferences()._GetPref( Preferences.pvtRemoteServerPort ) );
		m_txtFldRemoteServerPort.setColumns(10);
		
		JLabel lblPort_1 = new JLabel("Port:");
		GroupLayout gl_remoteServerPanel = new GroupLayout(remoteServerPanel);
		gl_remoteServerPanel.setHorizontalGroup(
			gl_remoteServerPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_remoteServerPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_remoteServerPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_remoteServerPanel.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 855, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(gl_remoteServerPanel.createSequentialGroup()
							.addGap(17)
							.addComponent(lblPort_1)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(m_txtFldRemoteServerPort, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
							.addGap(79)
							.addComponent(m_btnStartRemoteServer, GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
							.addGap(251))))
		);
		gl_remoteServerPanel.setVerticalGroup(
			gl_remoteServerPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_remoteServerPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_remoteServerPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(m_btnStartRemoteServer)
						.addComponent(lblPort_1)
						.addComponent(m_txtFldRemoteServerPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		remoteServerPanel.setLayout(gl_remoteServerPanel);
	}

	/**
	 * 
	 * @param tabbedPane
	 */
	private void _configureWebServerPanel( JTabbedPane tabbedPane ) {
        /*****************************************************
         * Test Manager Panel
         *****************************************************/
		tabbedPane.addTab("Web Server", null, this.m_WebServerPanel, null);
		
		JLabel lblPort = new JLabel("Port:");
		
		m_txtFldHttpPort = new JTextField();
		m_txtFldHttpPort.setText( DatabaseMgr._Preferences()._GetPref( Preferences.pvtWebServerPort ) );
		m_txtFldHttpPort.setColumns(10);
		
		m_btnStartWebServer = new JButton("Start Server");
		m_btnStartWebServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
        		if( ((JButton)e.getSource()).getText().equals( "Start Server" ) )
        			ToolboxWindow.this._startWebServer();
        		else 
        			ToolboxWindow.this._stopWebServer();
			}
		});
		
		m_btnOpenPrefsPage = new JButton("Open Preferences Page");
		m_btnOpenPrefsPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ToolboxWindow.this.m_ToolboxHTTPServer._OpenPreferencesPage();
			}
		});
		
		GroupLayout gl_m_WebServerPanel = new GroupLayout(m_WebServerPanel);
		gl_m_WebServerPanel.setHorizontalGroup(
			gl_m_WebServerPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_m_WebServerPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_m_WebServerPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_m_WebServerPanel.createSequentialGroup()
							.addComponent(lblPort)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(m_txtFldHttpPort, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(m_btnStartWebServer))
						.addComponent(m_btnOpenPrefsPage))
					.addContainerGap(640, Short.MAX_VALUE))
		);
		gl_m_WebServerPanel.setVerticalGroup(
			gl_m_WebServerPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_m_WebServerPanel.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_m_WebServerPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPort)
						.addComponent(m_txtFldHttpPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(m_btnStartWebServer))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(m_btnOpenPrefsPage)
					.addContainerGap(344, Short.MAX_VALUE))
		);
		m_WebServerPanel.setLayout(gl_m_WebServerPanel);

	}

	/**
	 * 
	 */
	private void _setStagingDir() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		chooser.setCurrentDirectory( ToolboxWindow.sfStagingDir );
	    int returnVal = chooser.showOpenDialog( this.m_TestManagerPanel );
	    if( returnVal == JFileChooser.APPROVE_OPTION ) {
	    	sfStagingDir= chooser.getSelectedFile();
	    	this.m_txtfldStagingDir.setText( ToolboxWindow.sfStagingDir.getAbsolutePath() );
	    	this._createStagingDirs();
	    }
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean _createStagingDirs() {
    	if( !sfStagingDir.exists() ) {
        	System.out.println( "Staging dir does not exist! : " + sfStagingDir.getAbsolutePath() );
    		return false;  		
    	}		
		
    	sfIncomingDir=  new File( ToolboxWindow.sfStagingDir.getAbsoluteFile() + "/Incoming" );
    	sfQueuedDir=    new File( ToolboxWindow.sfStagingDir.getAbsoluteFile() + "/Queued" );
    	sfRunningDir=   new File( ToolboxWindow.sfStagingDir.getAbsoluteFile() + "/Running" );
    	sfCompletedDir= new File( ToolboxWindow.sfStagingDir.getAbsoluteFile() + "/Completed" );
    	sfRetiredDir=   new File( ToolboxWindow.sfStagingDir.getAbsoluteFile() + "/Retired" );
    	
    	sfIncomingDir.mkdir();
    	sfQueuedDir.mkdir();
    	sfRunningDir.mkdir();
    	sfCompletedDir.mkdir();
    	sfRetiredDir.mkdirs();
    	
    	return true;
	}
	
	/**
	 * 
	 */
	public void _StartTestManagerOnLaunch() {
		if( DatabaseMgr._Preferences()._GetPrefBool( Preferences.StartTestManagerOnLaunch ) )
			this._startTestManagerTimer();
	}
	
	/**
	 * 
	 */
	private void _startTestManagerTimer() {
		if( this.m_timerTestManager == null ) {
			//Set up timer to drive animation events.
			this.m_timerTestManager= new Timer( 1000, this );
			this.m_timerTestManager.setInitialDelay( 1000 );
		}
		
		if( !this.m_timerTestManager.isRunning() ) {
			this._createStagingDirs();
			
			this.m_timerTestManager.start();
			System.out.println( "The Test Manager started!" );
		}
		else
			System.out.println( "The Test Manager is already running!" );
		
		// Set button text to Stop
		this.m_btnStartManager.setText( "Stop Manager" );
	}
	
	/**
	 * 
	 */
	private void _stopTestManagerTimer() {
		if( this.m_timerTestManager != null ) {
			System.out.println( "The Test Manager stopped!" );
			this.m_timerTestManager.stop();
		}
		else
			System.out.println( "The Test Manager is already running!" );
		
		// Set button text to Start
		this.m_btnStartManager.setText( "Start Manager" );
	}
	

	/**
	 * 
	 * @param nRow
	 */
	private void _runJob( int[] nRows ) {
		if( nRows != null && nRows.length > 0 ) {
			for( int nRow : nRows ) {
				// Only run completed jobs
				if( ToolboxWindow.this.m_tableTestManager.getValueAt( nRow, COL_STATUS ).toString().equals( sfCompletedDir.getName() ) ) {
	    			for( File f : this._getJobDir( nRow ).listFiles() ) {
	    				if( f.getName().endsWith( ".job.xml" )) {
	    					try {
	    						// All this to copy a file!
								File fTemp= new File( f.getParentFile(), "tmp.xml" );
								fTemp.createNewFile();
						        
								FileInputStream inStream= new FileInputStream( f );
								FileOutputStream outStream= new FileOutputStream( fTemp );
								FileChannel in = inStream.getChannel();
						        FileChannel out = outStream.getChannel();
						        
						        out.transferFrom( in, 0, in.size() );
						        
						        out.close();
						        in.close();						        
						        outStream.close();
						        inStream.close();
						        
						        fTemp.renameTo( new File( sfIncomingDir, f.getName() ) );
						        ToolboxWindow.this.m_tableTestManager.getSelectionModel().removeSelectionInterval( nRow, nRow );						        
						    } catch( IOException e ) {
								e.printStackTrace();
							}
	    				}
	    			}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param nRow
	 */
	private void _stopJob( int[] nRows ) {
		if( nRows != null && nRows.length > 0 ) {
			for( int nRow : nRows ) {
				File fDir= new File( this.m_txtfldStagingDir.getText() + "/" + 
									 this.m_tableTestManager.getValueAt( nRow, COL_STATUS ).toString() + "/" +
									 this.m_tableTestManager.getValueAt( nRow, COL_REQUEST ).toString() );
				JobRunner jobRunner= new JobRunner( fDir, sfRunningDir, sfCompletedDir, null );
				if( jobRunner._Kill() ) {
					ToolboxWindow.this._removeTestbeds( jobRunner.m_strTestbeds );
			        ToolboxWindow.this.m_tableTestManager.getSelectionModel().removeSelectionInterval( nRow, nRow );
				}
			}
		}
	}

	/**
	 * 
	 * @param nRow
	 */
	private void _showJobInfo( int[] nRows ) {
		if( nRows != null && nRows.length > 0 ) {
			for( int nRow : nRows ) {
				JobInfoWindow je= new JobInfoWindow( this._getJobDir( nRow ), sfIncomingDir );
				je.setVisible( true );
			}
		}
	}
	
	/**
	 * 
	 * @param nRow
	 */
	private void _showJobLog( int[] nRows ) {
		if( nRows != null && nRows.length > 0 ) {
			for( int nRow : nRows ) {
				File fLogFile= new JobRunner( this._getJobDir( nRow ), null, null, null )._GetOutputFile();
				try {
					if( fLogFile.exists() ) {
						if( System.getProperty("os.name").contains( "Mac OS X" ) ) 
							new ProcessBuilder( "/bin/bash", "-c", "open \"" + fLogFile.getAbsolutePath() + "\"" ).start();
						else 
							Runtime.getRuntime().exec( new String[]{"cmd", "/c", fLogFile.getAbsolutePath()} );
					}
					else
						JOptionPane.showMessageDialog( this, "No Log File Found in Dir:\n" + this._getJobDir( nRow ).getAbsolutePath() );

				} catch( IOException e1 ) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog( this, e1.getMessage() );
				}
			}
		}
	}
	
	/**
	 * 
	 * @param nRow
	 */
	private void _showJobStatus( int[] nRows ) {
		if( nRows != null && nRows.length > 0 ) {
			for( int nRow : nRows ) {
				ArrayList<File> fResultsFiles= new JobRunner( this._getJobDir( nRow ), null, null, null )._GetResultsFile();
				try {
					if( !fResultsFiles.isEmpty() ) {
						for( File fResFile : fResultsFiles ) {
							if( System.getProperty("os.name").contains( "Mac OS X" ) ) 
								new ProcessBuilder( "/bin/bash", "-c", "open \"" + fResFile.getAbsolutePath() + "\"" ).start();
							else
								Runtime.getRuntime().exec( new String[]{"C:/Program Files/Internet Explorer/iexplore.exe", fResFile.getAbsolutePath()} );
						}
					}
					else
						JOptionPane.showMessageDialog( this, "No Result File Found in Dir:\n" + this._getJobDir( nRow ).getAbsolutePath() );

				} catch( IOException e1 ) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog( this, e1.getMessage() );
				}
			}
		}
	}
	
	/**
	 * 
	 * @param nRow
	 */
	private void _deleteJob( int[] nRows ) {
		if( nRows != null && nRows.length > 0 ) {
			for( int nRow : nRows ) {
        		ToolboxWindow.this._deleteFile( ToolboxWindow.this._getJobDir( nRow ), true );
		        ToolboxWindow.this.m_tableTestManager.getSelectionModel().removeSelectionInterval( nRow, nRow );
			}
		}
	}

	/**
	 * 
	 * @param nRow
	 */
	private void _deleteFile( File fFile, boolean bGetConfirmation ) {
		try {
			boolean bDelete= !bGetConfirmation;
			
			if( bGetConfirmation ) {
				if( fFile.getAbsolutePath().contains( sfCompletedDir.getAbsolutePath() ) )
					bDelete= JOptionPane.showConfirmDialog( null, "Are you sure you want to delete:\n" + fFile.getName(), "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION;
			}
			
			if( bDelete && fFile != null ) {
				// Delete contents first
				if( fFile.isDirectory() )
					for( File f : fFile.listFiles() )
						this._deleteFile( f, false );
				// Now delete the empty dir
				fFile.delete();
			}			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param nRow
	 * @return
	 */
	private File _getJobDir( int nRow ) {
		return new File( this.m_txtfldStagingDir.getText() + "/" + 
						 this.m_tableTestManager.getValueAt( nRow, COL_STATUS ).toString() + "/" +
						 this.m_tableTestManager.getValueAt( nRow, COL_REQUEST ).toString() );
	}
	
    /**
     * Handle timer event.
     */ 
    public void actionPerformed( ActionEvent e ) {
    	this._ScanFolders();
    }
    
    /**
     * 
     */
    public void _ScanFolders() {
    	try {
        	//System.out.println( "Running Test Manager" );
        	boolean bRescan= false;
      	
        	if( !sfStagingDir.exists() ) {
            	System.out.println( "Staging dir does not exist! : " + sfStagingDir.getAbsolutePath() );
        		return;  		
        	}
        	        	    	
    		do {
    			bRescan= false;
    			int completedCount= 0;
    			int rowNum= 0;
    			int numRows= ((DefaultTableModel)this.m_tableTestManager.getModel()).getRowCount();
    			int nShowJobCount= DatabaseMgr._Preferences()._GetPrefInt( Preferences.ShowJobCount );
    			JobRunner jobRunner;

    			// Folders will be processed in the order they appear in list and that order is very important
    			for( File stagingDir : new File[]{sfRunningDir, sfQueuedDir, sfIncomingDir, sfCompletedDir} ) {
    				File[] fFiles= stagingDir.listFiles();
    				
    				// Sort items by date
    				if( stagingDir.equals( sfCompletedDir ) ) {
    					// Sort starting with most recent 
	    				Arrays.sort(fFiles, new Comparator<File>(){
	    				    public int compare(File f1, File f2) {
	    				        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
	    				    } 
	    				 });
    				}
    				else {
    					// Sort starting with oldest
	    				Arrays.sort(fFiles, new Comparator<File>(){
	    				    public int compare(File f1, File f2) {
	    				        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
	    				    } 
	    				 });   					
    				}
    				
    				for( File fItem : fFiles) {
    					jobRunner= null;
    					if( fItem.getName().equals( ".DS_Store" ))
    						continue;  // ignore it
    					// Handle Queued
    					if( stagingDir.equals( sfQueuedDir ) && fItem.isDirectory() )
    						jobRunner= this._HandleJobRequest( fItem );
    					// Handle Incoming
    					else if( stagingDir.equals( sfIncomingDir ) && fItem.isFile())
    						jobRunner= this._HandleJobRequest( fItem );
    					// Handle Completed
    					else if( stagingDir.equals( sfCompletedDir ) && fItem.isDirectory() ) {
    						jobRunner= new JobRunner( fItem, sfRunningDir, sfCompletedDir, sfQueuedDir );
    						if( ++completedCount > nShowJobCount || jobRunner.m_bErrors )
    							jobRunner._Retire( sfRetiredDir ); //only show the last n completed
    					}
    					// Handle Running
    					else if( stagingDir.equals( sfRunningDir ) && fItem.isDirectory() ) {
    						jobRunner= new JobRunner( fItem, sfRunningDir, sfCompletedDir, sfQueuedDir );
    						if( jobRunner._IsRunning() ) 
    							this._addTestbeds( jobRunner.m_strTestbeds );
    						else {
    							jobRunner._CleanUp();
    							this._removeTestbeds( jobRunner.m_strTestbeds );
    						}
    					}
    					//ignore it
    					else 
        					continue;
    					
    					if( jobRunner._IsValid() ) {    						    					
	    					//Update Table
	    					if( rowNum++ >= numRows ) {
	    						((DefaultTableModel)this.m_tableTestManager.getModel()).addRow( new Object[]{} );
	    						numRows= ((DefaultTableModel)this.m_tableTestManager.getModel()).getRowCount();
	    					}
	    					
	        				((DefaultTableModel)this.m_tableTestManager.getModel()).setValueAt( jobRunner._GetStatusString(), 	rowNum-1, COL_STATUS );
	        				((DefaultTableModel)this.m_tableTestManager.getModel()).setValueAt( jobRunner._GetJobID(), 			rowNum-1, COL_REQUEST );
	        				((DefaultTableModel)this.m_tableTestManager.getModel()).setValueAt( jobRunner.m_strUser, 	  		rowNum-1, COL_USER );
	    					((DefaultTableModel)this.m_tableTestManager.getModel()).setValueAt( jobRunner.m_strTestbeds, 		rowNum-1, COL_TESTBED );
	    					//((DefaultTableModel)this.m_tableTestManager.getModel()).fireTableDataChanged();
    					}
    				}
    				
    				if( bRescan )
    					break;
    	    	}
    	    	//Remove obsolete rows
    	    	for( int i= rowNum; i < numRows; ++i )
    	    		((DefaultTableModel)this.m_tableTestManager.getModel()).removeRow( rowNum );  		
    		}
    		while( bRescan );    		
    	}
    	catch( Exception ex ) {
    		System.out.println( ex.getMessage() );
    	}
    }
       
    /**
     * 
     * @param jobFile
     * @return
     */
    private JobRunner _HandleJobRequest( File jobFile ) {
    	JobRunner jobRunner= new JobRunner( jobFile, sfRunningDir, sfCompletedDir, sfQueuedDir );
    	// If this is not a valid job request then move it directly to completed
    	if( !jobRunner._IsValid() )
        	jobRunner._CleanUp();
    	// Check to see if this testbed is already in use
    	else if( this._areTestbedsInUse( jobRunner.m_strTestbeds ) )
			jobRunner._QueueJob();
    	//Kick it off and add it to our list of running jobs
		else {
			try { TimeUtils.sleep( .5 ); } catch( Exception e ) { e.printStackTrace(); } //Give it a bit of time to finish copying
			if( jobRunner._Run() != null )
				this._addTestbeds( jobRunner.m_strTestbeds );
		}
		
		return jobRunner;
    }
    
    /**
     * 
     */
    private boolean _areTestbedsInUse( List<String> strTestbeds ) {
    	for( String strTestbed : strTestbeds )
    		if( this.m_ActiveTestbeds.containsKey( strTestbed ) )
    			return true;
    	return false;
    }
    
    /**
     * 
     */
    private void _removeTestbeds( List<String> strTestbeds ) {
    	for( String strTestbed : strTestbeds )
			ToolboxWindow.this.m_ActiveTestbeds.remove( strTestbed );
    }

    /**
     * 
     */
    private void _addTestbeds( List<String> strTestbeds ) {
    	for( String strTestbed : strTestbeds )
    		if( !this.m_ActiveTestbeds.containsKey( strTestbed ) )
    			ToolboxWindow.this.m_ActiveTestbeds.put( strTestbed, true );
    }
    
	/**
	 * 
	 */
	private void _startRemoteServer( ) {
		if( this.m_threadRemoteServer == null ) {
			try {
				int iPort= Integer.valueOf( this.m_txtFldRemoteServerPort.getText() );
				this.m_threadRemoteServer = new Thread( new RemoteServerMain( iPort ), "RemoteServer");
				this.m_threadRemoteServer.start();
			} catch (Exception e) {
				e.printStackTrace();
				this.m_threadRemoteServer= null;
			}
		}
		else
			System.out.println("Remote Server is already running!");
		
		if( this.m_threadRemoteServer != null && this.m_threadRemoteServer.isAlive() ) {
			DatabaseMgr._Preferences()._PutPref( Preferences.pvtRemoteServerPort, this.m_txtFldRemoteServerPort.getText() );
			this.m_btnStartRemoteServer.setText( "Stop Remote Server" );
			this.m_txtFldRemoteServerPort.setEnabled( false );
		}
		else
			this.m_threadRemoteServer= null;	
	}

	/**
	 * 
	 */
	private void _stopRemoteServer( ) {
		if( this.m_threadRemoteServer != null && this.m_threadRemoteServer.isAlive() ) {
			try {
				int iPort= DatabaseMgr._Preferences()._GetPrefInt( Preferences.pvtRemoteServerPort );
				// Connect to Remote Server and send it a stop message
				Socket socket= new Socket();
				socket.connect( new InetSocketAddress( "127.0.0.1", iPort), 2000 );
				socket.getOutputStream().write( "stop".getBytes() );
				socket.close();
				
			} catch( IOException e ) {
				e.printStackTrace();
			}
		}
		else
			System.out.println("Remote Server has already stopped!");
		
		this.m_threadRemoteServer= null;
		this.m_btnStartRemoteServer.setText( "Start Remote Server" );
		this.m_txtFldRemoteServerPort.setEnabled( true );
	}

	/**
	 * 
	 * @param textArea
	 */
	private void _redirectSystemStreams( final JTextArea textArea ) {
		OutputStream out = new OutputStream() {
	        @Override
	        public void write(final int b) throws IOException {
	        	textArea.append(String.valueOf((char) b));
	        }

	        @Override
	        public void write(byte[] b, int off, int len) throws IOException {
	        	if( textArea.getDocument().getLength() > 5000000 )
					try {
						textArea.getDocument().remove( 0, 1000000 );
					} catch( BadLocationException e ) {
						e.printStackTrace();
					}
	        	textArea.append(new String(b, off, len));
	        	textArea.setCaretPosition( textArea.getDocument().getLength() ); // Scroll to bottom
	        }

	        @Override
	        public void write(byte[] b) throws IOException {
	        	this.write(b, 0, b.length);
	        }
	    };

	    System.setOut(new PrintStream(out, true));
	    System.setErr(new PrintStream(out, true));
    }
	
	/**
	 * 
	 */
	private void _setLookAndFeel() {
		//Default windows theme is LAME! so change it to nimbus
		if( !System.getProperty("os.name").contains( "Mac OS X" ) ) 
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            try {
						UIManager.setLookAndFeel(info.getClassName());
					} catch ( Exception e ) { 
						e.printStackTrace();
					}
		            break;
		        }
		    }
	}
	
	// Static Accessor Methods for the Staging dirs
	public static File _IncomingDir() {
		return ToolboxWindow.sfIncomingDir;
	}
	public static File _QueuedDir() {
		return ToolboxWindow.sfQueuedDir;
	}
	public static File _RunningDir() {
		return ToolboxWindow.sfRunningDir;
	}
	public static File _CompletedDir() {
		return ToolboxWindow.sfCompletedDir;
	}
	public static File _RetiredDir() {
		return ToolboxWindow.sfRetiredDir;
	}
}
