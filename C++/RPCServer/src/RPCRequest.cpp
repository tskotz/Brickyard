///////////////////////////////////////////////////////////
//
// RPCRequest.cpp: RPC outgoing request
// Copyright (c) 2015 - Brickyard, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#include <iostream>
#include "RPCServerInterfaces.h"
#include "RPCServerTypes.h"
#include "RPCRequest.h"
#include "RPCChunk.h"
#include <sstream>

RPCRequest::RPCRequest() :
	mData(NULL),
	mDataSize(0),
	mMultipleRequests(0),
	mParsed(0),
	mRequestUID(0),
	mDataOffset(0),
	mFunctionDatum(0)
{
}

const char * TypeToString(uint32 datumType) {
	static char typeStr[5]= {0};
	memcpy((void*)typeStr, &datumType, 4);
	typeStr[4]= 0;

	return typeStr;
}

//=============================================================================
//	Reads input from the given buffer until a zero is found or a complete
//	input message is found.  Returns true if it found a complete message.

void RPCRequest::SetData ( void const * copyData, uint32 copySize ) {
	if( copyData && copySize) {
		//Calculate the bufSize and allocate the memory
		uint32 bufSize= this->mDataSize + copySize;
		char * tmpBuf= new char[bufSize];

		//make sure the allocation worked
		if( tmpBuf) {
			//make a copy of the current data
			if( this->mData)
				memcpy((char *)tmpBuf, this->mData, this->mDataSize);

			memcpy((char *)(tmpBuf + this->mDataSize), copyData, copySize);
		}

		//delete the existing data
		delete [] this->mData;

		//now point the data to the new buffer
		this->mData= tmpBuf;
		this->mDataSize= bufSize;
	}
}

//=============================================================================
const char* RPCRequest::GetData () {
	return this->mData;
}

//=============================================================================
uint32 RPCRequest::GetDataSize () {
	return this->mDataSize;
}

//=============================================================================
bool RPCRequest::Parse (void) {
	RPCDatum * d;

	//Account for multiple requests in same data buffer
	if( !mMultipleRequests && mParsed)
		return false;

	if( mMultipleRequests)
		RemoveAll(); //clear out all the previous datums

	mParsed= true;

	if( mDataSize - mDataOffset > 12 ) {
		uint32 requestType;
		uint32 requestSize;
		const char * reqBytes= mData + mDataOffset;

		memcpy(&requestType, reqBytes, 4); //bytes 0-3 : Request Type

		#if __APPLE__		
//		requestType= CFSwapInt32HostToLittle(requestType);
		#endif

		//Make sure we recognize the request type
		if( requestType == RPCRequestChunkID ) {
			memcpy(&requestSize, reqBytes+4, 4); //bytes 4-7 : Request Size
			memcpy(&mRequestUID, reqBytes+8, 4); //bytes 8-11 : Request UID

			//make sure we have the bits and that the requestSize is ok
			if( requestSize >= 20 && (requestSize + mDataOffset) <= mDataSize ) {
				uint32 datumOffset= 12; //datums starts at byte 12
				while (datumOffset < requestSize ) {
					//!!!ParseDatum will update datumOffset with the bytes processed
					d= ParseDatum(reqBytes+datumOffset, datumOffset, requestSize);
					if( d == NULL ) {
						mDataOffset += requestSize; //move mDataOffset past this request 
						throw "NULL Datum detected!";
					}
					else if( d->GetTypeID() == RPCFuncionDatumID )
						mFunctionDatum= ((RPCFunctionDatum*)d);
					else
						AddDatum(d); // add it to our list
				}
				mDataOffset += datumOffset;
			}
			else
				throw "Incomplete transfer bytes!";

			if( mDataSize > mDataOffset)
				mMultipleRequests= true;
			else
				mMultipleRequests= false;
		}
		else
			throw "Unknown Transfer ID!";
	}
	else
		throw "Insufficient bytes in RQST transfer!";

	return true;
}

//=============================================================================
//	Parses one datum, and returns a pointer to the new object.
//	Returns NULL if it can't parse one.

