///////////////////////////////////////////////////////////
//
// RPCServer.cpp: RPC Server class for processing requests and sending replies
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#include <iostream>
#include <sstream>
#include <map>

#ifdef __APPLE__
#include <errno.h>
#include <unistd.h>
#include <sys/socket.h>
#include <time.h>
#include <netinet/in.h>
#include <netdb.h> 		
#include <sys/time.h>
#include <sys/sysctl.h>
#else //Windows
#include <WinSock2.h>
#include <time.h>
#endif 

#include "RPCServer.h"
#include "RPCMethods.h"

#define RPCSERVER_VERSION 7

using namespace TestAutomation;

//=============================================================================
extern "C" RPCSERVER_DLL_LINK RPCServerInterface* RPCServer_Create() {
	return new RPCServer();
}

//=============================================================================
extern "C" RPCSERVER_DLL_LINK uint32 RPCServer_Version() {
	return RPCSERVER_VERSION;
}

//=============================================================================
//	constructor
RPCServer::RPCServer() {
	mMasterSocket= 0;
	mClientSocket= 0;
	mTCPPort= 0;
	mTransactionPending= false;
	mSendNestedEventMsg= true;
	mServerRunning= false;
	mNumClients= 0;
	mEnableLogging= false;
	mSuspendTransferLog= false;

	mTimeOut= new timeval;
	mTimeOut->tv_sec= 0;
	mTimeOut->tv_usec= 0;

	this->mSocketList=  new fd_set;
	FD_ZERO(this->mSocketList);

	memset(mHostName,		0, sizeof(mHostName));	//zero it out
	memset(mHostIPAddr,		0, sizeof(mHostIPAddr));	//zero it out
	memset(mClientIPStr,	0, sizeof(mClientIPStr)); //zero it out
	memset(mClientName,		0, sizeof(mClientName));	//zero it out
	memset(mServerID,		0, sizeof(mServerID));	//zero it out
	
	RegisterMethods();
}

//=============================================================================
//	destructor
RPCServer::~RPCServer() {
	Debug("RPCServer::Destructor\n");
	if( mServerRunning) {
		NotifyClient("Destroyed");
		Stop();
	}
	delete mSocketList;
	delete mTimeOut;
}

//=============================================================================
int32 RPCServer::Start(const char* serverID, uint32 tcpPort) {
	int err= 0;

	if( mServerRunning)
		return 0;
	
	if( tcpPort > 0) {
		try {			
			mTCPPort= tcpPort;

			if( serverID)
#ifdef __APPLE__
            strncpy(mServerID, serverID, sizeof(mServerID)-1);
#else
            strncpy_s(mServerID, serverID, sizeof(mServerID)-1);
#endif
			err= CreateMasterSocket();
			if( err == 0)				
				mServerRunning= true;
		}
		catch (...)	 {
			err= 1;
		}
	}
	else {
        if( mEnableLogging )
            printf("RPCServer::Start - Invalid TCP port number: %d\n", tcpPort);
		err= 2;
	}
	
	return err;
}

//=============================================================================
int32 RPCServer::Stop() {
	mServerRunning= false;
    NotifyClient("Stopped");
    CloseSocket(mClientSocket);
    CloseSocket(mMasterSocket);
	return (mClientSocket==0 && mClientSocket==0)?0:-1;
}

//=============================================================================
int32 RPCServer::Version() {
	return RPCSERVER_VERSION;
}

