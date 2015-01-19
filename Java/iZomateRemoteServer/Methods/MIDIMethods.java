package iZomateRemoteServer.Methods;

import iZomateCore.ServerCore.RPCServer.IncomingRequest;
import iZomateCore.ServerCore.RPCServer.OutgoingReply;
import iZomateRemoteServer.ServerThread;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class MIDIMethods
{
	public MIDIMethods() { }
	
    //-----------------------------------
	//         Public Methods
	//-----------------------------------

    /**
     * Wrapper for sending MIDI commands.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void sendMIDICommand(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
	{
    	Receiver receiver = this._getDeviceReceiver(req._getString("device"));
    	if( receiver == null )
    		throw new Exception( "The MIDI device was not found: " + req._getString("device") );
    	
    	//Requires that chunks are in the following order:
    	//  Command, Channel, Note, Velocity, sleep
		for (int i = 2; i < req._getCount(); ++i) {
			//----------------------  Command			Channel				Note				Velocity -----------
			this._sendCommand(receiver, req._getInt32(i), req._getInt32(++i), req._getInt32(++i), req._getInt32(++i));
			Thread.sleep(req._getInt32(++i));
		}
	}
  
    /**
     * Wrapper for reporting all the MIDI devices available.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void getMIDIDevices(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
	{
    	for (Info i : MidiSystem.getMidiDeviceInfo())    		
    		reply._addString(i.getName() /* + " Desr: " + i.getDescription() + " Vendor: " + i.getVendor() + " Version: " + i.getVersion(), "device"*/);
	}
    
    /**
     * Sends the command to the specified receiver
     * 
     * @param receiver
     * @param cmd
     * @param channel
     * @param note
     * @param velocity
     * @throws Exception
     */
    private void _sendCommand(Receiver receiver, int cmd, int channel, int note, int velocity) throws Exception
    {
		ShortMessage myMsg = new ShortMessage();		  
		myMsg.setMessage(cmd, channel, note, velocity);
		receiver.send(myMsg, -1L /*timeStamp*/);
    }
    
    /**
     * Finds and returns Receiver for the MIDI device with the specified name
     * @param deviceName
     * @return MIDI device's Receiver
     * @throws Exception
     */
    private Receiver _getDeviceReceiver(String deviceName) throws Exception
    {
    	for (Info i : MidiSystem.getMidiDeviceInfo())
    		if (i.getName().equals(deviceName))
    		{
    			MidiDevice dev = MidiSystem.getMidiDevice(i);
    			if (!dev.isOpen())
    				dev.open();
    			return dev.getReceiver();
    		}

     	return null;
    }
}