RPCDatum * RPCRequest::ParseDatum ( const char* data, uint32 & offset, uint32 maxSize ) {
	int32  datumID;
	uint32 datumSize;

	memcpy(&datumID, data, 4);		//Datum ID.  bytes 0-3
	memcpy(&datumSize, data+4, 4);	//Datum Size bytes 4-7
	offset += datumSize; //now that we have the datum size.  bump the offest

	#if __APPLE__		
//	datumID= CFSwapInt32HostToLittle(datumID);
	#endif
	
	if( datumSize > maxSize )
		throw "Bogus datum size!!";
	if( datumID == RPCFuncionDatumID )
		return new RPCFunctionDatum(data, datumSize);
	if( datumID == RPCStringDatumID )
		return new RPCStringDatum(data, datumSize);
	if( datumID == RPCBoolDatumID )
		return new RPCBoolDatum(data, datumSize);
	if( datumID == RPCInt32DatumID )
		return new RPCInt32Datum(data, datumSize);
	if( datumID == RPCUInt32DatumID )
		return new RPCUInt32Datum(data, datumSize);
	if( datumID == RPCInt16DatumID )
		return new RPCInt16Datum(data, datumSize);
	if( datumID == RPCUInt16DatumID )
		return new RPCUInt16Datum(data, datumSize);
	if( datumID == RPCInt64DatumID )
		return new RPCInt64Datum(data, datumSize);
	if( datumID == RPCUInt64DatumID )
		return new RPCUInt64Datum(data, datumSize);
	if( datumID == RPCFloatDatumID )
		return new RPCFloatDatum(data, datumSize);
	if( datumID == RPCDoubleDatumID )
		return new RPCDoubleDatum(data, datumSize);
	if( datumID == RPCBufferDatumID )
		return new RPCBufferDatum(data, datumSize);

	return NULL;
}

//=============================================================================
//	Adds a contained datum, which will be owned by the compound datum.
//	Returns true on success, false if datum doesn't match namedness.

bool RPCRequest::AddDatum ( RPCDatum * datum ) {
	if( datum == NULL)
		throw "Attempting to add NULL datum";

	mContents.push_back(datum);
	return true;
}

//=============================================================================
//	clears the request so the object can be reused

void RPCRequest::Clear() {
	mParsed= false;
	mDataOffset= 0;
	mMultipleRequests= false;
	delete mFunctionDatum;
	mFunctionDatum= NULL;
}

//=============================================================================
//	remove all contained elements

void RPCRequest::RemoveAll (void) {
//	for( int32 i= GetCount()-1; i >= 0; i--)
//		delete GetNth(i);
//	this->contents.clear();
}

//=============================================================================
//	GetFunctionName
const char* RPCRequest::GetFunctionName (void) {
	if( mFunctionDatum)
		return mFunctionDatum->GetFunction();

	return NULL;
}

//=============================================================================
RPCDatum * RPCRequest::GetDatum ( uint32 position, uint32 typeID ) {
	RPCDatum * d= NULL;
	std::stringstream err;

	if( position < mContents.size())
		d= mContents.at(position);

	if( d == NULL) {
		err << "Datum not found at requested postion: " << position;
		throw err.str();
	}
	
	if( typeID != RPCNULLDatumID && d->GetTypeID() != typeID) {
		err << "Expected datum at postion: " << position << " to be " << TypeToString(typeID) << " instead of " << TypeToString(d->GetTypeID());
		throw err.str();
	}

	return d;
}

//=============================================================================
RPCDatum * RPCRequest::GetDatum ( const char* paramName, uint32 typeID ) {
	RPCDatum * d= NULL;
	std::stringstream err;

	if( paramName != NULL) {
		for( std::vector<RPCDatum *>::iterator iter= mContents.begin(); d == NULL && iter != mContents.end(); ++iter) {
			const char * name= (*iter)->GetName();
			if( name != NULL && strcmp(name, paramName) == 0)
				d= *iter;
		}
	}

	if( d == NULL) {
		err << "Datum not found: " << paramName;
		throw err.str();
	}
	
	if( typeID != RPCNULLDatumID && d->GetTypeID() != typeID) {
		err << "Expected datum " << paramName << " to be " << TypeToString(typeID) << " instead of " << TypeToString(d->GetTypeID());
		throw err.str();
	}

	return d;
}

//=============================================================================
uint32 RPCRequest::GetCount () {
	return uint32(mContents.size());
}

//=============================================================================
const char* RPCRequest::GetName (uint32 position) {
	 return GetDatum(position, RPCNULLDatumID)->GetName();
}

