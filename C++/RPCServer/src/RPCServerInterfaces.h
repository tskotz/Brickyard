///////////////////////////////////////////////////////////
//
// RPCServerInterfaces.h: Test Automation public class interfaces
// Copyright (c) 2015 - Brickyard, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once

#include "RPCServerTypes.h"

#ifdef __APPLE__
#  ifdef RPCSERVER_LIB_EXPORTS
#	 define RPCSERVER_DLL_LINK __attribute__((visibility("default")))
#  else
#    define RPCSERVER_DLL_LINK
#  endif
#else
#  ifdef RPCSERVER_LIB_EXPORTS
#    define RPCSERVER_DLL_LINK __declspec(dllexport) 
#  else
#    define RPCSERVER_DLL_LINK __declspec(dllimport)
#  endif
#endif

namespace TestAutomation {
	//////////////////////////////////////////////////////////////////////////////
	//! RPCServer interface for communicating with the TestAutomation RPC Server 
	class RPCServerInterface {
	public:
		virtual ~RPCServerInterface() {};
		virtual int32 Start( const char* pServerID, uint32 uTCPPort )= 0; //!< Attempts to open a socket on the specided port for listening for requests
		virtual int32 Stop()= 0;										 //!< Prevents incoming requests from being processed
		virtual int32 ProcessSocket()= 0;								 //!< Checks socket for incoming requests and processes them
		virtual int32 Version()= 0;										//!< Returns the version of the DLL
		//! Registers a method with the RPC Server
		virtual int32 AddMethod( const char* pMethodName, RPCfunctionPtr pFunc )= 0;
		//! Internal method that will return a dump of all the registered methods via AddMethod()
		virtual void GetMethodNames( RPCRequestInterface* pReq, RPCServerInterface* pServer, RPCReplyInterface* pReply )= 0;
		//! Sends a DLOG notification to java RPC server
		virtual void DialogNotification( const char* pTitle, const char* pMessage, const char* pButtons )= 0;
		//! Sends an EVNT notification to java RPC server
		virtual void EventNotification( const char* pType, const char* pMessage, int32 nVal )= 0;
		//! Sends a STAT notification to java RPC server
		virtual void StatusNotification( const char* pMessage, uint32 uPercntg )= 0;
		//! Sends a WNDW notification to java RPC server
		virtual void WindowNotification( const char* pTitle, const char* pUID, const char* pStatus )= 0;		
	}; // end class RPCServerInterface

	//////////////////////////////////////////////////////////////////////////////
	//! RPCServer Request interface - Contains data being sent to the hook from the java RPC Server 
	class RPCRequestInterface {
	public:
		virtual uint32		GetCount	( )= 0;							//!< Number of data parameters in request
		virtual const char*	GetName		( uint32 uPosition )= 0;		//!< Gets the name of the data param at position uPosition
		virtual uint32		GetTypeID	( uint32 uPosition )= 0;		//!< Gets the TypeID of the data param at position uPosition
		virtual uint32		GetTypeID	( const char* pParamName )= 0;	//!< Gets the TypeID of the data param with the supplied param name
		virtual bool		GetBool		( uint32 uPosition)= 0;			//!< Gets the bool value of the data param at position uPosition
		virtual bool		GetBool		( const char* pParamName )= 0;	//!< Gets the bool value of the data param with the supplied param name
		virtual int16		GetInt16	( uint32 uPosition )= 0;		//!< Gets the int16 value of the data param at position uPosition
		virtual int16		GetInt16	( const char* pParamName )= 0;	//!< Gets the int16 value of the data param with the supplied param name
		virtual uint16		GetUInt16	( uint32 uPosition)= 0;			//!< Gets the uint16 value of the data param at position uPosition
		virtual uint16		GetUInt16	( const char* pParamName )= 0;	//!< Gets the uint16 value of the data param with the supplied param name
		virtual int32		GetInt32	( uint32 uPosition)= 0;			//!< Gets the int32 value of the data param at position uPosition
		virtual int32		GetInt32	( const char* pParamName )= 0;	//!< Gets the int32 value of the data param with the supplied param name
		virtual uint32		GetUInt32	( uint32 uPosition)= 0;			//!< Gets the uint32 value of the data param at position uPosition
		virtual uint32		GetUInt32	( const char* pParamName )= 0;	//!< Gets the uint32 value of the data param with the supplied param name
		virtual int64		GetInt64	( uint32 uPosition)= 0;			//!< Gets the int64 value of the data param at position uPosition
		virtual int64		GetInt64	( const char* pParamName )= 0;	//!< Gets the int64 value of the data param with the supplied param name
		virtual uint64		GetUInt64	( uint32 uPosition)= 0;			//!< Gets the uint64 value of the data param at position uPosition
		virtual uint64		GetUInt64	( const char* pParamName )= 0;	//!< Gets the uint64 value of the data param with the supplied param name
		virtual float		GetFloat	( uint32 uPosition)= 0;			//!< Gets the float value of the data param at position uPosition
		virtual float		GetFloat	( const char* pParamName )= 0;	//!< Gets the float value of the data param with the supplied param name
		virtual double		GetDouble	( uint32 uPosition)= 0;			//!< Gets the double value of the data param at position uPosition
		virtual double		GetDouble	( const char* pParamName )= 0;	//!< Gets the double value of the data param with the supplied param name
		virtual const char*	GetString	( uint32 uPosition)= 0;			//!< Gets the string value of the data param at position uPosition
		virtual const char*	GetString	( const char* pParamName )= 0;	//!< Gets the string value of the data param with the supplied param name
		virtual const char*	GetBuffer	( uint32 uPosition)= 0;			//!< Gets the buffer value of the data param at position uPosition
		virtual const char*	GetBuffer	( const char* pParamName )= 0;	//!< Gets the buffer value of the data param with the supplied param name
		virtual uint32		GetBufferSize	( uint32 uPosition )= 0;	//!< Gets the buffer size of the Buffer data param at position uPosition
		virtual uint32		GetBufferSize	( const char* pParamName )= 0;	//!< Gets the buffer size of the Buffer data param with the supplied param name
	}; // end class RPCRequestInterface

