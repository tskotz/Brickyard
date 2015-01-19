package iZomateCore.ServerCore.Notifications;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.util.ArrayList;

/**
 * Registry for registering user defined callback for specific datum typs and event types.
 */
public class NotificationCallbackRegistry
{
    private ArrayList<NotificationInfo> m_NotificationCallbackList;

    /**
     * Constructs an empty NotificationCallbackRegistry
     */
    public NotificationCallbackRegistry()
    {
    	this.m_NotificationCallbackList = new ArrayList<NotificationInfo>(0);
    }
    
    /**
     * Registers a callback in the registry with a specifc datum type and event type.
     *
     * @param callback the callback to register.  Pulls info from callback itself
     * @return true if registered successfully
     * @throws Exception
     */
    public boolean _registerCallback(NotificationCallback callback) throws Exception
    {
     	return this._registerCallback( callback._GetNotificationType(), callback._GetTitle(), callback._GetMessage(), callback );
    }

    /**
     * Registers a callback in the registry with a specifc datum type and event type.
     *
     * @param type the datum type to associate the callback with.
     * @param strTitleOrSubType The EVNT sub type or WND/DLOG title.
     * @param strMessage The notification message.
     * @param callback the callback to register
     * @return true if registered successfully
     * @throws Exception
     */
    public boolean _registerCallback(NotificationType type, String strTitleOrSubType, String strMessage, NotificationCallback callback) throws Exception
    {
    	if( this._getNotification( type, strTitleOrSubType, strMessage, true ) != null )
            return false; //Already been registered!
    	return this.m_NotificationCallbackList.add(new NotificationInfo(type, strTitleOrSubType, strMessage, callback));
    }

    /**
     * Unregisters a callback in the registry with a specific notification type, notification title or subtype and notification message.
     *
     * @param callback the callback to unregister.  Pulls info from callback itself
     * @return true if unregistered successfully
     * @throws Exception
     */
    public boolean _unregisterCallback(NotificationCallback callback) throws Exception
    {
     	return this._unregisterCallback( callback._GetNotificationType(), callback._GetTitle(), callback._GetMessage() );
    }

    /**
     * Unregisters a callback in the registry with a specific notification type, notification title or subtype and notification message.
     *
     * @param type the datum type associated with the callback.
     * @param strTitleOrSubType The EVNT sub type or WND/DLOG title.
     * @param strMessage The notification message.
     * @return true if unregistered successfully
     * @throws Exception
     */
    public boolean _unregisterCallback(NotificationType type, String strTitleOrSubType, String strMessage) throws Exception
    {
       	NotificationInfo info= this._getNotification( type, strTitleOrSubType, strMessage, true );
    	if( info != null )
            return this.m_NotificationCallbackList.remove( info );
    	return false;
    }

    /**
     * Looks up and returns the registered callback that is associated with the notification type, notification title or subtype and notification message.
     *
     * @param type the Datum type
     * @param strTitleOrSubType The EVNT sub type or WND/DLOG title.
     * @param strMessage The notification message.
     * @return the NotificationCallback
     */
    public  NotificationCallback _getCallback (NotificationType type, String strTitleOrSubType, String strMessage )
    {
    	NotificationInfo info= this._getNotification( type, strTitleOrSubType, strMessage, false );
    	if( info != null )
    		return info.m_callback;
    	return null;
    }

    /**
     * Looks up and returns the registered Notification that is associated with the notification type, notification title or subtype and notification message.
     *
     * @param type the Datum type
     * @param strTitleOrSubType The EVNT sub type or WND/DLOG title.
     * @param strMessage The notification message.
     * @return the NotificationInfo
     */
    private  NotificationInfo _getNotification (NotificationType type, String strTitleOrSubType, String strMessage, boolean bExactMatch )
    {
    	NotificationInfo infoMatch= null;
    	int nPreviousMatchScore= 0; //No Match
    	for (NotificationInfo info: this.m_NotificationCallbackList) {
            if( info.m_type.equals(type) ) {
            	// We need to find the best match
            	int nMatchScore= 0; //No Match
            	if( info.m_strTitleOrSubType == strTitleOrSubType /*both null*/ || (info.m_strTitleOrSubType != null && info.m_strTitleOrSubType.equals( strTitleOrSubType ) ) )
            		nMatchScore= 4; //Exact match
            	else if( !bExactMatch && info.m_strTitleOrSubType == null )
            		nMatchScore= 1; //Allow any
            	else
            		continue; // We need both to find matches in order to succeed so just continue
           	
            	if( info.m_strMessage == strMessage || (info.m_strMessage != null && info.m_strMessage.equals( strMessage ) ) )
            		nMatchScore+= 8; //Exact match
            	else if( !bExactMatch && info.m_strMessage == null )
            		nMatchScore+= 2; //Allow any
            	else
            		continue;
            	
            	// We must find a match from both the m_strTitleOrSubType and m_strMessage.  We want to return the best match as well
            	// A score of 3 means we found both defaults
            	// A score of 6 means m_strTitleOrSubType exact match, default m_strMessage
            	// A score of 9 means default m_strTitleOrSubType and exact m_strMessage
            	// A score of 12 means both exact matches so return immediately
            	if( nMatchScore == 12 )
            		return info; // We found an exact match!  Return it!
            	if( (nMatchScore == 3 || nMatchScore == 6 || nMatchScore == 9) && nMatchScore > nPreviousMatchScore ) {
            		infoMatch= info;
            		nPreviousMatchScore= nMatchScore;
            	}
            }
        }
    	return infoMatch; //Return the NotificationInfo based on match score
    }

    /**
     *	Private Notification Info storage class for Notification Registry
     */
    private class NotificationInfo
    {
    	public NotificationType m_type;
    	public String m_strTitleOrSubType;
    	public String m_strMessage;
    	public NotificationCallback m_callback;

    	public NotificationInfo(NotificationType type, String strTitleOrSubType, String strMessage, NotificationCallback callback) throws Exception
    	{
    		this.m_type = type;
    		this.m_strTitleOrSubType= strTitleOrSubType;
    		this.m_strMessage= strMessage;
    		this.m_callback = callback;
    	}
    }
}
