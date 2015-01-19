package iZomateCore.iZTests;

import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteServer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class auvalTest {

	public auvalTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main( String[] args ) throws Exception {
		int testCount= 0;
		String testbed= null;
		String logdir = null;
		HashMap<String, String> params= new HashMap<String, String>();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); //add S for milliseconds
		
		for (int i = 0; i < args.length; ++i)
		{
			if (args[i].equals("-testbed"))
				testbed = args[++i];
			else if (args[i].equals("-logDir"))
				logdir = args[++i];
			else if ( i+1 < args.length && !args[i+1].startsWith( "-" ))
				params.put(args[i], args[++i]);
			else // treat everything else as a boolean
				params.put(args[i], "true");
		}
	
		if (testbed == null)
			throw new Exception("-testbed arg not specified");
	
		if (logdir == null)
			throw new Exception("-logDir arg not specified");
		
		Logs logs = new Logs( logdir, "AuvalTest", 0, true );
			
		StringBuffer stdOut= new StringBuffer();
		StringBuffer stdErr= new StringBuffer();
		
		RemoteServer rs = new RemoteServer(testbed, logs);
		String applescriptCommand= "tell app \"Terminal\"\n" + 
                   				   "activate\n" +
                   				   "do script \"auval -s aufx 2>&1 | tee myOutput.log\"\n" +
                   				   "end tell";

		String applescriptCommand2= "do shellscript \"auval -s aufx > \\\"" + logdir + "/myOutput.log\\\" 2>&1 \"";

		rs._commandLine( new String[] { "osascript", "-e",	applescriptCommand2 }, false );
		rs._commandLine( new String[] { "osascript", "-e",	applescriptCommand }, false );
		rs._commandLine( new String[]{"auval", "-s", "aufx"}, stdOut, stdErr, true );
		System.out.println(stdOut);
		
		Scanner plugins= new Scanner(stdOut.toString());
		while( plugins.hasNextLine() ) {
			String line= plugins.nextLine();
			if( line.contains( "iZtp" ) ) {
				testCount++;
				String pluginName= line.split( "iZotope: " )[1];
				String[] auargs= line.split( " " );
				logs._ResultLog()._skipSpecialCharsFilter()._logMessage( "<font size=\"5\">" + pluginName + "</font>\n" + dateFormat.format(new Date()) + "  Running: '" + "auval -v " + auargs[0] + " " + auargs[1] + " " + auargs[2] + "'" );
				rs._commandLine( new String[]{"auval", "-v", auargs[0], auargs[1], auargs[2]}, stdOut, stdErr, true );
				if( stdOut.indexOf( "AU VALIDATION SUCCEEDED." ) != -1 )
					logs._ResultLog()._logDivData( stdOut.toString(), "<font color=\"green\">Passed</font>", false );
				else
					logs._ResultLog()._logDivData( stdOut.toString() + stdErr.toString(), "<font color=\"red\">FAILED</font>", false )._incrErrorCount();				
			}
		}
		plugins.close();
		
		if( testCount == 0 )
			logs._ResultLog()._logError( "No iZotope plugins were detected!", false );
		
		logs._ResultLog()._printSummary();
	}

}
