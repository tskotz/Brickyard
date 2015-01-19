///////////////////////////////////////////////////////////
//
// RPCRequest.h: RPC outgoing request
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once

#include <vector>

namespace TestAutomation { class RPCRequestInterface; }
class RPCDatum; class RPCFunctionDatum;

class RPCRequest : public TestAutomation::RPCRequestInterface
{
public:
	RPCRequest();
	virtual ~RPCRequest() {}

	RPCDatum *		GetDatum ( uint32 position, uint32 typeID );
	RPCDatum *		GetDatum ( const char* paramName, uint32 typeID );

	virtual uint32					GetCount	();
	virtual const char*				GetName		(uint32 position);
	virtual uint32					GetTypeID	(uint32 position);
	virtual uint32					GetTypeID	(const char*paramName);
	virtual bool					GetBool		(uint32 position);
	virtual bool					GetBool		(const char* paramName);	
	virtual int16					GetInt16	(uint32 position);
	virtual int16					GetInt16	(const char* paramName);
	virtual uint16					GetUInt16	(uint32 position);
	virtual uint16					GetUInt16	(const char* paramName);
	virtual int32					GetInt32	(uint32 position);
	virtual int32					GetInt32	(const char* paramName);
	virtual uint32					GetUInt32	(uint32 position);
	virtual uint32					GetUInt32	(const char* paramName);
	virtual int64					GetInt64	(uint32 position);
	virtual int64					GetInt64	(const char* paramName);
	virtual uint64					GetUInt64	(uint32 position);
	virtual uint64					GetUInt64	(const char* paramName);
	virtual float					GetFloat	(uint32 position);
	virtual float					GetFloat	(const char* paramName);
	virtual double					GetDouble	(uint32 position);
	virtual double					GetDouble	(const char* paramName);
	virtual const char*				GetString	(uint32 position);
	virtual const char*				GetString	(const char* paramName);
	virtual const char*				GetBuffer	(uint32 position);
	virtual const char*				GetBuffer	(const char* paramName);
	virtual uint32					GetBufferSize	(uint32 position);
	virtual uint32					GetBufferSize	(const char* paramName);

	void			SetData(void const * copyData, uint32 copySize);
	const char*		GetData();
	uint32			GetDataSize();
	bool			AddDatum(RPCDatum * datum);
	bool			Parse();
	RPCDatum *		ParseDatum( const char* data, uint32 & offset, uint32 maxSize );
	void			RemoveAll();
	void			Clear();
	const char*		GetFunctionName();
	bool			mMultipleRequests;
	int32			mRequestUID;

private:
	RPCFunctionDatum *		mFunctionDatum;
	uint32					mDataSize;
	const char*				mData;
	uint32					mDataOffset;
	bool					mParsed;
	std::vector<RPCDatum *> mContents;
};