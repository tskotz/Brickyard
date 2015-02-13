///////////////////////////////////////////////////////////
//
// RPCMethods.h: RPC Server internal methods
// Copyright (c) 2015 - Brickyard, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
namespace TestAutomation { class RPCRequestInterface; class RPCServerInterface; class RPCReplyInterface; }

namespace RPCMethods {
	void RegisterMethods(TestAutomation::RPCServerInterface* server);
#ifdef __APPLE__
	bool RPC_BringAppToFront( void* pInstance );
#else
	bool RPC_BringAppToFront( HWND pHWND );
#endif
}