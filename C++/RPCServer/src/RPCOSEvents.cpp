#include "iZBase/common/common.h"
#include "RPCServerTypes.h"
#include "RPCOSEvents.h"

#ifdef __IZPLATFORM_WIN__
//#define _WIN32_WINNT 0x0501  //to get MOUSEINPUT
#include <windows.h>
#include <winuser.h>
#endif

bool RPCOSEvents::MouseMove( int32 nX, int32 nY, uint32 uModFlags )	{
#ifdef __IZPLATFORM_WIN__
	uint32 uFlags= MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_MOVE | MOUSEEVENTF_VIRTUALDESK;
	normalizeScreenCoordinates( nX, nY );
	return sendMouseInput( nX, nY, uFlags, uModFlags);
#endif
}

#ifdef __IZPLATFORM_WIN__
//////////////////////////////////////////////////////////////////////////////
/*! Injects keyboard input at the Windows OS level. */
bool RPCOSEvents::sendKeyboardInput( uint16 uVkCode, uint32 uFlags, uint32 uModFlags ) {
	if (uModFlags != RPCModKeyFlags::NONE && uFlags == 0) //key down
		handleModFlags(uModFlags, uFlags);
		
	KEYBDINPUT keyboardInputEvent;
	keyboardInputEvent.wVk = uVkCode;
	keyboardInputEvent.wScan = (uFlags & KEYEVENTF_EXTENDEDKEY)?uVkCode:0;
	keyboardInputEvent.dwFlags(uFlags); // Not specifying KEYEVENTF_KEYUP means that it is a key press.
	keyboardInputEvent.time(0);			// Specifying 0 tells the OS to provide a time.
	keyboardInputEvent.dwExtraInfo = 0; // No extra info.
	INPUT inputStream;
	inputStream.type(INPUT_KEYBOARD);
	inputStream.ki = keyboardInputEvent;

	UINT numEventsInserted = SendInput( 1, &inputStream, sizeof( INPUT ) );

	if (uModFlags != RPCModKeyFlags::NONE && uFlags == KEYEVENTF_KEYUP) //mouse up
		handleModFlags(uModFlags, uFlags);

	if( numEventsInserted != 1 )
		return false;

	return true;
}

//////////////////////////////////////////////////////////////////////////////
/*! Simulates a moving the mouse to the coordinates of a widget. */
bool RPCOSEvents::sendMouseInput( int32 nX, int32 nY, uint32 uFlags, uint32 uModFlags ) {
	if( uModFlags != RPCModKeyFlags::NONE && (uFlags == MOUSEEVENTF_LEFTDOWN || uFlags == MOUSEEVENTF_MIDDLEDOWN || uFlags == MOUSEEVENTF_RIGHTDOWN) )
		handleModFlags( uModFlags, 0 );

	MOUSEINPUT mouseInputEvent;
	mouseInputEvent.dx= nX;
	mouseInputEvent.dy= nY;
	mouseInputEvent.mouseData(0);
	mouseInputEvent.dwFlags(uFlags);
	mouseInputEvent.time(0); // Lets windows provide the timestamp. Correct?
	mouseInputEvent.dwExtraInfo= 0;

	INPUT inputStream;
	inputStream.type(INPUT_MOUSE);
	inputStream.mi= mouseInputEvent;

	UINT numEventsInserted= SendInput( 1, &inputStream, sizeof( INPUT ) );

	if( uModFlags != RPCModKeyFlags::NONE && (uFlags == MOUSEEVENTF_LEFTUP || uFlags == MOUSEEVENTF_MIDDLEUP || uFlags == MOUSEEVENTF_RIGHTUP) )
		handleModFlags( uModFlags, KEYEVENTF_KEYUP );

	if( numEventsInserted <= 0 )
		return false;

	return true;
}

//////////////////////////////////////////////////////////////////////////////
/*!  Sends all the modifier key events	*/
void RPCOSEvents::handleModFlags( int32 nModFlags, uint32 uFlags ) {
	if( nModFlags == RPCModKeyFlags::NONE )
		return;
	if( nModFlags & RPCModKeyFlags::SHIFT )
		sendKeyboardInput( VK_SHIFT, uFlags, RPCModKeyFlags::NONE );
	if( nModFlags & RPCModKeyFlags::CONTROL )
		sendKeyboardInput( VK_CONTROL, uFlags | KEYEVENTF_EXTENDEDKEY, RPCModKeyFlags::NONE );
	if( nModFlags & RPCModKeyFlags::ALT)
		sendKeyboardInput( VK_MENU, uFlags | KEYEVENTF_EXTENDEDKEY, RPCModKeyFlags::NONE );
	if( nModFlags & RPCModKeyFlags::WINDOWSKEY)
		sendKeyboardInput( VK_RWIN, uFlags | KEYEVENTF_EXTENDEDKEY, RPCModKeyFlags::NONE );
}

//////////////////////////////////////////////////////////////////////////////
/*!  Helper function used to normalize screen coordinates to the coordinate system used for mouse input. */
void RPCOSEvents::normalizeScreenCoordinates( int32& nX, int32& nY ) {
	double dXPos= nX;
	double dYPos= nY;
	double dScreenWidth= GetSystemMetrics(SM_CXVIRTUALSCREEN);
	double dScreenHeight= GetSystemMetrics(SM_CYVIRTUALSCREEN);
	double dNormalFactor = 65536.0;

	// The normalized range is (most likely) larger than the screen geometry. 
	// Therefore, multiple values in the normalized range maps to a single pixel in screen space. 
	// It is desirable to avoid boundery case issues, rounding issues, etc. So, find out how many normalized 
	// numbers map to a single pixel, and put the mouse in the middle of that normalized range. 
	// That should give us enough leeway that we always move to the correct pixel location.
	double dXNormalToScreen = dNormalFactor / dScreenWidth;
	double dYNormalToScreen = dNormalFactor / dScreenHeight;
	nX = (int32)( dXPos * ( dNormalFactor/dScreenWidth ) + dXNormalToScreen/2.0 );
	nY = (int32)( dYPos * ( dNormalFactor/dScreenHeight ) + dYNormalToScreen/2.0 );
}
#endif