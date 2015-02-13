///////////////////////////////////////////////////////////
//
// RPCChunk.h: RPC Data Chunks
// Copyright (c) 2015 - Brickyard, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once 

#include <vector>

//#############################################################################
//
//		Data Element Classes
//
//#############################################################################
//	abstract base class for a data element

class RPCDatum
{
public:
			RPCDatum ();
	virtual ~RPCDatum() {;}

			void			SetName		(const char* data, int32 size);
			char const*		GetName		() 	{ return this->mName.c_str(); }
			uint32			GetTypeID	()	{ return mTypeID; }

	virtual bool			GetBool		()	{throw "GetBool - Needs override!";}
	virtual int16			GetInt16	()	{throw "GetInt16 - Needs override!";}
	virtual uint16			GetUInt16	()	{throw "GetUInt16 - Needs override!";}
	virtual int32			GetInt32	()	{throw "GetInt32 - Needs override!";}
	virtual uint32			GetUInt32	()	{throw "GetUInt32 - Needs override!";}
	virtual int64			GetInt64	()	{throw "GetInt64 - Needs override!";}
	virtual uint64			GetUInt64	()	{throw "GetUInt64 - Needs override!";}
	virtual float			GetFloat	()	{throw "GetFloat - Needs override!";}
	virtual double			GetDouble	()	{throw "GetDouble - Needs override!";}
	virtual const char*		GetString	()	{throw "GetString - Needs override!";}
	virtual const char*		GetBuffer	()	{throw "GetBuffer - Needs override!";}
	virtual uint32			GetBufferSize()	{throw "GetBufferSize - Needs override!";}

protected:
	std::string		mName;
	uint32			mTypeID;
};

//=============================================================================
//	a bool data element

class RPCBoolDatum : public RPCDatum
{
public:
	RPCBoolDatum ( const char* data, uint32 datumSize );

	bool			GetBool () { return this->value; }

private:
	bool			value;
};

//=============================================================================
//	a bool data element

class RPCBufferDatum : public RPCDatum
{
public:
	RPCBufferDatum ( const char* data, uint32 datumSize );
	~RPCBufferDatum ();

	const char*		GetBuffer () { return this->value; }
	uint32			GetBufferSize () { return this->valueSize; }

private:
	const char*	value;
	uint32			valueSize;
};

//=============================================================================
//	an int16 data element

class RPCInt16Datum : public RPCDatum
{
public:
	RPCInt16Datum ( const char* data, uint32 datumSize );

	int16			GetInt16	() { return this->value; }

private:
	int16			value;
};

//=============================================================================
//	an unsigned int16 data element

class RPCUInt16Datum : public RPCDatum
{
public:
	RPCUInt16Datum ( const char* data, uint32 datumSize );

	uint16			GetUInt16	() { return this->value; }

private:
	uint16	value;
};

//=============================================================================
//	an int32 data element

class RPCInt32Datum : public RPCDatum
{
public:
	RPCInt32Datum ( const char* data, uint32 datumSize );
	
	int32			GetInt32 () { return this->value; }
	
private:
	int32			value;
};

//=============================================================================
//	an unsigned int32 data element

class RPCUInt32Datum : public RPCDatum
{
public:
	RPCUInt32Datum ( const char* data, uint32 datumSize );
	
	uint32			GetUInt32 () { return this->value; }
	
private:
	uint32			value;
};

//=============================================================================
//	an unsigned int64 data element

class RPCInt64Datum : public RPCDatum
{
public:
	RPCInt64Datum ( const char* data, uint32 datumSize );
	
	int64			GetInt64 () { return this->value; }
	
private:
	int64			value;
};

//=============================================================================
//	an unsigned int64 data element

class RPCUInt64Datum : public RPCDatum
{
public:
	RPCUInt64Datum ( const char* data, uint32 datumSize );
	
	uint64			GetUInt64 () { return this->value; }
	
private:
	uint64			value;
};

//=============================================================================
//	an floating point data element

class RPCFloatDatum : public RPCDatum
{
public:
	RPCFloatDatum ( const char* data, uint32 datumSize );

	float			GetFloat	() { return this->value; }

private:
	float			value;
};

//=============================================================================
//	an double data element

class RPCDoubleDatum : public RPCDatum
{
public:
	RPCDoubleDatum ( const char* data, uint32 datumSize );

	double			GetDouble	() { return this->value; }

private:
	double			value;
};

//=============================================================================
//	a character string data element

class RPCStringDatum : public RPCDatum
{
public:
	RPCStringDatum ( const char* data, uint32 datumSize );

	const char*	GetString	()	{ return this->value.c_str(); }
	std::string		GetStdString()	{ return this->value; }

private:
	std::string		value;
};

//=============================================================================
//	a function data element

class RPCFunctionDatum : public RPCDatum
{
public:
	RPCFunctionDatum ( const char* data, uint32 datumSize );

	const char*	GetFunction ()		{ return this->value.c_str(); }

private:
	std::string		value;
};