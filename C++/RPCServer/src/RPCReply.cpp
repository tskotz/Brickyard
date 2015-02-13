///////////////////////////////////////////////////////////
//
// RPCReply.cpp: RPC incoming reply
// Copyright (c) 2015 - Brickyard, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#include <iostream>
#include "RPCServerInterfaces.h"
#include "RPCReply.h"
#include <sstream>

//=============================================================================
RPCReply::RPCReply() :
	mDataSize(0),
	mExceptionSet(0)
{
	mBufferSize= 500;
	mDataBuffer= new char [this->mBufferSize];

	//Initialize header
//	#if __APPLE__		
//	uint32 tmp= CFSwapInt32HostToLittle(RPCReplyChunkID);
//	#else
	uint32 tmp= RPCReplyChunkID;
//	#endif
	
	AppendMessage(&tmp, 4); //Transfer Chunk ID
	tmp= 12; //default transfer size
	AppendMessage(&tmp, 4);//Transfer Size
	tmp= 0;
	AppendMessage(&tmp, 4);//Transfer UID default
}

//=============================================================================
RPCReply::~RPCReply ()
{
	delete [] mDataBuffer;
}

//=============================================================================
void RPCReply::SetHeaderDataSize (void)
{
	memcpy(mDataBuffer + 4, &mDataSize, 4); //bytes 4,5,6,7
}

//=============================================================================
void RPCReply::SetTransferUID (int32 uid)
{
	memcpy(mDataBuffer + 8, &uid, 4); //bytes 8,9,10,11
}

//=============================================================================
void RPCReply::SetException ( const char* message )	
{
	mExceptionSet= true;
	mExceptionStr= message; 	
}

//=============================================================================
bool RPCReply::IsExceptionSet()
{
	return mExceptionSet;
}

//=============================================================================
void RPCReply::FinishException(const char * funcName)
{
	std::stringstream excptSS;

	excptSS << mExceptionStr;
	if( funcName)  excptSS << ", Function: " << funcName;

	OverwriteDatum( RPCExceptionChunkID, excptSS.str().c_str() );
}

//=============================================================================
void RPCReply::Clear()
{
	mDataSize= 12;
	SetHeaderDataSize();
}

//=============================================================================
uint32 RPCReply::OverwriteDatum ( int32 datumTypeID, const char * message)
{
	uint32 messageSize= ( message ? uint32(strlen(message)) : 0 );
	uint32 datumSize= 8 + messageSize;

	mDataSize= 0;  //Reset data size to 0
	if(  !CheckBufferSize(datumSize) )
		return -1;

	#if __APPLE__		
//	datumTypeID= CFSwapInt32HostToLittle(datumTypeID);
	#endif
	
	AppendMessage(&datumTypeID, 4);		//4 bytes datum type
	AppendMessage(&datumSize, 4);		//4 bytes datum size
	if(  message )
		AppendMessage(message, messageSize);//messageSize bytes - the actual message data

	return 0;
}

//=============================================================================
uint32 RPCReply::OverwriteDatum ( int32 datumTypeID, const char * message, unsigned char percentage)
{
	uint32 messageSize= ( message ? uint32(strlen(message)) : 0 );
	uint32 datumSize= 9 + messageSize;

	mDataSize= 0;  //Reset data size to 0
	if(  !CheckBufferSize(datumSize) )
		return -1;

	#if __APPLE__	
//	datumTypeID= CFSwapInt32HostToLittle(datumTypeID);
	#endif

	AppendMessage(&datumTypeID,  4);	//4 bytes datum type
	AppendMessage(&datumSize, 4);		//4 bytes datum size
	AppendMessage(&percentage, 1);		//1 bytes uCharData
	if(  message )
		AppendMessage(message, messageSize);//dataSize bytes - the actual data

	return 0;
}

//=============================================================================
uint32 RPCReply::OverwriteDatum (int32 datumTypeID, const char * type, const char * message, int32 ival)
{
	unsigned char	typeSize= ( type ? (strlen(type)<255?uint32(strlen(type)):255) : 0 );
	uint32			messageSize= ( message ? uint32(strlen(message)) : 0 );
	uint32			datumSize= 13 + typeSize + messageSize;

	mDataSize= 0;  //Reset data size to 0
	if(  !CheckBufferSize(datumSize) )
		return -1;

	#if __APPLE__	
//	datumTypeID= CFSwapInt32HostToLittle(datumTypeID);
	#endif

	AppendMessage(&datumTypeID, 4);		//4 bytes datum type
	AppendMessage(&datumSize, 4);		//4 bytes datum size
	AppendMessage(&ival, 4);			//4 bytes lval size
	AppendMessage(&typeSize, 1);		//1 bytes status size
	if(  type )
		AppendMessage(type, typeSize);//titleSize bytes - the actual title data
	if(  message )
		AppendMessage(message, messageSize);//statusSize bytes - the actual status data

	return 0;
}

