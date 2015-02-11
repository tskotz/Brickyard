package AutomationToolbox.src;

import iZomateCore.UtilityCore.TimeUtils;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;

import AutomationToolbox.src.JobRunner.TestInfo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.RandomAccessFile;


public class JobInfoWindow extends JFrame {
	private static final long serialVersionUID= 1L;
	
	private JTable m_tableTests;
	private JTextField m_txtfldUser;
	private JTextField m_txtfldTestbed;
	private JTextField m_txtfldPlatform;
	private JTextField m_txtfldJobFile;
	private JTextField m_txtfldClassPath;
	private JTextField m_txtfldCmdLineArgs;
	private JobRunner  m_JobInfo;
	private File	   m_fIcomingDir;

	/**
	 * 
	 * @param fJobFile
	 * @throws HeadlessException
	 */
	public JobInfoWindow( File fJobFile, File fIncomingDir ) throws HeadlessException {
		setBounds(100, 100, 626, 566);
		this.m_fIcomingDir= fIncomingDir;
		
		this.m_JobInfo= new JobRunner( fJobFile, null, null, null );
		this.setTitle( "Job Info: " + this.m_JobInfo._GetJobFile().getParentFile().getName() );
										
		JLabel lblJobFile = new JLabel("Job File:");		
		this.m_txtfldJobFile = new JTextField( this.m_JobInfo._GetJobFile().getName() );
		this.m_txtfldJobFile.setEditable(false);
		this.m_txtfldJobFile.setColumns(10);

		JLabel lblUser = new JLabel("User:");
		this.m_txtfldUser = new JTextField( this.m_JobInfo.m_strUser );
		//this.m_txtfldUser.setEditable(false);
		this.m_txtfldUser.setColumns(10);
		
		JLabel lblTestbed = new JLabel("Testbed:");
		this.m_txtfldTestbed = new JTextField( this.m_JobInfo.m_strTestbeds.toString().replace( "[", "" ).replace( "]", "" ) );
		//this.m_txtfldTestbed.setEditable(false);
		this.m_txtfldTestbed.setColumns(10);
		
		JLabel lblPlatform = new JLabel("Platform:");
		this.m_txtfldPlatform = new JTextField( "Obsolete" );
		//this.m_txtfldPlatform.setEditable(false);
		this.m_txtfldPlatform.setColumns(10);

		JLabel lblClasspath = new JLabel("Classpath:");	
		this.m_txtfldClassPath = new JTextField( this.m_JobInfo._GetClassPathString() );
		//this.m_txtfldClassPath.setEditable(false);
		this.m_txtfldClassPath.setColumns(10);
		
		JLabel lblCmdLineArgs = new JLabel("Optional Args:");	
		this.m_txtfldCmdLineArgs = new JTextField( this.m_JobInfo._GetCommandLineArgs() );
		//this.m_txtfldCmdLineArgs.setEditable(false);
		this.m_txtfldCmdLineArgs.setColumns(10);

		this.m_tableTests = new JTable( new DefaultTableModel() );
		this.m_tableTests.setFillsViewportHeight(true);
		this.m_tableTests.setBounds(24, 125, 462, 102);
		((DefaultTableModel)this.m_tableTests.getModel()).addColumn("Test");

		JLabel lblTests = new JLabel("Tests");
		JScrollPane m_scrollPaneTests = new JScrollPane( this.m_tableTests );
		
		JButton btnOk = new JButton("Okay");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JobInfoWindow.this.setVisible( false );
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JobInfoWindow.this.setVisible( false );
			}
		});
						
		JButton btnTestInfo = new JButton("Test Info");
		btnTestInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selRow= JobInfoWindow.this.m_tableTests.getSelectedRow();
				if( selRow >= 0 )
					new TestEditorWindow( new File( JobInfoWindow.this.m_tableTests.getValueAt( selRow, 0 ).toString() ) ).setVisible( true );
			}
		});
		
		JButton btnReveal = new JButton("Reveal");
		btnReveal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open( JobInfoWindow.this.m_JobInfo._GetJobFile().getParentFile() );
					JobInfoWindow.this.setVisible( false );
				} catch( Exception e1 ) {
					e1.printStackTrace();
				}
			}
		});
		
		JButton btnRunSelectedTests = new JButton("Run Selected Tests");
		btnRunSelectedTests.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					JobInfoWindow.this._runSelectedTests();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
				
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(323)
					.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
					.addGap(6))
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(24)
							.addComponent(lblPlatform, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(this.m_txtfldPlatform, GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(24)
							.addComponent(lblTestbed, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(this.m_txtfldTestbed, GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(23)
							.addComponent(lblUser, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(this.m_txtfldUser, GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(23)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblJobFile)
									.addGap(18)
									.addComponent(this.m_txtfldJobFile, GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(btnTestInfo)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRunSelectedTests))
								.addComponent(btnReveal)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblClasspath)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(this.m_txtfldClassPath, GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE))
								.addComponent(m_scrollPaneTests, GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
								.addComponent(lblTests)
								.addGroup(groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblCmdLineArgs)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(this.m_txtfldCmdLineArgs, GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)))))
					.addGap(22))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(this.m_txtfldJobFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblJobFile))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(this.m_txtfldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblUser))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(this.m_txtfldTestbed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblTestbed))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(this.m_txtfldPlatform, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPlatform))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblClasspath)
						.addComponent(this.m_txtfldClassPath, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(this.m_txtfldCmdLineArgs, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblCmdLineArgs))
					.addGap(22)
					.addComponent(lblTests)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(m_scrollPaneTests, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnTestInfo)
						.addComponent(btnRunSelectedTests))
					.addGap(30)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnCancel)
							.addComponent(btnReveal))
						.addComponent(btnOk))
					.addGap(12))
		);
		getContentPane().setLayout(groupLayout);
		
		for( TestInfo f : this.m_JobInfo.m_fTests )
			((DefaultTableModel)this.m_tableTests.getModel()).addRow( new Object[]{ (f._DataparamFile().exists()?"":"Not Found: ") + f._DataparamFile().getAbsolutePath() } );	
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	private void _runSelectedTests() throws Exception {
		if( JobInfoWindow.this.m_tableTests.getSelectedRows().length == 0 ) {
			JOptionPane.showMessageDialog( this, "Please select some tests and try again." );			
			return;
		}
		
		String strFName= JobInfoWindow.this.m_txtfldUser.getText().replace( " ", "" ) + "_" + TimeUtils.getDateTime().replace( " ", "_" ).replace( "-", "." ).replace( "/", "." ).replace( ":", "." ) + ".job.xml"; 
		File f= new File( JobInfoWindow.this.m_fIcomingDir, strFName );
		RandomAccessFile rf= new RandomAccessFile( f, "rw");
		
		try {
			rf.writeBytes( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" );
			rf.writeBytes( "<Job>\n" );
			rf.writeBytes( "    <Timestamp>" + TimeUtils.getDateTime().replace( "-", "/" ) + "</Timestamp>\n" );
			rf.writeBytes( "    <User>" + JobInfoWindow.this.m_txtfldUser.getText() + "</User>\n" );
			rf.writeBytes( "    <Platform>" + JobInfoWindow.this.m_txtfldPlatform.getText() + "</Platform>\n" );
			rf.writeBytes( "    <Testbed>" + JobInfoWindow.this.m_txtfldTestbed.getText() + "</Testbed>\n" );
			rf.writeBytes( "    <Classpath>" + JobInfoWindow.this.m_txtfldClassPath.getText().replace( JobRunner.NO_CLASSPATH_SPECIFIED, "" ) + "</Classpath>\n" );
			rf.writeBytes( "    <CommandLineArgs>" + JobInfoWindow.this.m_txtfldCmdLineArgs.getText().replace( JobRunner.NO_CMD_LINE_ARGS_SPECIFIED, "" ) + "</CommandLineArgs>\n" );
			
			for( int r : JobInfoWindow.this.m_tableTests.getSelectedRows() )
				rf.writeBytes( "    <DataParamFile>" + JobInfoWindow.this.m_tableTests.getValueAt( r, 0 ).toString() + "</DataParamFile>\n" );
			
			rf.writeBytes( "</Job>" ); 			
			JobInfoWindow.this.setVisible( false );
		} finally {
			rf.close();
		}
	}
}
