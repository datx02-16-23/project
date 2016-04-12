package assets;

import java.util.Properties;

public abstract class DefaultProperties {
	/**
	 * Default config file. Use when original is missing/corrupt or to restore program defaults.
	 */
	private static final Properties defaultConfig = new Properties();
	private static boolean loaded = false;
	
	public static final Properties get(){
		if(loaded == false){
			defaultConfig.put("playbackStepDelay", 500);
			defaultConfig.put("autoPlayOnIncomingStream", true);
			defaultConfig.put("firstRun", true);
			
			loaded = true;
		}
		
		return defaultConfig;
	}
}
