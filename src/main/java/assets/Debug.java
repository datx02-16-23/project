package assets;

/**
 * @author Richard Sundqvist
 *
 */
public final class Debug {
    public static void main (String[] args) {
        System.out.println(Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY);
    }

    /**
     * {@link System#err} printouts etc.
     */
    public static boolean ERR = false;

    /**
     * {@link System#out} printouts etc.
     */
    public static boolean OUT = false;
}
