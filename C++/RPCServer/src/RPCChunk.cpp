///////////////////////////////////////////////////////////
//
// RPCChunk.cpp: RPC Data Chunks
// Copyright (c) 2015 - Brickyard, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#include <iostream>
#include "RPCServerInterfaces.h"
#include "RPCChunk.h"

#define NULL_POSITION 9999

//#############################################################################
//
//		RPCDatum methods
//
//#############################################################################

RPCDatum::RPCDatum (void) :
	mTypeID(RPCNULLDatumID) { 
}

//-----------------------------------------------------------------------------
void RPCDatum::SetName( const char* data, int32 size ) {
	mName.clear();
	if( size > 0 ) 		
		mName.append( data, size );
}

//#############################################################################
RPCInt32Datum::RPCInt32Datum( const char* data, uint32 datumSize ) {
	if( data == NULL )
		throw "RPCInt32Datum: NULL Data";
	if( datumSize < 12 )
		throw "RPCInt32Datum: Bogus Datum size";

	memcpy( &(value), data+8, 4 );
	SetName( data+12, datumSize - 12 );
	mTypeID= RPCInt32DatumID;
}

//#############################################################################
RPCUInt32Datum::RPCUInt32Datum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCUInt32Datum: NULL Data";
	if( datumSize < 12)
		throw "RPCUInt32Datum: Bogus Datum size";
	
	memcpy( &(value), data+8, 4 );
	SetName( data+12, datumSize - 12 );
	mTypeID= RPCUInt32DatumID;
}

//#############################################################################
RPCInt64Datum::RPCInt64Datum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCInt64Datum: NULL Data";
	if( datumSize < 16)
		throw "RPCInt64Datum: Bogus Datum size";
	
	memcpy( &(value), data+8, 8 );
	SetName( data+16, datumSize - 16 );
	mTypeID= RPCInt64DatumID;
}

//#############################################################################
RPCUInt64Datum::RPCUInt64Datum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCUInt64Datum: NULL Data";
	if( datumSize < 16)
		throw "RPCUInt64Datum: Bogus Datum size";
	
	memcpy( &(value), data+8, 8 );
	SetName( data+16, datumSize - 16 );
	mTypeID= RPCUInt64DatumID;
}

//#############################################################################
RPCInt16Datum::RPCInt16Datum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCInt16Datum: NULL Data";
	if( datumSize < 10)
		throw "RPCInt16Datum: Bogus Datum size";

	memcpy( &(value), data+8, 2 );
	SetName( data+10, datumSize - 10 );
	mTypeID= RPCInt16DatumID;
}

//#############################################################################
RPCUInt16Datum::RPCUInt16Datum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCUInt16Datum: NULL Data";
	if( datumSize < 10)
		throw "RPCUInt16Datum: Bogus Datum size";

	memcpy( &(value), data+8, 2 );
	SetName( data+10, datumSize - 10 );
	mTypeID= RPCUInt16DatumID;
}

//#############################################################################
RPCFloatDatum::RPCFloatDatum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCFloatDatum: NULL Data";
	if( datumSize < 12)
		throw "RPCFloatDatum: Bogus Datum size";

	memcpy( &(value), data+8, 4 );
	SetName( data+12, datumSize - 12 );
	mTypeID= RPCFloatDatumID;
}

//#############################################################################
RPCDoubleDatum::RPCDoubleDatum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCDoubleDatum: NULL Data";
	if( datumSize < 16)
		throw "RPCDoubleDatum: Bogus Datum size";

	memcpy( &(value), data+8, 8 );
	SetName( data+16, datumSize - 16 );
	mTypeID= RPCDoubleDatumID;
}

//#############################################################################
RPCStringDatum::RPCStringDatum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCStringDatum: NULL Data";
	if( datumSize < 9)
		throw "RPCStringDatum: Bogus Datum size";

	value= "";
	mTypeID= RPCStringDatumID;

	if( datumSize > 9 ) {
		unsigned char nameSize;
		memcpy( &nameSize, data+8, 1 );

		SetName( data+9, nameSize );

		uint32 dataSize= datumSize - 9 - nameSize;
		if( dataSize > 0 )
			value.append(data+9+nameSize, dataSize);
	}
}

//#############################################################################
RPCFunctionDatum::RPCFunctionDatum (const char* data, uint32 datumSize) {
	if( data == NULL)
		throw "RPCFunctionDatum: NULL Data";
	if( datumSize < 12)
		throw "RPCFunctionDatum: Bogus Datum size";

	value= "";
	mTypeID= RPCFuncionDatumID;

	if( datumSize > 8 )
		value.append( data+8, datumSize - 8 );
}

//#############################################################################
RPCBoolDatum::RPCBoolDatum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPCBoolDatum: NULL Data";
	if( datumSize < 9)
		throw "RPCBoolDatum: Bogus Datum size";

	memcpy( &(value), data+8, 1 );
	SetName( data+9, datumSize - 9 );
	mTypeID= RPCBoolDatumID;
}

//#############################################################################
RPCBufferDatum::RPCBufferDatum ( const char* data, uint32 datumSize ) {
	if( data == NULL)
		throw "RPbufferDatum: NULL Data";
	if( datumSize < 9)
		throw "RPCBufferDatum: Bogus Datum size";

	value= NULL;
	mTypeID= RPCBufferDatumID;

	unsigned char nameSize;
	memcpy( &nameSize, data+8, 1 );
	SetName( data+9, nameSize );

	valueSize= datumSize - 9 - nameSize;
	if( valueSize > 0 ) {
		value= new char[valueSize]; 
		memcpy( (void*)value, data + (9 + nameSize), valueSize );
	}
}

//#############################################################################
RPCBufferDatum::~RPCBufferDatum () {
	delete [] value;
}
