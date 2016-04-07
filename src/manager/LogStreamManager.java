package manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import assets.Strings;
import manager.datastructures.DataStructure;
import manager.datastructures.DataStructureParser;
import manager.operations.OperationParser;
import wrapper.AnnotatedVariable;
import wrapper.Header;
import wrapper.Operation;
import wrapper.Wrapper;

/**
 * A LogStreamManager can read and print logs adhering to <our standard> on a JSON format.
 * @author Richard
 */
public class LogStreamManager implements CommunicatorListener {	
	/**
	 * Set to true to enable human readable printing of log files. False by default to increase performance.
	 */
	public boolean PRETTY_PRINTING;
	
	private final Gson gson = new Gson();
	private final Communicator communicator;
	private CommunicatorListener listener;
	
	private Wrapper wrapper;

	private Map<String, DataStructure> knownVariables;
	private List<Operation> operations;

	/**
	 * Creates a new LogStreamManager. You may read a log file using the readLog() methods.
	 */
	public LogStreamManager(){
		communicator = new JGroupCommunicator(this);
		restoreDefaultState();
	}
	
	/**
	 * Restores this LogStreamManager to its initial state.
	 */
	public void restoreDefaultState(){
		wrapper = null;
		PRETTY_PRINTING = false;
		knownVariables = new HashMap<String, DataStructure>();
		operations = new ArrayList<Operation>();
	}
	
	/**
	 * Returns the map of known variables held by this LogStreamManager.
	 * @return The list of known variables used by this LogStreamManager.
	 */
	public Map<String, DataStructure> getKnownVariables() {
		return knownVariables;
	}

	/**
	 * Set the map of known variables used by this LogStreamManager.
	 * @param knownVariables A new map of known variables to be used by this LogStreamManager.
	 */
	public void setKnownVariables(Map<String, DataStructure> knownVariables) {
		this.knownVariables = knownVariables;
	}
	
	/**
	 * Returns the list of operations used by this LogStreamManager.
	 * @return The list of operations used by this LogStreamManager.
	 */
	public List<Operation> getOperations(){
		return operations;
	}
	
