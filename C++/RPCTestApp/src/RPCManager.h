///////////////////////////////////////////////////////////
//
// RPCManager.h: Automation Client Manager
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once

namespace TestAutomation { class RPCServerInterface; class RPCRequestInterface; class RPCReplyInterface; }
namespace Util { class Module; }

class RPCManager {
public:
	RPCManager();
	~RPCManager();

	static void Start(const char *serverID, int32 port);
	static void Stop();
	static void RequestCheck();
	static void SendEvent(const char * type, const char * message, int32 ival = 0);
	static void SendWindow();
	static void SendDialog();
	static void SendStat();

private:
	static void RegisterHooks();
	static void TestHook(TestAutomation::RPCRequestInterface* req, TestAutomation::RPCServerInterface* server, TestAutomation::RPCReplyInterface* reply);

};