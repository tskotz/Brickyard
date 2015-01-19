///////////////////////////////////////////////////////////
//
// RPCTestApp.h: Automation Client Manager
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#include "iZBase/common/common.h"
#include "RPCManager.h"
#include "../../RPCServer/src/RPCServerInterfaces.h"

//////////////////////////////////////////////////////////////////////////////
/*! Test app for testing iZAutoRPC RPCServer changes */
int main() {
	bool bLoop= true;
	bool bSendEvent= false;

	RPCManager::Start( "RPCTestApp", 54321 );

	do {
		RPCManager::RequestCheck();

		if( bSendEvent)
			RPCManager::SendEvent( "ManagerEvent", "test message 1" );

	} while( bLoop );

	RPCManager::SendEvent( "ManagerEvent", "test message 2" );
	RPCManager::Stop();

	return 0;
}

