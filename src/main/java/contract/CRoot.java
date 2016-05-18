package contract;

import java.io.Serializable;
import java.util.List;

import assets.Strings;

/**
 * Wrapper class for GLO and HOG files on the JSON format.
 */
public class CRoot implements Serializable {

	/**
	 * Version number for this class.
	 */
	private static final long serialVersionUID = Strings.VERSION_NUMBER;
	/**
	 * Header data for the file. Contains version number and variable
	 * declarations.
	 */
	public final Header header;
	/**
	 * Operations contained in this file.
	 */
	public final List<Operation> body;

	/**
	 * Create a new Wrapper with the given header and body.
	 * 
	 * @param header
	 *            Header data for the file. Contains version number and variable
	 *            declarations.
	 * @param body
	 *            Operations contained in this file.
	 */
	public CRoot(Header header, List<Operation> body) {
		this.header = header;
		this.body = body;
	}

	public String toString() {
		return "header = " + header + ",\n" + "body = " + body;
	}
}
