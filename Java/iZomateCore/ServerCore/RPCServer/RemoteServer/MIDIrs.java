package iZomateCore.ServerCore.RPCServer.RemoteServer;

import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;

import javax.sound.midi.ShortMessage;

public final class MIDIrs
{
	/**
     * The iZomate RPC server from which this object originated.
     */
    protected final RPCServer mServer;
    
    /**
     * Constructor.
     *
     * @param srvr the RemoteLauncher.
     * @throws Exception
     */
    public MIDIrs(RPCServer srvr) throws Exception
    {
        this.mServer = srvr;
    }

    //-----------------------------------
	//          Public Methods
	//-----------------------------------
    
    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @return The click point or null
     * @throws Exception
     */
    public void _sendMIDICommand(String midiDevice, ShortMessage... midiMessages) throws Exception
    {
    	int delayBetweenMsgs = 20;
       	OutgoingRequest req = this.mServer._createRequest("sendMIDICommand");
       	req._addString(midiDevice, "device");
       	for (ShortMessage msg : midiMessages)
       	{
       		req._addInt32(msg.getCommand(), "cmd");
       		req._addInt32(msg.getChannel(), "ch");
       		req._addInt32(msg.getData1(), "note");
       		req._addInt32(msg.getData2(), "vlcty");
       		req._addInt32(delayBetweenMsgs, "sleep");
       	}
        this.mServer._processRequest(req);
    }
    
    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @return The click point or null
     * @throws Exception
     */
    public String[] _getMIDIDevices() throws Exception
    {
       	IncomingReply reply = this.mServer._createAndProcessRequest("getMIDIDevices");
       	String[] devices = new String[reply._getCount()];
       	for (int i = 0; i < reply._getCount(); ++i)
       		devices[i] = reply._getString(i);
       	return devices;
    }


}