	/**
	 * Set the list of operations used by this LogStreamManager.
	 * @param operations A new list of operations to be used by this LogStreamManager.
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
	public void readLog(String filePath){
		readLog(new File(filePath));
	}
	
	/**
	 * Read, unwrap and store data from a JSON log file.
	 * @param LogStream The file to read.
	 */
	public void readLog(File LogStream){
		try {
			wrapper = gson.fromJson(new JsonReader(new FileReader(LogStream)), Wrapper.class);
			unwrap(wrapper);
		} catch (JsonIOException e) {
			System.err.println("JSON IO error: " + e);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON syntax error: " + e);
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e);
		}
	}
	
	/**
	 * Returns the Communicator used by this LogStreamManager.
	 * @return The Communicator used by this LogStreamManager.
	 */
	public Communicator getCommunicator(){
		return communicator;
	}
	
	/**
	 * Print the operations and header information currently held by this LogStreamManager.  Set the public variable
	 * PRETTY_PRINTING to true to enable human-readable output.
	 * @param targetPath The location to print the log file.
	 */
	public void printLog(File targetPath){
		printLog(targetPath.toString());
	}
	
	/**
	 * Print the operations and header information currently held by this LogStreamManager.  Set the public variable
	 * PRETTY_PRINTING to true to enable human-readable output.
	 * @param targetPath The location to print the log file.
	 */
	public void printLog(String targetPath){
		HashMap<String, AnnotatedVariable> annotatedVariables = new HashMap<String, AnnotatedVariable>();
		annotatedVariables.putAll(knownVariables);
		Header header = new Header(Header.VERSION_UNKNOWN, annotatedVariables);
		
		printLog(targetPath, new Wrapper(header, operations));
	}
	
	/**
	 * Stream the data held by this LogStreamManager using the current Communicator.
	 * @return True if data was successfully streamed.
	 */
	public boolean streamLogData(){
		HashMap<String, AnnotatedVariable> annotatedVariables = new HashMap<String, AnnotatedVariable>();
		annotatedVariables.putAll(knownVariables);
		Header header = new Header(Header.VERSION_UNKNOWN, annotatedVariables);
		return streamWrapper(new Wrapper(header, operations));
	}
	
	/**
	 * Stream the data held by this LogStreamManager using the current Communicator, then clear data.
	 * @return True if data was successfully streamed.
	 */
	public boolean streamAndClearLogData(){
		boolean result = streamLogData();
		operations.clear();
		knownVariables.clear();
		return result;
	}
	
	/**
	 * Stream the given Wrapper using the Communicator carried by this LogStreamManager.
	 * @param wrapper The Wrapper to stream.
	 * @return True if successful, false otherwise.
	 */
	public boolean streamWrapper(Wrapper wrapper){
		return communicator.send(wrapper);
	}
	
	/**
	 * Stream the given Wrapper using the Communicator carried by this LogStreamManager.
	 * @param operation The Operation to stream.
	 * @return True if successful, false otherwise.
	 */
	public boolean streamOperation(Operation operation){
		ArrayList<Operation> operations = new ArrayList<Operation>();
		operations.add(operation);
		return streamWrapper(new Wrapper(null, operations));	
	}
	
	/**
	 * Stream the given Wrapper using the Communicator carried by this LogStreamManager.
	 * @param operation The operations to stream.
	 * @return True if successful, false otherwise.
	 */
	public boolean streamOperations(List<Operation> operations){
		return streamWrapper(new Wrapper(null, operations));	
	}
	
	/**
	 * Stream the given Wrapper using the Communicator carried by this LogStreamManager.
	 * @param annotatedVariable The Wrapper to stream.
	 * @return True if successful, false otherwise.
	 */
	public boolean streamAnnotatedVariable(AnnotatedVariable annotatedVariable){
		HashMap<String, AnnotatedVariable> annotatedVariables = new HashMap<String, AnnotatedVariable>();
		annotatedVariables.put(annotatedVariable.identifier, annotatedVariable);
		Header header = new Header(Header.VERSION_UNKNOWN, annotatedVariables);
		return streamWrapper(new Wrapper(header, null));
	}
	
	/**
	 * Stream the given Wrapper using the Communicator carried by this LogStreamManager.
	 * @param wrappers The Wrappers to stream.
	 * @return True if ALL wrappers successfully sent, false otherwise.
	 */
	public boolean streamWrappers(List<Wrapper> wrappers){
		boolean allSuccessful = true;
		for (Wrapper w : wrappers){
			allSuccessful = allSuccessful && communicator.send(w);
		}
		return allSuccessful;
	}
	
	public void printSimpleLog(String targetPath){
		HashMap<String, AnnotatedVariable> annotatedVariables = new HashMap<String, AnnotatedVariable>();
		annotatedVariables.putAll(knownVariables);
		Header header = new Header(Header.VERSION_UNKNOWN, annotatedVariables);
		
		printSimpleLog(targetPath+"simple.log", new Wrapper(header, operations));
	}
	
	public void printSimpleLog(String targetPath, Wrapper wrapper){
		StringBuilder sb = new StringBuilder();
		
		sb.append("This is a simplified version of the log. It sacrifices completeness for readability and cannot be processed by " + Strings.PROJECT_NAME + ".\n\n");
		Collection<AnnotatedVariable> c = wrapper.header.annotatedVariables.values();
		sb.append("Header: " + c.size() + " declared variables.\n");
		int i = 0;
		for (AnnotatedVariable av : c){
			i++;
			sb.append("\t" + i + ":\t" + av.identifier + " (" + av.rawType + ")\n");
		}
		sb.append("\nBody: " + wrapper.body.size() + " operations.\n");
		
		i = 0;
		for (Operation op : wrapper.body){
			i++;
			sb.append("\t" + i + ":\t\t" + op + "\n");
		}
		
		
		pringString(targetPath, sb.toString());
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
		String fileName = File.separator + dateFormat.format(cal.getTime()) + ".json";
		if (PRETTY_PRINTING){
			GSON = new GsonBuilder().setPrettyPrinting().create();
		} else {
			GSON = this.gson;
		}

		pringString(targetPath+fileName, GSON.toJson(wrapper));

	}
	
	private void pringString(String completePath, String str){
		try {
			PrintStream out = new PrintStream(new FileOutputStream(completePath));
		    System.out.println("Log printed: " + completePath);
		    out.print(str);
		    out.flush();
		    out.close();
		} catch (Exception e) {
			System.err.println("Printing failed: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Unwrap a wrapper, add contents to knownVariables and operations.
	 * @param wrapper The wrapper to unwrap.
	 */
	private void unwrap(Wrapper wrapper){
		if (wrapper.header != null && wrapper.header.annotatedVariables != null){
			Collection<AnnotatedVariable> avList = wrapper.header.annotatedVariables.values();
			for (AnnotatedVariable av : avList){
				knownVariables.put(av.identifier, DataStructureParser.unpackAnnotatedVariable(av));
			}
		}
		if (wrapper.body != null){
			for (Operation op : wrapper.body){
				operations.add(OperationParser.unpackOperation(op));
			}
		}
	}

	@Override
	public void messageReceived() {
	
		
		List<Wrapper> wrappers = communicator.getAllQueuedMessages();
		for(Wrapper w : wrappers){
			unwrap(w);
		}
		if (listener != null){
			listener.messageReceived();
		}
	}
	
	/**
	 * Clear list of operations and known variables.
	 * Equivalent to calling clearOperations() and clearKnownVariables().
	 */
	public void clearData(){
		knownVariables.clear();
		operations.clear();
	}
	
	/**
	 * Clear all operations held by this LogStreamManager.
	 */
	public void clearOperations(){
		operations.clear();
	}
	
	/**
	 * Clear all known variables held by this LogStreamManager.
	 */
	public void clearKnownVariables(){
		knownVariables.clear();
	}
	
	/**
	 * Set the CommunicatorListener which will be notified when this Communicator accepts a message.
	 * @param newListener The new CommunicatorListener.
	 */
	public void setListener(CommunicatorListener newListener){
		listener = newListener;
	}
}