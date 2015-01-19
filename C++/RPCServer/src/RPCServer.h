///////////////////////////////////////////////////////////
//
// RPCServer.h: The RPC Server class
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once

#include "RPCServerInterfaces.h"
#include "RPCRequest.h"
#include "RPCReply.h"
#include <map>

///////////////////////////////////////////////////////////
//
// RPCServer.cpp: RPC Server class for processing requests and sending replies
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
class RPCTransaction;

typedef std::map<std::string, TestAutomation::RPCfunctionPtr> RPCmapStringToFunc;

#ifdef __APPLE__
typedef int		rpcSocket_t;
#else
typedef SOCKET	rpcSocket_t;
#endif

class RPCServer : public TestAutomation::RPCServerInterface
{
public:
		RPCServer ();
		virtual ~RPCServer ();

		int32			Start( const char* pServerID, uint32 uTCPPort );
		int32			Stop( );
		int32			Version( );
		int32			ProcessSocket( );
		int32			RegisterMethods( );
		int32			AddMethod( const char* pName,  TestAutomation::RPCfunctionPtr pFunc );
		TestAutomation::RPCfunctionPtr	GetMethod( const char* pName );
		void			GetMethodNames( TestAutomation::RPCRequestInterface* pReq, TestAutomation::RPCServerInterface* pServer, TestAutomation::RPCReplyInterface* pReply );
virtual void			DialogNotification( char const* pTitle, char const* pMessage, char const* pButtons );
virtual	void			EventNotification ( const char* pType, const char* pMessage, int32 nVal );
virtual void			StatusNotification( const char* pMessage, uint32 uPercntg );
virtual void			WindowNotification( const char* pTitle, const char* pUID, const char* pStatus );

private:
		bool			IsRunning				( void ) { return this->mServerRunning; }
		int32			TCPPort					( void ) { return this->mTCPPort; }
		rpcSocket_t		MasterSocket			( void ) { return this->mMasterSocket; }
		int32			CreateMasterSocket		( void );
		void			AcceptConnection		( rpcSocket_t nMasterSocket );	
		bool			GetRequest				( RPCTransaction& trans, rpcSocket_t nSocket );
		bool			PreprocessRequest		( RPCTransaction& trans );
		void			ExecuteRequest			( RPCTransaction& trans );
		void			FinishTransaction		( RPCTransaction& trans );
		void			CloseSocket				( rpcSocket_t socketNum );
		bool			PollSockets				( void );
		bool			IsLocalClient			( unsigned char ipAddr[4] );
		bool			SendReply				( RPCReply& reply );
		bool			SendReply				( const char* pReplyMsg, rpcSocket_t nSocketNum );
		bool			SendReply				( const char* pReplyMsg, uint32 uLength, rpcSocket_t nSocketNum );
		void			NotifyClient			( const char* pMessage );
		void			LogTransfer				( char const* pTrans, uint32 uTransSize, int32 nRecursion=0 );
		char *			RPCTimeString			( void );
		void			Debug					( const char*msg );

		bool				mServerRunning;		
		int32				mNumClients;
		int32				mTCPPort;
		rpcSocket_t			mMasterSocket;
		rpcSocket_t			mClientSocket;
		bool				mSendNestedEventMsg;
		bool				mTransactionPending;
		bool				mEnableLogging;
		bool				mSuspendTransferLog;

		struct fd_set *		mSocketList;
		struct timeval *	mTimeOut; 
		unsigned char		mHostIPAddr[4];
		char				mHostName[64];
		char				mClientIPStr[32];
		char				mClientName[64];
		char				mServerID[32];
		RPCmapStringToFunc	mRPCMethods;
};

//=============================================================================
//	contains a request and its response
class RPCTransaction {
public:
	RPCTransaction ();

	TestAutomation::RPCfunctionPtr	reqFunc;
	RPCRequest						request;
	RPCReply						reply;
};