//=============================================================================
//	Creates the master socket that we use to listen for connection requests.
//	Returns value is meaningless; throws C++ exception on error.
//	Implementation is mostly magic cribbed from TCP/IP book.
int32 RPCServer::CreateMasterSocket () {
	Debug("CreateMasterSocket");

#ifndef __APPLE__
	WSADATA wsd;
	if( WSAStartup(2, &wsd)) {
		Debug("CreateMasterSocket  WSAStartup failed:");
		return -1;
	}
#endif

	int32 opt= 1;
	int32 err= -1;
	struct sockaddr_in sin;	// an Internet endpoint address		
	mMasterSocket= socket( PF_INET, SOCK_STREAM, IPPROTO_TCP );

#ifdef __APPLE__
	if( mMasterSocket > 0 ) {
#else //Windows
	if( mMasterSocket != INVALID_SOCKET ) {
#endif	
        if( mEnableLogging )
            printf("RPCServer::CreateMasterSocket  master socket= %d\n", mMasterSocket);

		memset( &sin, 0, sizeof(sin) );	
		sin.sin_family= AF_INET;
		sin.sin_addr.s_addr= INADDR_ANY;
		sin.sin_port= htons(mTCPPort);	// fix byte ordering

        if( mEnableLogging )
            printf("RPCServer::CreateMasterSocket  port= %d\n", ntohs(sin.sin_port));

#ifdef __APPLE__
		setsockopt(mMasterSocket, SOL_SOCKET, SO_NOSIGPIPE, &opt, sizeof(opt));
        setsockopt(mMasterSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
#endif
		if( bind(mMasterSocket, (struct sockaddr *) &sin, sizeof(sin)) == 0 ) {
			if( listen(mMasterSocket, 1) == 0 ) {
				FD_SET(mMasterSocket, mSocketList);	//this value should always be mSocketList->fd_array[0]													

				//Record who we are!  host name and IP
				gethostname( mHostName, sizeof(mHostName) );
				hostent *hent= gethostbyname(mHostName);
				if( hent && hent->h_addrtype == AF_INET )
					memcpy( mHostIPAddr, hent->h_addr, sizeof(mHostIPAddr) );

				err= 0;
			}
			else
				Debug("CreateMasterSocket  can't listen on port.");
		}
		else
			Debug("CreateMasterSocket   can't bind to port.");
	}
	else
		Debug("CreateMasterSocket   can't create socket. error:");

	return err;
}

//=============================================================================
//	Close a socket
void RPCServer::CloseSocket ( rpcSocket_t socketNum ) {
	if( socketNum ) {
        if( mEnableLogging )
            printf("RPCServer::CloseSocket  closing socket %d\n", socketNum);
        #ifdef __APPLE__
        if( close(socketNum) == 0 )
        #else //Windows
        if( closesocket(socketNum) == 0 )
        #endif
        {
            FD_CLR(socketNum, mSocketList);
            if( socketNum == mClientSocket )
                mClientSocket= 0;
            else if( socketNum == mMasterSocket )
                mMasterSocket= 0;
        }
        else
            Debug("CloseSocket  closesocket error: %d (%s)");
    }
}

//=============================================================================
int32 RPCServer::ProcessSocket() {
	int32 err= 0;

	if( !mServerRunning )
		return -1;

	if( mTransactionPending && mSendNestedEventMsg ) {
		NotifyClient("Nested Loop Condition");  //Notify client so it doesn't time out waiting for a reply
		mSendNestedEventMsg= false;
	}

	if( PollSockets() ) {
		RPCTransaction trans;
		mTransactionPending= true;

		if( GetRequest( trans, mClientSocket ) ) {
			do {
				if( PreprocessRequest(trans) )
					ExecuteRequest(trans);

				FinishTransaction(trans);
			} 
			while( trans.request.mMultipleRequests );  //do all the requests in the request data
		}
		else
			CloseSocket( mClientSocket ); //i/o problem with this socket; shut it down

		mTransactionPending= false;
		mSendNestedEventMsg= true;
	}

	return err;
}

//=============================================================================
//	Polls the sockets to see if anything interesting is happening.
//	Returns true if anything happened, or false if nothing happened.
bool RPCServer::PollSockets (void) {
	bool sockReady= false;
	fd_set readSockets;

	//See if there is any data to be read on our sockets.  Set readSockets to our active sockets.
	//We must make a copy first because select() will modify the contents of readSockets
	memcpy(&readSockets, mSocketList, sizeof(readSockets));
	int32 numGoodSockets= select(FD_SETSIZE, &readSockets, (fd_set *) NULL, (fd_set *) NULL, mTimeOut);

	if(  numGoodSockets > 0 ) {
		if( FD_ISSET( mClientSocket, &readSockets ) )
			sockReady= true;
		else if( FD_ISSET(mMasterSocket, &readSockets) )
			AcceptConnection(mMasterSocket);
		else
			Debug("PollSockets receiving info on a socket that is not the Master or Connection socket");
	}
	else if( numGoodSockets < 0)
		Debug("PollSockets  select failed.");

	return sockReady;
}

//=============================================================================
//	Accepts a connection.
void RPCServer::AcceptConnection( rpcSocket_t masterSocket )	{
	struct	sockaddr_in fsin;	// the address of a client	
#ifdef __APPLE__
	socklen_t alen= sizeof(fsin);	// length of client's address	
#else //Windows
	int32	alen= sizeof(fsin);	// length of client's address	
#endif

	rpcSocket_t incomingSocket= accept(masterSocket, (struct sockaddr *) &fsin, &alen);
	
#ifdef __APPLE__
	if( incomingSocket != 0) {
#else //Windows
	if( incomingSocket != INVALID_SOCKET) {
#endif
        if( mEnableLogging )
            printf("RPCServer::AcceptConnection got connection on socket %d\n", incomingSocket);

		unsigned char ipAddr[4];
#ifdef __APPLE__
		ipAddr[3]= fsin.sin_addr.s_addr;
		ipAddr[2]= fsin.sin_addr.s_addr >> 8;
		ipAddr[1]= fsin.sin_addr.s_addr >> 16;
		ipAddr[0]= fsin.sin_addr.s_addr >> 24;
#else //Windows
		ipAddr[0]= fsin.sin_addr.S_un.S_un_b.s_b1;
		ipAddr[1]= fsin.sin_addr.S_un.S_un_b.s_b2;
		ipAddr[2]= fsin.sin_addr.S_un.S_un_b.s_b3;
		ipAddr[3]= fsin.sin_addr.S_un.S_un_b.s_b4;
#endif		

		//Only allow one remote connection at a time.
		//If we have a connection then politely refuse new connection
		if( mClientSocket ) {
            if( mEnableLogging )
                printf("RPCServer::AcceptConnection  refusing connection from %d:%d:%d:%d because there is already an active connection\n", ipAddr[0],ipAddr[1],ipAddr[2],ipAddr[3]);
			SendReply("connection refused by server because there is already an active connection", incomingSocket);
			CloseSocket(incomingSocket);
		}
		else {
			Debug("AcceptConnection Accepting incoming client");
			mClientSocket= incomingSocket;
#ifdef __APPLE__
            int32 opt= 1;
            setsockopt(mClientSocket, SOL_SOCKET, SO_NOSIGPIPE, &opt, sizeof(opt));
            setsockopt(mClientSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
#endif
			FD_SET(mClientSocket, mSocketList); 

			short one= 1;
			uint32 serverIDLen= (strlen(mServerID) < sizeof(mServerID)) ? uint32(strlen(mServerID)) : sizeof(mServerID)-1;
			int32 version= RPCSERVER_VERSION;
			uint32 replyBufSize= 8 + serverIDLen;
			char* replyBuf= new char[replyBufSize];
			memcpy(replyBuf,	&one,			2);	//allows receiver to detect correct byte order
			memcpy(replyBuf+2,	&version,		4);	//dll version
			memcpy(replyBuf+6,	&serverIDLen,	1); //mServerID length
			memcpy(replyBuf+7,	&mServerID,		strlen(mServerID)); //mServerID
			Debug("AcceptConnection Sending handshake");
			SendReply(replyBuf, replyBufSize, mClientSocket);  //short 1 + version number as int32 + serverID size + serverID

			//client will return "accepted" to ensure handshake has completed so we can start sending real data!
			//This prevents early notification from being sent during start up
			Debug("AcceptConnection Waiting for go response from client to start");
			int32 sz= recv(mClientSocket, replyBuf, 8, 0); //Read the next 2 bytes off the stream

			//Verify that the size is 8 and that the data equals "accepted"
			if( sz == 8 && strncmp( replyBuf, "accepted", 8 ) == 0 )
				Debug( "AcceptConnection Complete!" );
			else {
				Debug("AcceptConnection Refusing connection.  Invalid handshake!");
				SendReply("Refusing connection.  Refusing connection.  Invalid handshake!", incomingSocket);
				CloseSocket(incomingSocket);
			}

			delete replyBuf;
		}
	}
	else
		Debug("AcceptConnection accept failed");
}

//=============================================================================
//	Handles input on a connection.  
//	Returns true on success, false on communication error.
bool RPCServer::GetRequest ( RPCTransaction & trans, rpcSocket_t theSocket ) {
	bool RequestReady= false;
	char buf[1025];
	int32 bufSize= sizeof(buf)-1;
	int32 bytesRead= 0;
	int32 totalBytesRead= 0;
	int32 transSize= 0;

	//We need a socket in order to do something
	if(  theSocket ) {
		// read data from socket
        if( mEnableLogging )
            printf("RPCServer::GetRequest reading socket %d ... \n", theSocket );

		do {
			bytesRead= recv(theSocket, buf, 8, MSG_PEEK);  //wait until the transfer id and size bytes are there
		} while (bytesRead > 0 && bytesRead < 8);

		if( bytesRead == 8) {
			//init Transfer size
			memcpy(&transSize, buf+4, 4); // btye[0-3]= Transfer ID, bytes[4-7] Transfer size
			do
			{ //if the data is larger than our buf then we will need to do multiple reads
				if( transSize - totalBytesRead < bufSize)
					bufSize= transSize - totalBytesRead;

				bytesRead= recv(theSocket, buf, bufSize, 0); //Let's really get the data now
		
				if( bytesRead > 0) {
                    if( mEnableLogging )
                        printf("RPCServer::GetRequest %d bytes:\n", bytesRead);
					buf[bytesRead]= 0;	// make sure it's terminated
					trans.request.SetData(buf, bytesRead);
					totalBytesRead += bytesRead;
				}
				else
					break;
			}
			while ( transSize > totalBytesRead && bytesRead != -1);
		}
		
		if( bytesRead > 8)
			RequestReady= true;
		else
			Debug("GetRequest error");	 

		LogTransfer(trans.request.GetData(), trans.request.GetDataSize());
	}

	return RequestReady;
}

//=============================================================================
//	Preprocesses one request: parses it and looks up the method.
//	This method takes a request from the COMPLETE state to the READY state.
//	Returns false if an error was encountered (and an exception reply prepared).
bool RPCServer::PreprocessRequest ( RPCTransaction & trans )
{
	bool status= false;
	std::stringstream exceptionData;

	try {
		trans.request.Parse();
		trans.reply.SetTransferUID(trans.request.mRequestUID);

		trans.reqFunc= GetMethod(trans.request.GetFunctionName());
		if( trans.reqFunc == NULL)	// make sure the method was found
			throw "Method not found";

		status= true;  //We successfully found the function!
	}
	// catch every exception we can think of that might happen
	catch (const char*err) {
		exceptionData << err << " : " << (trans.request.GetFunctionName() ? trans.request.GetFunctionName() : "NULL FUNCTION NAME");
		trans.reply.SetException(exceptionData.str().c_str());
	}
	catch (std::string err) {
		trans.reply.SetException(err.c_str());
	}
	catch (...)	{
		// We don't ever want to fall into this exception catcher,
		// because we don't have anything useful to report.
		trans.reply.SetException("Unknown C++ Exception");
	}

	return status;
}

//=============================================================================
//	This is a private front-end for ExecuteRequest to ensure that all 
//	exceptions get caught.  ExecuteRequest is *supposed* to catch all
//	exceptions, but we can't afford to let one escape and wreak havoc
//	on an unsuspecting application.

void RPCServer::ExecuteRequest ( RPCTransaction & trans ) {
	Debug("RPCserver::ExecuteRequest()");

	std::stringstream excptnStr;

	try {	// call the handler function
		trans.reqFunc((RPCRequestInterface*)&trans.request, (RPCServerInterface*)this, (RPCReplyInterface*)&trans.reply);	
	}
	// catch every exception we can think of that might happen
	catch (const char*err) {
		trans.reply.SetException(err);
	}
	catch (std::string err) {
		trans.reply.SetException(err.c_str());
	}
	catch (std::exception err) {
		trans.reply.SetException(err.what());
	}
	catch (...) {
		trans.reply.SetException("Server Programming Error: Exception should have been caught earlier");
	}
}

//=============================================================================
void RPCServer::FinishTransaction (RPCTransaction & trans) {
	if(  trans.reply.IsExceptionSet() )
		trans.reply.FinishException(trans.request.GetFunctionName());
									
//	mSuspendTransferLog= trans.reply.mDoNotLog; //check to see if we don't want to log this reply
	SendReply( trans.reply );
	mSuspendTransferLog= false;  //make sure we reset this back to false

	if( trans.request.mMultipleRequests )
		trans.reply.Clear();  //clear the current reply if there are more transactions in the current request
}

//=============================================================================
//	Returns whether the given ip is the local host ip
bool RPCServer::IsLocalClient( unsigned char ipAddr[4] ) {
	if( ipAddr[0] == mHostIPAddr[0] && ipAddr[1] == mHostIPAddr[1] &&
		ipAddr[2] == mHostIPAddr[2] && ipAddr[3] == mHostIPAddr[3] )
		return true;
	
	return false;
}

//=============================================================================
//	Sends the OutgoingData's reply data.  Returns false on a socket error.
bool RPCServer::SendReply ( RPCReply & reply ) {
	reply.SetHeaderDataSize();
	return SendReply(reply.ReplyData(), reply.ReplySize(), mClientSocket);
}

//=============================================================================
//	Sends a reply.  Returns false on a socket error.
bool RPCServer::SendReply ( const char* reply, rpcSocket_t socketNum ) {
	return SendReply( reply, uint32(strlen(reply)), socketNum);
}

//=============================================================================
//	Sends a reply.  Returns false on a socket error.
bool RPCServer::SendReply ( const char* reply, uint32 length, rpcSocket_t socketNum ) {
	bool status= false;

	//Make sure we have everything!
	if( reply && length && socketNum > 0 )
	{
		Debug( "SendReply sending reply" );
		LogTransfer( reply, length );

		int32 bytesSent= send( socketNum, reply, length, 0 );
		
		if( bytesSent == length )
			status= true;
		else if( bytesSent < 0) { //SOCKET_ERROR
			Debug("SendReply reply send failed");
			CloseSocket(socketNum);
		}
		else {// bytesSent != length 
            if( mEnableLogging )
                printf("SendReply Warning! Only %d of %d bytes were sent!!!\n", bytesSent, length );
        }
	}
	else
		Debug("SendReply Reply not sent.  Incomplete parameters\n");

	return status;
}

//=============================================================================
//	Add one method to the class
int32 RPCServer::AddMethod (const char* name, RPCfunctionPtr func) {
	if( name == NULL )
		return -1;
	if( func == NULL )
		return -2;

	RPCmapStringToFunc::iterator p= mRPCMethods.find(name);
	if( p != mRPCMethods.end() )
		return -3; //already exists

	this->mRPCMethods[name]= func;
	return 0;
}

//=============================================================================
//	Add one method to the class
RPCfunctionPtr RPCServer::GetMethod (const char* name) {
	if( name != NULL) {
		RPCmapStringToFunc::iterator p= mRPCMethods.find(name);
		if( p != mRPCMethods.end())
			return p->second;
	}
	return NULL;
}

//=============================================================================
int32 RPCServer::RegisterMethods()
{
	RPCMethods::RegisterMethods(this);
	return 0;
}

//=============================================================================
void RPCServer::DialogNotification (char const *title, const char *message, const char *buttons)
{
	if( mClientSocket == 0 )
		PollSockets();  //See if someone is trying to connect before we ignore the notification

	if( mClientSocket ) {		
		RPCReply reply;
		reply.SetDialogNotification( title, message, buttons );
		SendReply(reply);
	}
}

//=============================================================================
void RPCServer::EventNotification (const char *type, const char *message, int32 ival) {
	if(  mClientSocket == 0 )
		PollSockets();  //See if someone is trying to connect before we ignore the notification

	if(  mClientSocket ) {		
		RPCReply reply;
		reply.SetEventNotification( type, message, ival );
		SendReply(reply);
	}
}

//=============================================================================
void RPCServer::StatusNotification(const char * message, uint32 percntg) {
	if(  mClientSocket == 0 )
		PollSockets();  //See if someone is trying to connect before we ignore the notification

	if(  mClientSocket ) {		
		RPCReply reply;						//Percentage is defined as 0 to 100 so this should be fine 
		reply.SetStatusNotification(message, (unsigned char)percntg);
		SendReply(reply);
	}
}

//=============================================================================
void RPCServer::WindowNotification(char const *title, char const *uid, char const *status) {
	if(  mClientSocket == 0 )
		PollSockets();  //See if someone is trying to connect before we ignore the notification

	if(  mClientSocket ) {		
		RPCReply reply;
		reply.SetWindowNotification(title, uid, status);
		SendReply(reply);
	}
}

//=============================================================================
void RPCServer::NotifyClient(const char*message) {
	// No Polling Sockets
	if(  mClientSocket ) {		
		RPCReply reply;
		reply.SetEventNotification("RPCServer", message, 0);
		SendReply(reply);
	}
}

//=============================================================================
void RPCServer::Debug(const char*msg) {
	if( mEnableLogging)
		printf("RPCServer::%s\n", msg);
}

//=============================================================================
void RPCServer::GetMethodNames(RPCRequestInterface* req, RPCServerInterface* server, RPCReplyInterface* reply) {
	for( RPCmapStringToFunc::iterator p= mRPCMethods.begin(); p != mRPCMethods.end(); p++ ) {
		const char* name= p->first.c_str();
		reply->AddString(name, "name");
	}
}

//=============================================================================
void RPCServer::LogTransfer (char const* trans, uint32 transSize, int32 recursion) {
	std::stringstream transString;
	std::string text;
	std::string dID;
	short s;
	float f;
	double d;
	unsigned short us;
	uint32 ul;
	int64 i64;
	uint64 u64;
	uint32 dSize;
	uint32 offset= 0;
	unsigned char c;
	unsigned char t;
	int32 i;
	bool isRequest= false;

	if( !mEnableLogging )
		return;

	if( recursion == 0 )
		Debug( RPCTimeString() );

	while( offset < transSize ) {
		for( i= 0; i < recursion; ++i )
			transString << "  ";

		text.clear();
		dID.clear();
		dID.append(trans+offset, 4); //id
		memcpy(&dSize, trans+offset+4, 4);
		transString << dID << "(" << dSize << ") ";

		if( dID == "RQST" || dID == "RPLY" ) {
			dSize= 12;
			int32 transUID= 0;
			memcpy(&transUID, trans+offset+8, 4);
			transString << "UID:" << transUID << "  ";

			if( dID == "RQST" )
				isRequest= true;
		}
		else if( dID == "XCPT" ) {
			text.append(trans+offset+8, dSize-8);
			transString << text;
		}
		else if( dID == "STAT" ) {
			memcpy(&c, trans+offset+8, 1);
			if( dSize > 9 )
				text.append(trans+offset+9, dSize-9);
			transString << (int32)c << "% " << text;
		}
		else if( dID == "Func" ) {
			text.append(trans+offset+8, dSize-8);
			transString << text;
		}
		else if( dID == "EVNT" ) {
			int32 lval;
			memcpy(&lval, trans+offset+8, 4);

			memcpy(&c, trans+offset+12, 1);
			if( c ) { //name size
				text.append(trans+offset+13, c);
				transString << text << "=";
			}

			if( dSize > (uint32)(13 + c) ) {
				text.clear();
				text.append(trans+offset+13+c, dSize-(13 + c));
				transString << text;
			}

			transString << " lval=" << lval;
		}
		else if( dID == "Stri" || dID == "Buff" || dID == "DLOG" || dID == "EVNT" ) {
			memcpy(&c, trans+offset+8, 1);
			if( c ) { //name size
				text.append(trans+offset+9, c);
				transString << text << "=";
			}

			if( dSize > (uint32)(9 + c) ) {
				text.clear();
				text.append( trans+offset+9+c, dSize-(9 + c) );
				transString << text;
			}
		}
		else if( dID == "WNDW" ) {
			transString << "title=";
			memcpy( &c, trans+offset+8, 1 );
			if( c ) { //status size
				text.append(trans+offset+9, c);
				transString << text;
			}

			transString << " : uid=";
			memcpy( &t, trans+offset+9+c, 1 );
			if( t ) { //uid size
				text.clear();
				text.append( trans+offset+10+c, t );
				transString << text;
			}

			transString << " : status=";
			if( dSize > (uint32)(10 + c + t) ) {
				text.clear();
				text.append(trans+offset+10+c+t, dSize-(10 + c + t));
				transString << text;
			}
		}
		else if( dID == "Bool" ) {
			memcpy(&c, trans+offset+8, 1);
			if( dSize > 9) {
				text.append(trans+offset+9, dSize-9);
				transString << text << "=";
			}
			transString << (int32)c;
		}
		else if( dID == "In16" ) {
			memcpy(&s, trans+offset+8, 4);
			if( dSize > 10) {
				text.append(trans+offset+10, dSize-10);
				transString << text << "=";
			}
			transString << s;
		}
		else if( dID == "UI16" ) {
			memcpy(&us, trans+offset+8, 4);
			if( dSize > 10) {
				text.append(trans+offset+10, dSize-10);
				transString << text << "=";
			}
			transString << us;
		}
		else if( dID == "In32") {
			memcpy(&i, trans+offset+8, 4);
			if( dSize > 12) {
				text.append(trans+offset+12, dSize-12);
				transString << text << "=";
			}
			transString << i;
		}
		else if( dID == "UI32" ) {
			memcpy(&ul, trans+offset+8, 4);
			if( dSize > 12) {
				text.append(trans+offset+12, dSize-12);
				transString << text << "=";
			}
			transString << ul;
		}
		else if( dID == "In64" ) {
			memcpy(&i64, trans+offset+8, 8);
			if( dSize > 16 ) {
				text.append(trans+offset+16, dSize-16);
				transString << text << "=";
			}
			transString << i64;
		}
		else if(  dID == "UI64" ) {
			memcpy(&u64, trans+offset+8, 8);
			if( dSize > 16) {
				text.append(trans+offset+16, dSize-16);
				transString << text << "=";
			}
			transString << u64;
		}
		else if( dID == "Flot" ) {
			memcpy(&f, trans+offset+8, 4);
			if( dSize > 12) {
				text.append(trans+offset+12, dSize-12);
				transString << text << "=";
			}
			transString << f;
		}
		else if( dID == "Dble" ) {
			memcpy(&d, trans+offset+8, 8);
			if( dSize > 16) {
				text.append(trans+offset+16, dSize-16);
				transString << text << "=";
			}
			transString.precision(15);
			transString << d;
			transString.precision(0);
		}
		else if( dID == "Cmpd" ) {
			memcpy(&c, trans+offset+8, 1);
			if( c ) { //name size
				text.append(trans+offset+9, c);
				transString << text << "=";
			}

			memcpy(&t, trans+offset+9+c, 1);
			if( t ) { //type name size
				text.clear();
				text.append(trans+offset+10+c, t);
				transString << text;
			}

			Debug(transString.str().c_str());
			Debug("\n");
			LogTransfer(trans+offset+10+c+t, dSize-(10+c+t), recursion+1);
		}
		else if( offset == 0 ) {	
			//This must not a transfer stream so just log it and leave
			Debug(trans);
			break;
		}

		offset += dSize;

		if( dID != "Cmpd")//Cmpd has its own LogPrintf
			Debug(transString.str().c_str());

		transString.str("");

//		if( dID != "RQST" )
//			Debug("\n");
			
		if(  mSuspendTransferLog ) {
			Debug("logging has been suspended for this notification!\n");
			break; //leave now!
		}
	}
	if( !isRequest && recursion == 0 )
		Debug("\n");
}

//=============================================================================
char * RPCServer::RPCTimeString (void) {
	static char timeStr[26]; //must be atleast 26 bytes for asctime_r()
#ifdef __APPLE__
	time_t rawtime;
	time ( &rawtime );
	asctime_r (localtime ( &rawtime ), timeStr);
	timeStr[strlen(timeStr) - 1]='\0'; //remove the trailing carriage return
#else //Windows
	_strtime_s(timeStr);
#endif
	return timeStr;
}

//#############################################################################
//
//		RPCTransaction methods
//
//#############################################################################

RPCTransaction::RPCTransaction( void ) :
	reqFunc( NULL ),
	reply(),
	request() {
}