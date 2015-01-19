package iZomateCore.ServerCore.RPCServer.RemoteServer;

public final class ProcessInfo
{
	public String 	mProcName;
	public long 	mPID;
	public long		mMemSize;
	public long		mMemRes;
	public long		mMemShare;
	public long		mMemPageFaults;
	public long 	mThreads;
	public long		mCPUTime;
	public double	mCPUPercentage;

	public ProcessInfo()
	{
		this("", 0, 0, 0, 0, 0, 0, 0, 0);
	}

	public ProcessInfo(String procName, long pid, 
					long memSize, long memRes, long memShare, long memPageFaults, long threads,
					long cpuTime, double cpuPercentage)
	{
		this.mProcName = procName;
		this.mPID = pid;
		this.mMemSize = memSize;
		this.mMemRes = memRes;
		this.mMemShare = memShare;
		this.mMemPageFaults = memPageFaults;
		this.mThreads = threads;
		this.mCPUTime = cpuTime;
		this.mCPUPercentage = cpuPercentage;
	}
	
	public String toString()
	{
		return (this.mProcName+" "+this.mPID+" "+this.mMemSize+" "+this.mMemRes+" "+this.mMemShare+" "+this.mMemPageFaults+" "+this.mThreads+" "+this.mCPUTime+" "+this.mCPUPercentage);
	}
}