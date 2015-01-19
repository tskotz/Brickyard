package iZomateCore.iZTests;

import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteServer;
import iZomateCore.UtilityCore.TimeUtils;
import org.hyperic.sigar.Sigar;

import java.awt.*;

//import org.eclipse.swt.internal.cocoa.OS;

public class RPCServerTest {	
	public RPCServerTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {	
	    String testbed = null;
		String logDir = null;

		for (int i = 0; i < args.length; ++i)
		{
			if (args[i].equals("-testbed"))
				testbed = args[++i];
			else if (args[i].equals("-logDir"))
				logDir = args[++i];
		}

		if (testbed == null)
			throw new Exception("-testbed arg not specified");

		if (logDir == null)
			throw new Exception("-logDir arg not specified");
				
//		localTest(logDir);
//		getMouseImage(testbed, logDir);
		
		//new String[]{testbed, "C:/Program Files (x86)/Avid/Pro Tools/ProTools.exe", logDir }
		PTDemo demo = new PTDemo( args );
		
		demo._testAppCheck();
		demo._quitAppIfRunning();
		demo._copyInSession("D:/Sessions/Gold/Blank.ptx");
		demo._startApp();
		demo._importAudioFile("Terry Skotz/My Music/iTunes/iTunes Media/Music/Joe Sample/Old Places Old Faces/03 Clifton's Gold.wav");
		demo._createNewTrack();
		demo._getProcessInfo("Initial");
		demo._togglePlay();
		demo._getProcessInfo("Play Started");
		demo._instantiatePlugin("Nectar", 1);
		demo._getProcessInfo("Nectar Instantiation");
		demo._tweakNectar();
		demo._getProcessInfo("Nectar tweaks");
		demo._instantiatePlugin("Ozone5", 2);
		demo._getProcessInfo("Ozone Instantiation");
		demo._tweakOzone();
		demo._getProcessInfo("Ozone tweaks");
		demo._uninstantiatePlugin(2);
		demo._getProcessInfo("Ozone Uninstantiation");
		demo._uninstantiatePlugin(1);
		demo._getProcessInfo("Nectar Uninstantiation");
		demo._togglePlay();
		demo._getProcessInfo("Play stopped");
		TimeUtils.sleep(5);
		demo._getProcessInfo("After 5 sec sleep");
		demo._reportProcessInfo();
	}
	
	public static void getMouseImage(String testbed, String logDir) throws Exception {
		RemoteServer rs = new RemoteServer(testbed, new Logs(logDir, "Test", 0, true));
//		rs._Robot()._imageClickII("/IzoImages/Ozone3BndMstrInstrSlowDy.bmp", 1);
		rs._Robot()._imageGetMouseImage("/IzoImages", "MouseImage.bmp");
		rs._Robot()._imageClickII("/IzoImages/Ozone3BndMstrInstrSlowDy.bmp", 1);
		rs._Robot()._imageGetMouseImage("/IzoImages", "MouseImage.bmp");
		rs._Robot()._imageClickII("/IzoImages/DoneBtn.bmp", 1);
		rs._Robot()._imageGetMouseImage("/IzoImages", "MouseImage.bmp");
		rs._Robot()._imageClickII("/IzoImages/DoneBtn.bmp", 1);

		for (int i = 0; i < 20; ++i)
			rs._Robot()._imageGetMouseImage("/IzoImages", "MouseImage.bmp");
	}
	
	public static void localTest(Logs logs) throws Exception {
		Sigar sigar = new Sigar();
//		for (Cpu cpu : sigar.getCpuList())
//			System.out.println(cpu.toString());
		
		long ptpid = 0;
		for (long p : sigar.getProcList()) {
			try {	
				//System.out.println(p + " " + sigar.getProcExe(p).getName());
				if (sigar.getProcExe(p).getName().contains("Pro Tools.app/Contents/MacOS/Pro Tools")) {
					//System.out.println(p + " " + sigar.getProcExe(p).getName());
					ptpid = p;
					break;
				}
			}
			catch (Exception e) {
				//System.out.println(e.getMessage());
			}
		}
		
		boolean b = true;
		do {	
			System.out.println("\n-------------------------------------------------------------");
			System.out.println("getCpuPerc:	" + sigar.getCpuPerc().toString());
			System.out.println("getMem:		" + sigar.getMem().toString());
			System.out.println("getThreadCpu:	" + sigar.getThreadCpu().toString());
			System.out.println("getProcExe:	" + sigar.getProcExe(ptpid).toString());
			System.out.println("getProcCpu:	" + sigar.getProcCpu(ptpid).toString());
			System.out.println("getProcEnv:	" + sigar.getProcEnv(ptpid).toString());
			System.out.println("getProcMem:	" + sigar.getProcMem(ptpid).toString());
			System.out.println("getProcState:	" + sigar.getProcState(ptpid).toString());
			System.out.println("getThreads:	" + sigar.getProcState(ptpid).getThreads());
			System.out.println("getProcTime:	" + sigar.getProcTime(ptpid).toString());
		}
		while (b);
		
		//int pid = OS.getpid();
		for (int i = 0; i < 0; ++i) {
			Thread.sleep(2000);
			//Window w = KeyboardFocusManager.;
			Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
			if (w == null)
				continue;
			
			System.out.println("Name: " + w.getName());
			System.out.println("Bounds: " + w.getBounds());
			System.out.println("x,y: " + w.getX() + ", " + w.getY());
		}				
	}
	
}


