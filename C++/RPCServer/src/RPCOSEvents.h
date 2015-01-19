///////////////////////////////////////////////////////////
//
// RPCOSEvents.h: RPC System Event creation class
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once 

class RPCOSEvents {
public:
	RPCOSEvents() {}

	bool MousePressLeftButton	( int32 nX, int32 nY, uint32 clickNum, uint32 uModFlags );
	bool MouseReleaseLeftButton	( int32 nX, int32 nY, uint32 clickNum, uint32 uModFlags );
	bool MousePressRightButton	( int32 nX, int32 nY, uint32 uModFlags );
	bool MouseReleaseRightButton( int32 nX, int32 nY, uint32 uModFlags );
	bool MouseClickLeftButton	( int32 nX, int32 nY, uint32 nClicks, uint32 uModFlags );
	bool MouseClickRightButton	( int32 nX, int32 nY, uint32 uModFlags );
	bool MouseMove				( int32 nX, int32 nY, uint32 uModFlags );

private:
	#ifdef __IZPLATFORM_WIN__
	void handleModFlags(int32 nModFlags, uint32 uFlags);
	void normalizeScreenCoordinates( int32& nX, int32& nY );
	bool sendKeyboardInput( uint16 uVkCode, uint32 uFlags, uint32 uModFlags );
	bool sendMouseInput( int32 nX, int32 nY, uint32 uFlags, uint32 uModFlags );
	#endif

}
