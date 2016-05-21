package assets;

import java.util.regex.Pattern;

/**
 * Debug level setting.
 * @author Richard Sundqvist
 *
 */
public final class Debug {
	public static void main(String[] args){
//		String str = "package.subpackage:class:subclass:subsubclass;watisthis.OK NOW WE STOP";
		String str = "OK NOW WE STOP";
		str = str.trim();
		Pattern p;
		String a[] = str.split("\\p{Punct}");
		for(int i = 0; i < a.length; i++){
			System.out.println(a[i]);
		}
	}
	
	/**
	 * {@link System#err} is used to print basic debug information.
	 */
	public static final boolean ERR = false;
	/**
	 * {@link System#out} is used to print verbose debug information.
	 */
	public static final boolean OUT = false;
	
	//TODO Add more specific
	

}
