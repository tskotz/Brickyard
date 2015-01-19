/**
 * 
 */
package iZomateRemoteServer.Methods;

import iZomateCore.ServerCore.RPCServer.IncomingRequest;
import iZomateCore.ServerCore.RPCServer.OutgoingReply;
import iZomateRemoteServer.ServerThread;
import org.hyperic.sigar.*;

/**
 * @author tskotz
 *
 */
public class SystemInfoMethods
{
	private Sigar mSigar;

	public SystemInfoMethods()
	{
		this.mSigar = new Sigar();
	}
	
    //-----------------------------------
	//         Public Methods
	//-----------------------------------

    /**
     * Wrapper for capturing all of the remote system's screens.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void getProcPID(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	//this.mSigar.load();
    	String procName = req._getString("procName");
    	
		for (long p : this.mSigar.getProcList())
		{
			try
			{
				if (this.mSigar.getProcExe(p).getName().contains(procName))
				{
					reply._addInt64(p, "pid");
					reply._addString(this.mSigar.getProcExe(p).getName(), "Name");
					break;
				}
			}
			catch (Exception e)
			{
				//Keep calm and carry on 
			}
		}
		
		if (!reply._exists("pid"))
			reply._addInt64(0, "pid");
    }
 
    /**
     * Wrapper for capturing all of the remote system's screens.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void getProcInfo(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	long pid = req._getInt64("pid");
		reply._addString(this.mSigar.getProcExe(pid).getName(), "Name");

		ProcMem pm = this.mSigar.getProcMem(pid);
    	reply._addInt64(pm.getSize(), 		"MemSize");
    	reply._addInt64(pm.getResident(), 	"MemRes");
    	reply._addInt64(pm.getShare(), 		"MemShare");
    	reply._addInt64(pm.getPageFaults(), "MemPageFaults");
    	
    	ProcCpu cpu = this.mSigar.getProcCpu(pid);
    	reply._addDouble(cpu.getPercent(), 	"CPUPerc");
    	reply._addInt64(cpu.getTotal(), 	"CPUTime");

    	reply._addInt64( this.mSigar.getProcState(pid).getThreads(), "Threads");
    }

    /**
     * Wrapper for capturing all of the remote system's screens.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void getSystemInfo(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {  
    	CpuPerc cpuPerc = this.mSigar.getCpuPerc();
    	reply._addDouble(cpuPerc.getUser(), 	"UserTime");
    	reply._addDouble(cpuPerc.getSys(), 		"SysTime");
    	reply._addDouble(cpuPerc.getNice(), 	"NiceTime");
    	reply._addDouble(cpuPerc.getWait(), 	"WaitTime");
    	reply._addDouble(cpuPerc.getIdle(), 	"IdleTime");
    	reply._addDouble(cpuPerc.getStolen(), 	"StolenTime");  
    	
    	Mem mem = this.mSigar.getMem();
    	reply._addInt64(mem.getActualFree(), "MemFree");
    	reply._addInt64(mem.getActualUsed(), "MemUsed");
    	reply._addInt64(mem.getTotal(), 	"MemTotal");
    	reply._addInt64(mem.getRam(), 		"MemRAM");
    }

}
