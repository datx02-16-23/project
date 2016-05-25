package assets;

/**
 * Constant container class.
 *
 * @author Richard Sundqvist
 *
 */
public abstract class Const {
    private Const () {
    } // Not to be instantiated.

    public static final long       VERSION_NUMBER               = Long.MAX_VALUE;

    public static final String     PROJECT_NAME                 = "Lorem Ipsum";
    public static final String     PROJECT_SLOGAN               = "Abstract Visualization of Programs";
    public static final String     PROGRAM_NAME                 = PROJECT_NAME + " - JavaFX Desktop Visualization";
    public static final String     PROPERTIES_FILE_NAME         = "config.properties";
    public static final String     DEFAULT_CHANNEL              = "mavster_stream";
    // Credits
    public static final String[]   DEVELOPER_NAMES              = {
    //@formatter:off
                                                                   "Johan GERDIN",
                                                                   "Ivar \"Cannonbait\" JOSEFSSON",
                                                                   "Dennis JONSSON",
                                                                   "Simon SMITH",
                                                                   "Richard \"Whisp\" SUNDQVIST"};
   //@formatter:on
}
