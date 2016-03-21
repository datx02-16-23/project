package interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import interpreter.wrapper.AnnotatedVariable;
import interpreter.wrapper.Header;
import interpreter.wrapper.Operation;
import interpreter.wrapper.Wrapper;

/**
 * A LogFileManager can read and print logs adhering to <our standard> on a JSON format.
 * @author Richard
 */
public class LogFileManager {	
	/**
	 * Set to true to enable human readable printing of log files. False by default to increase performance.
	 */
	public boolean PRETTY_PRINTING = false;
	
	private static final Gson GSON = new Gson();
	
	private Wrapper wrapper;

	private Map<String, AnnotatedVariable> knownVariables;
	private List<Operation> operations;

	/**
	 * Creates a new LogFileManager. You may read a log file using the readLog() methods.
	 */
	public LogFileManager(){
		knownVariables = new HashMap<String, AnnotatedVariable>();
		operations = new ArrayList<Operation>();
	}
	
	
	/**
	 * Returns the list of operations used by this LogFileManager.
	 * @return The list of operations used by this LogFileManager.
	 */
	public List<Operation> getOperations(){
		return operations;
	}
	
	/**
	 * Set the list of operations used by this LogFileManager.
	 * @param operations A new list of operations to be used by this LogFileManager.
	 */
	public void setOperations (List<Operation> operations){
		this.operations = operations;
	}
	
	/**
	 * Read a log file from the file specified by filePath.
	 * @param filePath Location of the file to read.
	 * @throws JsonIOException This exception is raised when Gson was unable to read an input stream or write to one.
	 * @throws JsonSyntaxException This exception is raised when Gson attempts to read (or write) a malformed JSON element.
	 * @throws FileNotFoundException Signals that the file located at filePath could not be opened.
	 */
	public void readLog(String filePath) throws JsonIOException, JsonSyntaxException, FileNotFoundException{
		readLog(new File(filePath));
	}
	
	/**
	 * 
	 * @param logFile The file to read.
	 * @throws JsonIOException This exception is raised when Gson was unable to read an input stream or write to one.
	 * @throws JsonSyntaxException This exception is raised when Gson attempts to read (or write) a malformed JSON element.
	 * @throws FileNotFoundException Signals that logFile could not be opened.
	 */
	public void readLog(File logFile) throws JsonIOException, JsonSyntaxException, FileNotFoundException{
		wrapper = GSON.fromJson(new JsonReader(new FileReader(logFile)), Wrapper.class);
		unwrap(wrapper);
	}
	
	/**
	 * Print the operations and header information currently held by this LogFileManager.  Set the public variable
	 * PRETTY_PRINTING to true to enable human-readable output.
	 * @param targetPath The location to print the log file.
	 */
	public void printLog(String targetPath){
		Header header = new Header(Header.VERSION_UNKNOWN, (HashMap<String, AnnotatedVariable>) knownVariables);
		printLog(targetPath, new Wrapper(header, operations));
	}
	
	/**
	 * Print the operations and header container in the wrapper given as argument. Set the public variable
	 * PRETTY_PRINTING to true to enable human-readable output.
	 * @param targetPath The location to print the log file.
	 * @param wrapper The wrapper to convert into a log file.
	 */
	public void printLog(String targetPath, Wrapper wrapper){
		Gson GSON;
		DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HHmmss");
		Calendar cal = Calendar.getInstance();
		String fileName = dateFormat.format(cal.getTime()) + ".json";
		if (PRETTY_PRINTING){
			GSON = new GsonBuilder().setPrettyPrinting().create();
		} else {
			GSON = LogFileManager.GSON;
		}
		try (PrintStream out = new PrintStream(new FileOutputStream(targetPath+fileName))) {
		    out.print(GSON.toJson(wrapper));
		    System.out.println("Log printed: " + targetPath + fileName);
		    out.flush();
		    out.close();
		} catch (Exception e) {
			System.out.println("printLog() failed: " + e.getMessage());
		}
	}
	
	/**
	 * Unwrap a wrapper, add contents to knownVariables and operations.
	 * @param wrapper The wrapper to unwrap.
	 */
	private void unwrap(Wrapper wrapper){
		//TODO: Unwrap instead of adding raw.
		if (wrapper.header != null){
			knownVariables.putAll(wrapper.header.annotatedVariables);
		}
		
		if (wrapper.body != null){
			operations.addAll(wrapper.body);
		}
	}
}