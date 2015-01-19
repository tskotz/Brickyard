package iZomateCore.AppCore;

import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.RPCServer;

public class PluginInfo {
	private RPCServer 	m_pRPCServer;
	private boolean 	m_bInitialized= false;
	
	public int	 		m_nBuildNumber= -1;
	public int			m_nVersionNumber= -1;
	public float		m_fUIInstantiationTime= (float) -.001;
	public float		m_fDSPInstantiationTime= (float) -.001;
	public String		m_strFullName= null;
	public String		m_strShortName= null;
	public int			m_nCodeModDate= 0;
	public String		m_strCodeBranch= null;

	public PluginInfo( RPCServer pPluginServer ) throws Exception {
		this.m_pRPCServer= pPluginServer;
		this._initialize();
	}
	
	/**
	 * Gets the plugin info from the plugin and store the returned plugin info data
	 * @throws Exception
	 */
	private void _initialize() throws Exception {
		IncomingReply reply= this.m_pRPCServer._createAndProcessRequest( "getPluginInfo" );

		this.m_nCodeModDate= reply._getInt32( "CodeModDate" );
		this.m_strCodeBranch= reply._getString( "CodeBranch" );
		if( reply._exists( "FullName" ) ) {
			this.m_strFullName= reply._getString( "FullName" );
			this.m_strShortName= reply._getString( "ShortName" );
			this.m_nBuildNumber= (int)reply._getUInt32( "BuildNumber" );
			this.m_nVersionNumber= (int)reply._getUInt32( "VersionNumber" );
			this.m_fDSPInstantiationTime= reply._getFloat( "DSP seconds" );
			this.m_fUIInstantiationTime= reply._getFloat( "UI seconds" );
		}
		
		this.m_bInitialized= true;
	}
	
	/**
	 * Returns the DSP Instantiation time in milliseconds
	 * 
	 * @return DSP Instantiation time in milliseconds
	 * @throws Exception
	 */
	public float _GetDSPInstantionTime() throws Exception {
		if( !this.m_bInitialized )
			this._initialize();
		return this.m_fDSPInstantiationTime*1000;
	}
	
	/**
	 * Returns the UI Instantiation time in milliseconds
	 * 
	 * @return UI Instantiation time in milliseconds
	 * @throws Exception
	 */
	public float _GetUIInstantionTime() throws Exception {
		if( !this.m_bInitialized )
			this._initialize();
		return this.m_fUIInstantiationTime*1000;
	}

    public String _GetVersionString(){
        String major = String.valueOf(this.m_nVersionNumber/1000);
        String minor = String.valueOf(this.m_nVersionNumber%1000/100);
        String patch = String.valueOf(this.m_nVersionNumber%10);
        String build = String.valueOf(this.m_nBuildNumber);
        return major + "." + minor + "." + patch + "b" + build;
    }

}
