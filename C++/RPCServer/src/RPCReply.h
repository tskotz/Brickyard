///////////////////////////////////////////////////////////
//
// RPCReply.h: RPC incoming reply
// Copyright (c) 2012 - iZotope, Inc.  All Rights Reserved
//
//////////////////////////////////////////////////////////////////////////////
#pragma once

#include <string>

class RPCReply : public TestAutomation::RPCReplyInterface {
public:
	RPCReply();
	virtual ~RPCReply();

			void	SetTransferUID		(int32 uid);
			void	FinishException		(const char * funcName);
			void	SetHeaderDataSize	();
			uint32	OverwriteDatum		(int32 datumTypeID, const char *message);
			uint32  OverwriteDatum		(int32 datumTypeID, const char *message, unsigned char percentage);
			uint32	OverwriteDatum		(int32 datumTypeID, const char *type, const char *message, int32 ival);
			uint32	OverwriteDatum		(int32 datumTypeID, const char *title, const char *uid, const char *status);
			void	Clear				();
			char *	ReplyData			()	{ return mDataBuffer; };
			uint32	ReplySize			()	{ return mDataSize; };
			void	AppendMessage		(const void * value, int32 numBytes);
			void	AppendDatum			(int32 datumTypeID, const char * name, const void * data, uint32 dataSize, bool dynamicData);
			bool	CheckBufferSize		(uint32 newBytes);
			void	SetDialogNotification(char const *title, char const * message, char const * buttons);
			void	SetEventNotification(const char * type, char const * message, int32 ival);
			void	SetStatusNotification (const char * message, unsigned char percentage);
			void	SetWindowNotification(char const *title, char const *uid, char const *status);

	virtual void	AddBool				(bool value,	const char* paramName);
	virtual void	AddInt16			(int16 value,	const char* paramName);
	virtual void	AddUInt16			(uint16 value,	const char* paramName);
	virtual void	AddInt32			(int32 value,	const char* paramName);
	virtual void	AddUInt32			(uint32 value,	const char* paramName);
	virtual void	AddInt64			(int64 value,	const char* paramName);
	virtual void	AddUInt64			(uint64 value,	const char* paramName);
	virtual void	AddFloat			(float value,	const char* paramName);
	virtual void	AddDouble			(double value,	const char* paramName);
	virtual void	AddString			(const char * value, const char* paramName);
	virtual void	AddBuffer			(const char * value, uint32 size, const char* paramName);
	virtual void	SetException		(const char * message);
	virtual bool	IsExceptionSet		();

protected:
	uint32			mDataSize;
	uint32			mBufferSize;
	char *			mDataBuffer;
	bool			mExceptionSet;
	std::string		mExceptionStr;
};