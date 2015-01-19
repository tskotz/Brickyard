package iZomateCore.ServerCore.RPCServer.RemoteServer;

public final class SystemInfo
{
	public double mUserTime;
	public double mSysTime;
	public double mNiceTime;
	public double mWaitTime;
	public double mIdleTime;
	public double mStolenTime;
	public long mMemFree;
	public long mMemUsed;
	public long mMemTotal;
	public long mMemRAM;
	
	public SystemInfo()
	{
		this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}
	
	public SystemInfo(double userTime, double sysTime, double niceTime,
			double waitTime, double idleTime, double stolenTime,
			long memFree, long memUsed, long memTotal, long memRAM)
	{
		this.mUserTime = userTime;
		this.mSysTime = sysTime;
		this.mNiceTime = niceTime;
		this.mWaitTime = waitTime;
		this.mIdleTime = idleTime;
		this.mStolenTime = stolenTime;
		this.mMemFree = memFree;
		this.mMemUsed = memUsed;
		this.mMemTotal = memTotal;
		this.mMemRAM = memRAM;
	}
	
	public String toString()
	{
		return "Free: " + this.mMemFree;
	}
}