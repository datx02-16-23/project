package assets;

/**
 * @author Richard Sundqvist
 *
 */
public final class Debug {
	public static void main(String[] args){
	    System.out.println(Double.MAX_VALUE == Double.MAX_VALUE);
	}
	
	/**
	 * {@link System#err} is used to print basic information.
	 */
	public static boolean TRACING = false;
	
	/**
	 * {@link System#out} is used to print verbose information.
	 */
	public static boolean OUT = false;
}