//=============================================================================
uint32 RPCRequest::GetTypeID (uint32 position) {
	 return GetDatum(position, RPCNULLDatumID)->GetTypeID();
}

uint32 RPCRequest::GetTypeID (const char*paramName) {
	return GetDatum(paramName, RPCNULLDatumID)->GetTypeID();
}

//=============================================================================
bool RPCRequest::GetBool (uint32 position) {
	return GetDatum(position, RPCBoolDatumID)->GetBool();
}

//=============================================================================
bool RPCRequest::GetBool (const char* paramName) {
	return GetDatum(paramName, RPCBoolDatumID)->GetBool();
}
	
//=============================================================================
int16 RPCRequest::GetInt16 (uint32 position) {
	return GetDatum(position, RPCInt16DatumID)->GetInt16();
}

//=============================================================================
int16 RPCRequest::GetInt16 (const char* paramName) {
	return GetDatum(paramName, RPCInt16DatumID)->GetInt16();
}

//=============================================================================
uint16 RPCRequest::GetUInt16 (uint32 position) {
	return GetDatum(position, RPCUInt16DatumID)->GetUInt16();
}

//=============================================================================
uint16 RPCRequest::GetUInt16 (const char* paramName) {
	return GetDatum(paramName, RPCUInt16DatumID)->GetUInt16();
}

//=============================================================================
int32 RPCRequest::GetInt32 (uint32 position) {
	return GetDatum(position, RPCInt32DatumID)->GetInt32();
}

//=============================================================================
int32 RPCRequest::GetInt32 (const char* paramName) {
	return GetDatum(paramName, RPCInt32DatumID)->GetInt32();
}

//=============================================================================
uint32 RPCRequest::GetUInt32 (uint32 position) {
	return GetDatum(position, RPCUInt32DatumID)->GetUInt32();
}

//=============================================================================
uint32 RPCRequest::GetUInt32 (const char* paramName) {
	return GetDatum(paramName, RPCUInt32DatumID)->GetUInt32();
}

//=============================================================================
int64 RPCRequest::GetInt64 (uint32 position) {
	return GetDatum(position, RPCInt64DatumID)->GetInt64();
}

//=============================================================================
int64 RPCRequest::GetInt64 (const char* paramName) {
	return GetDatum(paramName, RPCInt64DatumID)->GetInt64();
}

//=============================================================================
uint64 RPCRequest::GetUInt64 (uint32 position) {
	return GetDatum(position, RPCUInt64DatumID)->GetUInt64();
}

//=============================================================================
uint64 RPCRequest::GetUInt64 (const char* paramName) {
	return GetDatum(paramName, RPCUInt64DatumID)->GetUInt64();
}

//=============================================================================
float RPCRequest::GetFloat (uint32 position) {
	return GetDatum(position, RPCFloatDatumID)->GetFloat();
}

//=============================================================================
float RPCRequest::GetFloat (const char* paramName) {
	return GetDatum(paramName, RPCFloatDatumID)->GetFloat();
}

//=============================================================================
double RPCRequest::GetDouble (uint32 position) {
	return GetDatum(position, RPCDoubleDatumID)->GetDouble();
}

//=============================================================================
double RPCRequest::GetDouble (const char* paramName) {
	return GetDatum(paramName, RPCDoubleDatumID)->GetDouble();
}

//=============================================================================
const char* RPCRequest::GetString (uint32 position) {
	return GetDatum(position, RPCStringDatumID)->GetString();
}

//=============================================================================
const char* RPCRequest::GetString (const char* paramName) {
	return GetDatum(paramName, RPCStringDatumID)->GetString();
}

//=============================================================================
const char* RPCRequest::GetBuffer (uint32 position) {
	return GetDatum(position, RPCBufferDatumID)->GetBuffer();
}

//=============================================================================
const char* RPCRequest::GetBuffer (const char* paramName) {
	return GetDatum(paramName, RPCBufferDatumID)->GetBuffer();
}

//=============================================================================
uint32 RPCRequest::GetBufferSize (uint32 position) {
	return GetDatum(position, RPCBufferDatumID)->GetBufferSize();
}

//=============================================================================
uint32 RPCRequest::GetBufferSize (const char* paramName) {
	return GetDatum(paramName, RPCBufferDatumID)->GetBufferSize();
}