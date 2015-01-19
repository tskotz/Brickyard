package iZomateCore.LogCore;

/**
 * The TransactionLog is an HTML test log that records all RPC transactions that are sent/received by the test.
 */
public final class TransactionLog extends Log
{
	private boolean m_bEchoSystemOut= false;
	
	/**
	 * Constructor
	 * @param LogName the name of the log
	 * @param LogDir the log's parent directory
	 * @throws Exception 
	 */
	public TransactionLog(String LogName, String LogDir) throws Exception
	{
		super(LogName, LogDir, ".html", false, false);
		this.log("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n<pre>");
	}
	
	/**
	 * Causes the next _log*() call to be echoed to System.out
	 * @return this
	 */
	public TransactionLog _echo() {
		this.m_bEchoSystemOut= true;
		return this;
	}
	
	/**
	 * Logs a line to the transaction log.
	 * 
	 * @param str the string to log
	 * @throws Exception
	 */
	public void _log(String str) throws Exception
	{
		if( this.m_bEchoSystemOut )
			System.out.println( str );
		
		this._logLine(str);
		
		this.m_bEchoSystemOut= false;
	}
	
	/**
	 * Logs a bold line to the transaction log
	 * 
	 * @param str the string to log
	 * @throws Exception
	 */
	public void _logBold(String str) throws Exception
	{
		this._log("<b>" + str + "</b>");
	}
	
	/**
	 * Logs a line to the transaction log with text in the specified color.
	 * 
	 * @param str the string to log
	 * @param color HTML color code
	 * @throws Exception
	 */
	public void _logColor(String str, String color) throws Exception
	{
		this._log("<FONT COLOR=\"" + color + "\"><B>" + str + "</B></FONT>");
	}
	
	/**
	 * Logs a transaction to the transaction log.
	 * 
	 * @param datumInfo the datum info
	 * @param name the datum name
	 * @param value the datum value
	 * @throws Exception
	 */
	public void _logTransaction(String datumInfo, String name, String value)  throws Exception
	{
		this._log("\t" + datumInfo + "\tName: " + name + "\tValue: " + value);
	}
}
