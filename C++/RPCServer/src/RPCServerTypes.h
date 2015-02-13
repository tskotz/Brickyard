///////////////////////////////////////////////////////////
//
// RPCServerTypes.h: Test Automation types
// Copyright (c) 2015 - Brickyard, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once

namespace TestAutomation {
	class RPCRequestInterface; class RPCReplyInterface; class RPCServerInterface;
	typedef void (*RPCfunctionPtr) ( RPCRequestInterface* pReq, RPCServerInterface* pServer, RPCReplyInterface* pReply );
}

#ifdef __APPLE__
typedef int16_t             int16;
typedef u_int16_t			uint16;
typedef int32_t				int32;
typedef u_int32_t			uint32;
typedef int64_t				int64;
typedef u_int64_t			uint64;
#else
typedef short				int16;
typedef unsigned short		uint16;
typedef int					int32;
typedef unsigned int		uint32;
typedef __int64				int64;
typedef unsigned __int64	uint64;
#endif

//=============================================================================
//	describes the type ID of a data chunk

#define RPC_CHAR4( ch0, ch1, ch2, ch3 )	(uint32) ( \
	((uint32)(unsigned char)(ch0))       | \
	((uint32)(unsigned char)(ch1) << 8 ) | \
	((uint32)(unsigned char)(ch2) << 16) | \
	((uint32)(unsigned char)(ch3) << 24) )

#define RPCRequestChunkID		RPC_CHAR4('R','Q','S','T')
#define RPCReplyChunkID			RPC_CHAR4('R','P','L','Y')
#define RPCEventChunkID			RPC_CHAR4('E','V','N','T')
#define RPCStatusChunkID		RPC_CHAR4('S','T','A','T')
#define RPCDialogChunkID		RPC_CHAR4('D','L','O','G')
#define RPCWindowChunkID		RPC_CHAR4('W','N','D','W')
#define RPCExceptionChunkID		RPC_CHAR4('X','C','P','T')

#define RPCStringDatumID		RPC_CHAR4('S','t','r','i')
#define RPCBoolDatumID			RPC_CHAR4('B','o','o','l')
#define RPCInt16DatumID			RPC_CHAR4('I','n','1','6')
#define RPCUInt16DatumID		RPC_CHAR4('U','I','1','6')
#define RPCInt32DatumID			RPC_CHAR4('I','n','3','2')
#define RPCUInt32DatumID		RPC_CHAR4('U','I','3','2')
#define RPCInt64DatumID			RPC_CHAR4('I','n','6','4')
#define RPCUInt64DatumID		RPC_CHAR4('U','I','6','4')
#define RPCFloatDatumID			RPC_CHAR4('F','l','o','t')
#define RPCDoubleDatumID		RPC_CHAR4('D','b','l','e')
#define RPCBufferDatumID		RPC_CHAR4('B','u','f','f')
#define RPCFuncionDatumID		RPC_CHAR4('F','u','n','c')

#define RPCNULLDatumID			RPC_CHAR4('0','0','0','0')

namespace RPCModKeyFlags {
	enum RPCmodifierFlags {
		NONE=			0x00000000,
		SHIFT=			0x00020000,
		CONTROL=		0x00040000,
		ALT=			0x00080000,
		COMMAND=		0x00100000,
		SECFUNCTION=	0x00400000, //Mac
		NUMERICPAD=		0x00800000, //Mac: Identifies key events from numeric keypad area on extended keyboards
		WINDOWSKEY=		0x01000000  //Win: Windows key
	};
}