package iZomateCore.UtilityCore;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains Time/Date formatting utilities, as well as a set of Default Timeout values.
 */
public class TimeUtils
{
    /** This timeout is 15 seconds in length. */
    private static int    	DEFAULT_TIMEOUT_MS= 15000;
    /**  Keeps track of the total amount of time the test has slept for */
    private static double 	sleepTally= 	0;
    private static boolean 	loggingOn= 		true;
    private static boolean	bTimeOutDefaultOverride= false;
    
    /**
     * Overrides the default value of 15 seconds with a custom time.
     * @param nTimeout
     */
    public static void _SetDefaultTimeout( int nTimeout ) {
    	_SetDefaultTimeoutMS( nTimeout * 1000 );
    }

    /**
     * Overrides the default value of 15 seconds with a custom time.
     * @param nTimeout
     */
    public static void _SetDefaultTimeoutMS( int nTimeout ) {
    	DEFAULT_TIMEOUT_MS= nTimeout;
    	bTimeOutDefaultOverride= true;
    }

    /**
     * Returns the current default timeout value
     * @param nTimeout
     */
    public static int _GetDefaultTimeout() {
    	return _GetDefaultTimeoutMS() / 1000;
    }
    
    /**
     * Returns the current default timeout value
     * @param nTimeout
     */
    public static int _GetDefaultTimeoutMS() {
    	return DEFAULT_TIMEOUT_MS;
    }

    public static int _GetTimeout( int nTimeout ) {
    	if( bTimeOutDefaultOverride )
    		return _GetDefaultTimeout();
    	return nTimeout;
    }

    public static int _GetTimeoutMS( int nTimeout ) {
    	if( bTimeOutDefaultOverride )
    		return _GetDefaultTimeoutMS();
    	return nTimeout;
    }

    /**
     * Converts the specified millisecond value to a time string in the format d:hh:mm:ss.mmm
     *
     * @param ms the number of ms to convert
     * @return time string in the format d:hh:mm:ss.mmm
     */
    public static String convertMStoString(long ms)
    {
        int days = (int)ms / 86400000;
        ms = ms - days * 86400000;
        int hours = (int)ms / 3600000;
        ms = ms - hours * 3600000;
        int mins = (int)ms / 60000;
        ms = ms - mins * 60000;
        int secs = (int)ms / 1000;
        ms = ms - secs * 1000;
        StringBuffer myTime = new StringBuffer();
        if(days > 0)
            myTime.append(days + ":");
        else
            myTime.append("00:");
        if(hours < 10)
            myTime.append("0" + hours + ":");
        else
            myTime.append(hours + ":");
        if(mins < 10)
            myTime.append("0" + mins + ":");
        else
            myTime.append(mins + ":");
        if(secs < 10)
            myTime.append("0" + secs + ".");
        else
            myTime.append(secs + ".");
        if(ms < 10)
            myTime.append("00" + ms);
        else if(ms < 100)
            myTime.append("0" + ms);
        else
            myTime.append(ms);
        return myTime.toString();
    }

    /**
     * Converts an input second value to a time string in the format d:hh:mm:ss
     *
     * @param s the number of seconds to convert
     * @return time string in the format d:hh:mm:ss
     */
    public static String convertStoString(long s)
    {
        int days = (int)s / 86400;
        s = s - days * 86400;
        int hours = (int)s / 3600;
        s = s - hours * 3600;
        int mins = (int)s / 60;
        s = s - mins * 60;
        StringBuffer myTime = new StringBuffer();
        if(days > 0)
            myTime.append(days + ":");
        else
            myTime.append("00:");
        if(hours < 10)
            myTime.append("0" + hours + ":");
        else
            myTime.append(hours + ":");
        if(mins < 10)
            myTime.append("0" + mins + ":");
        else
            myTime.append(mins + ":");
        if(s < 10)
            myTime.append("0" + s);
        else
            myTime.append(s);
        return myTime.toString();
    }

    /**
     * Creates a timestamp string from the current time.
     *
     * @return the date in the format ""
     */
    public static String getTimestamp() {
    	return new SimpleDateFormat("MMM-dd-yyyy HH:MM:ss").format( new Date() );
    }

    /**
     * Creates a date string.
     *
     * @param d the date
     * @return the date in the format "YYYY-MM-DD"
     */
    public static String getDate()
    {
    	return new SimpleDateFormat("yyyy-MM-dd").format( new Date() );
    }

    /**
     * Creates a time string.
     *
     * @param d the date and time
     * @return the date in the format "hh:mm:ss".
     */
    public static String getTime() {
    	return new SimpleDateFormat("hh:mm:ss").format( new Date() );
    }

    /**
     * Creates a time string from a date.
     *
     * @param d the date and time
     * @return the date in the format "YYYY-M-D hh:mm:ss" (space between date and time).
     */
    public static String getDateTime() {
    	return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format( new Date() );
    }

    /**
     * Creates a time string from a date.
     *
     * @param d the date and time
     * @return the date in the format "YYYY-M-D_hh:mm:ss" (underscore between date and time)
     */
    public static String getDateTimeNoSpaces()
    {
        String timestamp = getDateTime();
        timestamp = timestamp.replace(" ", "_");
        return timestamp;
    }

    /**
     * Performs a sleep for the specified amount of time.
     *
     * @param seconds the number of seconds to sleep (as a double so .5 = 1/2 second)
     * @throws Exception
     */
    public static void sleep(double seconds) throws Exception
    {
    	if (seconds > 0)
    	{
    		sleepTally += seconds;
    		if (loggingOn)
    			System.out.println("Sleeping for: " + seconds + "  Total Time Slept: " + String.format("%.02f", sleepTally)) ;
    		Thread.sleep((int)(seconds * 1000));
    	}
    }
    
    public static String curTime() {
    	return new Date().toString();
    }
    
    /**
     * Turns on/off the sleep tally logging
     *  
     * @param state
     */
    public static void _setLogging(boolean state)
    {
    	loggingOn = state;
    }

    /********************************************************************
	 * Util to convert HMS string to milliseconds 
	 */
	public static float _HmsToMilliSeconds(String strHms ) {
		String[] parts= strHms.split(":");	
		int hr= Integer.valueOf(parts[0]);
		int min= Integer.valueOf(parts[1]);
		
		String[] lastParts= parts[2].split("\\.");
		int sec= Integer.valueOf(lastParts[0]);
		int ms= Integer.valueOf(lastParts[1]);
		
		return ms + sec*1000 + min*60*1000 + hr*60*60*1000;		
	}
	/********************************************************************
	 * Util to convert HMS string to seconds 
	 */
	public static  float _HmsToSeconds(String strHms ) {
		return _HmsToMilliSeconds(strHms) / 1000.0f;				
	}
}
