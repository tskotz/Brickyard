/******************************************************************
RPCManager.cpp 

2/13/2012 Terry Skotz
******************************************************************/

#include "iZBase/common/common.h"
#include "RPCManager.h"
#include "iZBase/Util/Module.h"
#include "../../RPCServer/src/RPCServerInterfaces.h"

using namespace TestAutomation;

static scoped_ptr<Util::Module>			g_pAutoRPCModule;
static shared_ptr<RPCServerInterface>	g_pRPCServer;
#define MIN_RPCSERVER_VERSION	0001

/*--------------------------------------------------------------------------*\
 * Initializes the global RPCManager											
\*--------------------------------------------------------------------------*/
RPCManager::RPCManager()
{
}

RPCManager::~RPCManager()
{
}

void RPCManager::Start(const char *serverID, int32 port)
{
	// Try to load the hook
#ifdef _DEBUG
	const std::string fpath = "iZAutoRPCD.dll";
#else
	const std::string fpath = "iZAutoRPC.dll";
#endif

	g_pAutoRPCModule.reset( new Util::Module(fpath) );
	if( g_pAutoRPCModule->Load() )  {
		// Grab hook function pointers
		FnRPCServer_Version fnVersion= g_pAutoRPCModule->GetFunction<FnRPCServer_Version>("RPCServer_Version");
		if (fnVersion) {
			int32 version = fnVersion();
			if (version < MIN_RPCSERVER_VERSION)
				printf("Unsupported iZotmateServer DLL version: $d.  Requires %d or higher\n", version, MIN_RPCSERVER_VERSION);
			else {
				FnRPCServer_Create  fnCreate = g_pAutoRPCModule->GetFunction<FnRPCServer_Create>("RPCServer_Create");
				FnRPCServer_Destroy fnDestroy = g_pAutoRPCModule->GetFunction<FnRPCServer_Destroy>("RPCServer_Destroy");

				if (fnCreate && fnDestroy) {
					g_pRPCServer.reset(fnCreate(), fnDestroy);

					if (g_pRPCServer.get()) {
						RPCManager::SendDialog();
						g_pRPCServer.get()->Start(serverID, port);
						RegisterHooks();
					}
				}
			}
		}
	}
	else
		printf("iZotmateServer DLL not found\n");

	//unload the module if something went wrong
	if (!g_pRPCServer.get())
		g_pAutoRPCModule.reset();
}

void RPCManager::Stop()
{
	g_pRPCServer.reset();
}

void RPCManager::RequestCheck()
{
	if (g_pRPCServer.get())
		g_pRPCServer.get()->ProcessSocket();		
}

void RPCManager::SendEvent(const char * type, const char * message, int32 ival)
{
	if (g_pRPCServer.get())
		g_pRPCServer.get()->EventNotification(type, message, ival);
}

void RPCManager::SendWindow()
{
	if (g_pRPCServer.get())
		printf("RPCServer dll version: %d\n", g_pRPCServer.get()->Version());
}

void RPCManager::SendDialog()
{
	if (g_pRPCServer.get())
		g_pRPCServer.get()->DialogNotification("Title", "Message", "Buttons");
}

void RPCManager::SendStat()
{
	if (g_pRPCServer.get())
		printf("RPCServer dll version: %d\n", g_pRPCServer.get()->Version());
}

void RPCManager::TestHook(RPCRequestInterface* req, RPCServerInterface* server, RPCReplyInterface* reply)
{
	const char * name = NULL;
	uint32 id = 0;
	bool b;
	float f;
	double d;
	int16 i16;
	uint16 ui16;
	int32 i32;
	uint32 ui32;
	int64 i64;
	uint64 ui64;
	const char * str;
	const char * buff = NULL;
	int32 x = req->GetCount();

	for (int i = 0; i < x; ++i)
	{
		name = req->GetName(i);
		id = req->GetTypeID(i);
		if (id != req->GetTypeID(name))
			reply->AddString("GetTypeID returned wrong value", "TypeIdError");

		if (id == RPCBoolDatumID)
		{
			b = req->GetBool(name);
			if (b != req->GetBool(i))
				reply->AddString("Bool mismatch error", "boolError");
			reply->AddBool(b, name);
		}
		else if (id == RPCBufferDatumID)
		{
			buff = req->GetBuffer(name);
			size_t size = req->GetBufferSize(name);
			if (strcmp(buff, req->GetBuffer(i)) != 0 )
				reply->AddString("Buffer mismatch error", "buffError");
			reply->AddBuffer(buff, size, name);
		}
		else if (id == RPCFloatDatumID)
		{
			f = req->GetFloat(name);
			if (f != req->GetFloat(i))
				reply->AddString("float mismatch error", "flotError");
			reply->AddFloat(f, name);
		}
		else if (id == RPCDoubleDatumID)
		{
			d = req->GetDouble(name);
			if (d != req->GetDouble(i))
				reply->AddString("double mismatch error", "dbleError");
			reply->AddDouble(d, name);
		}
		else if (id == RPCInt16DatumID)
		{
			i16 = req->GetInt16(name);
			if (i16 != req->GetInt16(i))
				reply->AddString("int16 mismatch error", "in16Error");
			reply->AddInt16(i16, name);
		}
		else if (id == RPCUInt16DatumID)
		{
			ui16 = req->GetUInt16(name);
			if (ui16 != req->GetUInt16(i))
				reply->AddString("uint16 mismatch error", "ui16Error");
			reply->AddUInt16(ui16, name);
		}
		else if (id == RPCInt32DatumID)
		{
			i32 = req->GetInt32(name);
			if (i32 != req->GetInt32(i))
				reply->AddString("int32 mismatch error", "in32Error");
			reply->AddInt32(i32, name);
		}
		else if (id == RPCUInt32DatumID)
		{
			ui32 = req->GetUInt32(name);
			if (ui32 != req->GetUInt32(i))
				reply->AddString("uint32 mismatch error", "ui32Error");
			reply->AddUInt32(ui32, name);
		}
		else if (id == RPCInt64DatumID)
		{
			i64 = req->GetInt64(name);
			if (i64 != req->GetInt64(i))
				reply->AddString("int64 mismatch error", "in64Error");
			reply->AddInt64(i64, name);
		}
		else if (id == RPCUInt64DatumID)
		{
			ui64 = req->GetUInt64(name);
			if (ui64 != req->GetUInt64(i))
				reply->AddString("uint64 mismatch error", "ui64Error");
			reply->AddUInt64(ui64, name);
		}
		else if (id == RPCStringDatumID)
		{
			str = req->GetString(name);
			if (strcmp(str, req->GetString(i)) != 0)
				reply->AddString("String mismatch error", "striError");
			reply->AddString(str, name);
		}
	}
}

void RPCManager::RegisterHooks()
{
	if (!g_pRPCServer.get())
		return;

	g_pRPCServer.get()->AddMethod("TestHook", RPCManager::TestHook);
}