	//////////////////////////////////////////////////////////////////////////////
	//! RPCServer Request interface - Contains data being sent to the hook from the java RPC Server 
	class RPCReplyInterface {
	public:
		virtual void		AddBool			( bool bValue,			const char* pParamName )= 0; //!< Adds a bool value to the Reply with the supplied data param name
		virtual void		AddInt16		( int16 nValue,			const char* pParamName )= 0; //!< Adds a int16 value to the Reply with the supplied data param name
		virtual void		AddUInt16		( uint16 uValue,		const char* pParamName )= 0; //!< Adds a uint16 value to the Reply with the supplied data param name
		virtual void		AddInt32		( int32 nValue,			const char* pParamName )= 0; //!< Adds a int32 value to the Reply with the supplied data param name
		virtual void		AddUInt32		( uint32 uValue,		const char* pParamName )= 0; //!< Adds a uint32 value to the Reply with the supplied data param name
		virtual void		AddInt64		( int64 nValue,			const char* pParamName )= 0; //!< Adds a int64 value to the Reply with the supplied data param name
		virtual void		AddUInt64		( uint64 uValue,		const char* pParamName )= 0; //!< Adds a uint64 value to the Reply with the supplied data param name
		virtual void		AddFloat		( float fValue,			const char* pParamName )= 0; //!< Adds a float value to the Reply with the supplied data param name
		virtual void		AddDouble		( double dValue,		const char* pParamName )= 0; //!< Adds a double value to the Reply with the supplied data param name
		virtual void		AddString		( const char* pValue,	const char* pParamName )= 0; //!< Adds a string value to the Reply with the supplied data param name
		virtual void		AddBuffer		( const char* pValue, uint32 uSize, const char* pParamName )= 0; //!< Adds a buffer to the Reply with the supplied data param name
		virtual void		SetException	( const char* pErrMsg )= 0; //!< Adds an exception data param
		virtual bool		IsExceptionSet	( )= 0; //!< Checks if an exception has been set via SetException()
	}; // end class RPCReplyInterface

} //end namespace TestAutomation

// izTestAutomation DLL Functions and typedefs
extern "C" RPCSERVER_DLL_LINK TestAutomation::RPCServerInterface*	RPCServer_Create( );
extern "C" RPCSERVER_DLL_LINK uint32								RPCServer_Version( );

extern "C" typedef TestAutomation::RPCServerInterface*	(*FnRPCServer_Create)( );
extern "C" typedef uint32								(*FnRPCServer_Version)( );
