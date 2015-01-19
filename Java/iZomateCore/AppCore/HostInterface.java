package iZomateCore.AppCore;

import iZomateCore.AppCore.AppEnums.Insert;
import iZomateCore.AppCore.AppEnums.PluginType;
import iZomateCore.AppCore.AppEnums.TrackFormat;
import iZomateCore.AppCore.AppEnums.TrackType;

public interface HostInterface
{	
	public void _Launch(int sleep, String optionalFile, boolean bRequireNewInstance, boolean bHideWindows) throws Exception;
	
	public void _Quit(int maxQuitDuration, boolean bGuarantee) throws Exception;

	public 	void _createNewTrack(TrackFormat trackFormat, TrackType trackType) throws Exception;
	
	public 	void _createNewTrack(TrackFormat trackFormat) throws Exception;
	
	public void _removeTrack() throws Exception;

	public void _importAudioFile(String audioFile) throws Exception;
	
	public void _togglePlay() throws Exception;
	
	public void _instantiatePlugin(PluginType plugin, Insert insert) throws Exception;
	
	public void _instantiatePlugin(String strPluginName, int insert) throws Exception;

	public void _instantiatePlugin(PluginType plugin, int insert) throws Exception;
	
	public void _instantiatePlugin(String strPlugin, String strCategory) throws Exception;
	
	public void _uninstantiatePlugin(int insertNumber) throws Exception;
	
	public void _uninstantiatePlugin() throws Exception;
	
	public void _hidePluginUI() throws Exception;
	
	public void _showPluginUI() throws Exception;
	
	public boolean _isPlaying() throws Exception;
	
}
