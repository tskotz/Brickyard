///////////////////////////////////////////////////////////
//
// RPCMethods.cpp: RPC Server internal methods
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////#include <iostream>
#include <iostream>
#include "RPCServerInterfaces.h"

#ifndef __APPLE__ //Windows
#include <Windows.h>
#endif

#include "RPCMethods.h"

using namespace TestAutomation;

void RPC_RoundTripTest( RPCRequestInterface* req, RPCServerInterface* server, RPCReplyInterface* reply ) {
	const char * name = NULL;
	uint32 id = 0;
	bool b;
	float f;
	double d;
	int16 i16;
	uint16 ui16;
	int32 i32;
	uint32 ui32;
	int64 i64;
	uint64 ui64;
	const char * str;
	const char * buff = NULL;

	int32 x = req->GetCount();

	for( int i = 0; i < x; ++i) {
		name = req->GetName(i);
		id = req->GetTypeID(i);
		if( id != req->GetTypeID(name))
			reply->AddString("GetTypeID returned wrong value", "TypeIdError");

		if( id == RPCBoolDatumID) {
			b = req->GetBool(name);
			if( b != req->GetBool(i))
				reply->AddString("Bool mismatch error", "boolError");
			reply->AddBool(b, name);
		}
		else if( id == RPCBufferDatumID) {
			buff = req->GetBuffer(name);
			uint32 size = req->GetBufferSize(name);
			if( strcmp(buff, req->GetBuffer(i)) != 0 )
				reply->AddString("Buffer mismatch error", "buffError");
			reply->AddBuffer(buff, size, name);
		}
		else if( id == RPCFloatDatumID) {
			f = req->GetFloat(name);
			if( f != req->GetFloat(i))
				reply->AddString("float mismatch error", "flotError");
			reply->AddFloat(f, name);
		}
		else if( id == RPCDoubleDatumID) {
			d = req->GetDouble(name);
			if( d != req->GetDouble(i))
				reply->AddString("double mismatch error", "dbleError");
			reply->AddDouble(d, name);
		}
		else if( id == RPCInt16DatumID) {
			i16 = req->GetInt16(name);
			if( i16 != req->GetInt16(i))
				reply->AddString("int16 mismatch error", "in16Error");
			reply->AddInt16(i16, name);
		}
		else if( id == RPCUInt16DatumID) {
			ui16 = req->GetUInt16(name);
			if( ui16 != req->GetUInt16(i))
				reply->AddString("uint16 mismatch error", "ui16Error");
			reply->AddUInt16(ui16, name);
		}
		else if( id == RPCInt32DatumID) {
			i32 = req->GetInt32(name);
			if( i32 != req->GetInt32(i))
				reply->AddString("int32 mismatch error", "in32Error");
			reply->AddInt32(i32, name);
		}
		else if( id == RPCUInt32DatumID) {
			ui32 = req->GetUInt32(name);
			if( ui32 != req->GetUInt32(i))
				reply->AddString("uint32 mismatch error", "ui32Error");
			reply->AddUInt32(ui32, name);
		}
		else if( id == RPCInt64DatumID) {
			i64 = req->GetInt64(name);
			if( i64 != req->GetInt64(i))
				reply->AddString("int64 mismatch error", "in64Error");
			reply->AddInt64(i64, name);
		}
		else if( id == RPCUInt64DatumID) {
			ui64 = req->GetUInt64(name);
			if( ui64 != req->GetUInt64(i))
				reply->AddString("uint64 mismatch error", "ui64Error");
			reply->AddUInt64(ui64, name);
		}
		else if( id == RPCStringDatumID) {
			str = req->GetString(name);
			if( strcmp(str, req->GetString(i)) != 0)
				reply->AddString("String mismatch error", "striError");
			reply->AddString(str, name);
		}
	}
}

void RPC_NotificationTest( RPCRequestInterface* req, RPCServerInterface* server, RPCReplyInterface* reply ) {
	server->EventNotification ("TestEventNotification", "Starting Test", 0);
	server->EventNotification ("TestEventNotificationII", "Hello World", 3);
	server->StatusNotification("Test Status", 0);
	server->StatusNotification("Test Status", 33);
	server->StatusNotification("Test Status", 66);
	server->StatusNotification("Complete", 100);
	server->EventNotification ("TestEventNotificationIII", "Hello World again", 3);
	server->EventNotification ("TestEventNotification", "Ending Test", 0);
}

void RPC_ExceptionTest( RPCRequestInterface* req, RPCServerInterface* server, RPCReplyInterface* reply ) {
	reply->SetException("This is a test exception");
}

void RPC_ForceSocketShutdown( RPCRequestInterface* req, RPCServerInterface* server, RPCReplyInterface* reply ) {
	server->Stop();
}

void RPCMethods::RegisterMethods( RPCServerInterface* server ) {
	server->AddMethod("RPC_RoundTripTest",		RPC_RoundTripTest);
	server->AddMethod("RPC_NotificationTest",	RPC_NotificationTest);
	server->AddMethod("RPC_ExceptionTest",		RPC_ExceptionTest);
	server->AddMethod("RPC_ForceSocketShutdown",RPC_ForceSocketShutdown);
}

#ifdef __APPLE__
bool RPC_BringAppToFront( void* pInstance ) {
    return false;
}
#else
bool RPCMethods::RPC_BringAppToFront( HWND pHWND ) {
	//Doesn't force window to front process but rather just this window to the front of our windows
	BOOL b= ::SetForegroundWindow( pHWND );
	b= ::SetWindowPos(pHWND, HWND_TOP, 0, 20, 0, 0, SWP_NOZORDER | SWP_NOSIZE | SWP_SHOWWINDOW );
	return b?true:false;
}
#endif