//=============================================================================
uint32 RPCReply::OverwriteDatum (int32 datumTypeID, const char * title, const char * uid, const char * status)
{
	unsigned char titleSize= ( title ? (strlen(title)<255?uint32(strlen(title)):255) : 0 );
	unsigned char uidSize= ( uid ? (strlen(uid)<255?uint32(strlen(uid)):255) : 0 );
	uint32		  statusSize= ( status ? uint32(strlen(status)) : 0 );
	uint32		  datumSize= 10 + titleSize + uidSize + statusSize;

	mDataSize= 0;  //Reset data size to 0
	if(  !CheckBufferSize(datumSize) )
		return -1;
	
	#if __APPLE__		
//	datumTypeID= CFSwapInt32HostToLittle(datumTypeID);
	#endif

	AppendMessage(&datumTypeID, 4);		//4 bytes datum type
	AppendMessage(&datumSize, 4);		//4 bytes datum size
	AppendMessage(&titleSize, 1);		//1 bytes datum size
	if(  title )
		AppendMessage(title, titleSize);// titleSize bytes - the actual status data
	AppendMessage(&uidSize, 1);			//1 bytes datum size
	if(  uid )
		AppendMessage(uid, uidSize);	//uidSize bytes - the actual uid data
	if(  status )
		AppendMessage(status, statusSize);//statusSize bytes - the actual title data

	return 0;
}

//=============================================================================
void RPCReply::AppendMessage ( const void * value, int32 numBytes )
{
	if(  value && numBytes > 0 )
	{
		memcpy(mDataBuffer + mDataSize, value, numBytes);
		mDataSize += numBytes;
	}
}

//=============================================================================
void RPCReply::AppendDatum ( int32 datumTypeID, const char * name, const void * data, uint32 dataSize, bool dynamicData )
{
	unsigned char nameSize= ( name ? (strlen(name)<255?uint32(strlen(name)):255) : 0 );
	uint32		  datumSize= 8 + nameSize + dataSize + (dynamicData?1:0);

	if(  !CheckBufferSize(datumSize) )
		throw "CheckBufferSize Failed";

	#if __APPLE__	
//	datumTypeID= CFSwapInt32HostToLittle(datumTypeID);
	#endif

	AppendMessage(&datumTypeID, 4);		//4 bytes datum type
	AppendMessage(&datumSize, 4);		//4 bytes datum size

	if( dynamicData)
	{
		AppendMessage(&nameSize, 1);	//1 byte  name size because we can't determine because data size is dynamic

		if(  name )
			AppendMessage(name, nameSize);	//nameSize bytes - the actual name data (text)
		if(  data )
			AppendMessage(data, dataSize);	//dataSize bytes - the actual data
	}
	else
	{
		if(  data )
			AppendMessage(data, dataSize);	//dataSize bytes - the actual data
		if(  name )
			AppendMessage(name, nameSize);	//nameSize bytes - the actual name data (text)
	}
}

//=============================================================================
bool RPCReply::CheckBufferSize (uint32 newBytes)
{
	if(  (mDataSize + newBytes) > mBufferSize )
	{	//create a new, larger buffer
		uint32 newBufferSize= mDataSize*2 + newBytes;
		char * tmp= new char [newBufferSize];

		if(  tmp )
		{
			memcpy(tmp, mDataBuffer, mBufferSize);
			delete [] mDataBuffer;
			mDataBuffer= tmp;
			mBufferSize= newBufferSize;
		}
		else
			return false;
	}
	return true;
}

//=============================================================================
void RPCReply::AddBool (bool value,	const char* paramName)
{
	unsigned char bVal= (value ? 1 : 0); //convert it to an unsigned char
	AppendDatum( RPCBoolDatumID, paramName, &bVal, 1, false );				
}

//=============================================================================
void RPCReply::AddInt16 (int16 value,	const char* paramName)
{
	AppendDatum( RPCInt16DatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddUInt16 (uint16 value,	const char* paramName)
{
	AppendDatum( RPCUInt16DatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddInt32 (int32 value,	const char* paramName)
{
	AppendDatum( RPCInt32DatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddUInt32 (uint32 value,	const char* paramName)
{
	AppendDatum( RPCUInt32DatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddInt64 (int64 value,	const char* paramName)
{
	AppendDatum( RPCInt64DatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddUInt64 (uint64 value,	const char* paramName)
{
	AppendDatum( RPCUInt64DatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddFloat (float value,	const char* paramName)
{
	AppendDatum( RPCFloatDatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddDouble (double value,	const char* paramName)
{
	AppendDatum( RPCDoubleDatumID, paramName, &value, sizeof(value), false );
}

//=============================================================================
void RPCReply::AddString (const char * value, const char* paramName)
{
	AppendDatum( RPCStringDatumID, paramName, value, (value ? uint32(strlen(value)) : 0), true );
}

//=============================================================================
void RPCReply::AddBuffer (const char * value, uint32 size, const char* paramName)
{
	AppendDatum( RPCBufferDatumID, paramName, value, size, true );
}

//=============================================================================
void RPCReply::SetDialogNotification(char const *title, char const * message, char const * buttons)
{
	OverwriteDatum( RPCDialogChunkID, title, buttons, message );
}

//=============================================================================
void RPCReply::SetEventNotification(const char * type, char const * message, int32 ival)
{
	OverwriteDatum( RPCEventChunkID, type, message, ival );
}

void RPCReply::SetStatusNotification (const char * message, unsigned char percentage)	
{
	OverwriteDatum( RPCStatusChunkID, message, percentage );				
}

//=============================================================================
void RPCReply::SetWindowNotification(char const *title, char const *uid, char const *status)
{
	OverwriteDatum( RPCWindowChunkID, title, uid, status );
}