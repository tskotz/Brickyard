package iZomateCore.LogCore;

import iZomateCore.LogCore.ResultLog.ResultLog;

/**
 * The Logs class is a wrapper for the creation of the core ResultsLog and Transaction log classes.
 * The core logs should always be created and accessed through the Logs class.
 */
public final class Logs
{
	private ResultLog resultLog = null;
	private TransactionLog transLog = null;
	private String logsDir;
	private String logsName;

	/**
	 * Constructor
	 *
	 * @param LogDir the directory where the log files should be written to
	 * @param LogName the name to use for the log files
	 * @param testCases the total number of testCases in the dataparam
	 * @param echo true to echo the result log output to the console
	 * @throws Exception
	 */
	public Logs(String LogDir, String LogName, int testCases, boolean echo) throws Exception
	{
		this.logsDir = LogDir!=null ? LogDir : "~";
		this.logsName = LogName!=null ? LogName : "";
		
		this.logsDir = LogDir.replace( "~", "/Users/" + System.getenv( "USER" ) );

		this.resultLog = new ResultLog(this.logsName + "_ResultLog", this.logsDir, testCases, echo);
		this.transLog = new TransactionLog(this.logsName + "_TransactionLog", this.logsDir);
		this.resultLog._SetTransactionLog( this.transLog );
	}

	/**
	 * Returns the path to the directory containing the log files.
	 *
	 * @return the path to the directory containing the log files
	 */
	public final String _GetLogsDir()
	{
		return this.logsDir;
	}

	/**
	 * Returns the name used for the logs files.
	 *
	 * @return the name used for the logs files
	 */
	public final String _GetLogsName()
	{
		return this.logsName;
	}

	/**
	 * Returns the ResultLog object.
	 *
	 * @return the ResultLog object
	 */
	public final ResultLog _ResultLog()
	{
		return this.resultLog;
	}

	/**
	 * Returns the TransactionLog object.
	 *
	 * @return the TransactionLog object
	 */
	public final TransactionLog _TransactionLog()
	{
		return this.transLog;
	}
}
