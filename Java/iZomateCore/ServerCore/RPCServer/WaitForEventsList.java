package iZomateCore.ServerCore.RPCServer;

import java.util.ArrayList;

/**
 * Manages events which are actively being waited on by a request.
 */
public class WaitForEventsList
{
	//ArrayList Structure
	static int sTYPE= 			0;
	static int sVALUE= 			1;
	static int sMSG= 			2;
	static int sIS_HANDLED= 	3;
	static int sCHECK_VALUE= 	4;
	static int sCHECK_MSG= 		5;
	
	private ArrayList<ArrayList<Object>> list;

	/**
	 * Constructor.
	 */
	public WaitForEventsList()
	{
		this.list = new ArrayList<ArrayList<Object>>();
	}

	/**
	 * Adds an event notification to the list.
	 *
	 * @param eventType
	 * @param eventValue
	 * @param checkEventValue
	 * @param eventMessage
	 * @param checkEventMessage
	 */
	public void _add(String eventType, int eventValue, boolean checkEventValue, String eventMessage, boolean checkEventMessage)
	{
		ArrayList<Object> event = new ArrayList<Object>();
		event.add(eventType);
		event.add(eventValue);
		event.add(eventMessage);
		event.add(false); //nIS_HANDLED, default false
		event.add(checkEventValue);
		event.add(checkEventMessage);
		this.list.add(event);
	}

	/**
	 * Clears out the list..
	 */
	public void _emptyWaitForEventsList()
	{
		this.list.clear();
	}

	/**
	 * Returns if the specified event has been handled.
	 * If it has been handled, the event is removed from the list.
	 *
	 * @param eventType
	 * @param eventValue
	 * @param eventMessage
	 * @return true if the event has been handled, false otherwise
	 */
	public boolean _isHandled(String eventType, int eventValue, String eventMessage)
	{
		if (eventType != null)
		{
			for (ArrayList<Object> event: this.list)
			{
				if ( eventType.equals(event.get(sTYPE)) )
	    	    {
	    	    	if ( !Boolean.parseBoolean(event.get(sCHECK_VALUE)+"") || (eventValue == Integer.parseInt(event.get(sVALUE)+"")) )
	    	    	{
	    	    		if ( !Boolean.parseBoolean(event.get(sCHECK_MSG)+"") || (eventMessage != null && eventMessage.equals(event.get(sMSG))) )
	        	    	{
	    	    			if (event.get(sIS_HANDLED).toString().equals("true"))
	    	    			{
		    	    			this.list.remove(event);
		    					return true;
	    	    			}
	        	    	}
	    	    	}
	    	    }
			}
		}

		return false;
	}

	/**
	 * Renames the specified event type/value/message in the WaitForEvents list.
	 *
	 * @param eventType
	 * @param eventValue
	 * @param eventMessage
	 * @param newType
	 * @param newValue
	 * @param newMessage
	 */
	public void _update(String eventType, int eventValue, String eventMessage,
						String newType, int newValue, String newMessage)
	{
		if (eventType != null && eventMessage != null)
		{
			for (ArrayList<Object> event: this.list)
			{
				if ( eventType.equals(event.get(sTYPE)) )
	    	    {
	    	    	if ( eventValue == Integer.parseInt(event.get(sVALUE)+"") )
	    	    	{
	    	    		if ( eventMessage.equals(event.get(sMSG)) )
	        	    	{
	    	    			event.set(sTYPE, newType);
	    	    			event.set(sVALUE, newValue);
	    	    			event.set(sMSG, newMessage);
	        	    	}
	    	    	}
	    	    }
			}
		}
	}

	/**
	 * Marks the specified event as handled.
	 *
	 * @param eventType
	 * @param eventValue
	 * @param eventMessage
	 * @return true if the event was found and handled otherwise false
	 */
	public boolean _setHandled(String eventType, int eventValue, String eventMessage)
	{
		if (eventType != null && eventMessage != null)
		{
			for (ArrayList<Object> event: this.list)
			{
				if ( eventType.equals(event.get(sTYPE)) )
	    	    {
	    	    	if ( !Boolean.parseBoolean(event.get(sCHECK_VALUE)+"") || (eventValue == Integer.parseInt(event.get(sVALUE)+"")) )
	    	    	{
	    	    		if ( !Boolean.parseBoolean(event.get(sCHECK_MSG)+"") || (eventMessage.equals(event.get(sMSG))) )
	        	    	{
    	    				event.set(sIS_HANDLED, true);
    						return true;
	        	    	}
	    	    	}
	    	    }
			}
		}
		return false;
	}
	
	/**
	 * Checks if the specified event is in the WaitFor queue and not handled.
	 *
	 * @param eventType
	 * @param eventValue
	 * @param eventMessage
	 * @return true if the event was found and handled otherwise false
	 */
	public boolean _isStillWaitingFor(String eventType, int eventValue, String eventMessage)
	{
		if (eventType != null && eventMessage != null)
		{
			for (ArrayList<Object> event: this.list)
			{
				if ( eventType.equals(event.get(sTYPE)) )
	    	    {
	    	    	if ( !Boolean.parseBoolean(event.get(sCHECK_VALUE)+"") || (eventValue == Integer.parseInt(event.get(sVALUE)+"")) )
	    	    	{
	    	    		if ( !Boolean.parseBoolean(event.get(sCHECK_MSG)+"") || (eventMessage.equals(event.get(sMSG))) )
	        	    	{
    						return Boolean.parseBoolean(event.get(sIS_HANDLED)+"");
	        	    	}
	    	    	}
	    	    }
			}
		}
		return false;
	}

}
