package assets;

import java.util.Properties;

public abstract class DefaultProperties {

    /**
     * Default config file. Use when original is missing/corrupt or to restore
     * program defaults.
     */
    private static final Properties defaultConfig = new Properties();

    public static final Properties get () {
        defaultConfig.setProperty("playbackStepDelay", "500");
        defaultConfig.setProperty("autoPlayOnIncomingStream", "true");
        defaultConfig.setProperty("firstRun", "true");
        defaultConfig.setProperty("highOrderRoutine", "2");
        return defaultConfig;
    }
}
