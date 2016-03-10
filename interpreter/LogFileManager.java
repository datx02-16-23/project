import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import operations.OP_Init;
import operations.OP_Message;
import operations.OP_Read;
import operations.OP_Write;
import wrapper.*;

/**
 * A LogFileManager can read and print logs adhering to <our standard> on a JSON format.
 * @author Richard
 */
public class LogFileManager {	
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
	 * Print the operations and header information currently held by this LogFileManager.
	 * @param targetPath The location to print the log file.
	 */
	public void printLog(String targetPath){
		Header header = new Header(Header.VERSION_UNKNOWN, (HashMap<String, AnnotatedVariable>) knownVariables);
		printLog(targetPath, new Wrapper(header, operations));
	}
	
	/**
	 * Print the operations and header container in the wrapper given as argument.
	 * @param targetPath The location to print the log file.
	 * @param wrapper The wrapper to convert into a log file.
	 */
	public void printLog(String targetPath, Wrapper wrapper){
		try (PrintStream out = new PrintStream(new FileOutputStream(targetPath))) {
		    out.print(GSON.toJson(wrapper));
		    out.flush();
		    out.close();
		} catch (FileNotFoundException e) {
			System.out.println("printLog() failed: " + e.getMessage());
		}
	}
	
	private void unwrap(Wrapper wrapper){
		if (wrapper.header != null){
			knownVariables.putAll(wrapper.header.annotatedVariables);
		}
		
		if (wrapper.body != null){
			for(Operation op : wrapper.body){
				unpackOperation(op); //TODO: Redundant? Could just add to operations as-is.
			}
		}
	}
	
	private void unpackOperation(Operation op){
			switch(op.operation){
				case "read":
					operations.add(unpackRead(op));
					break;
				
				case "write":
					operations.add(unpackWrite(op));
					break;
					
				case "init":
					operations.add(unpackInit(op));
					break;
				
				case "message":
					operations.add(unpackMessage(op));
					break;
				
				default:
					System.out.print("Skipping operation: " + op);
					break;
			}
	}
	
	private OP_Init unpackInit(Operation initOperation){
		OP_Init ans = new OP_Init();
		ans.setTarget((ArrayVariable) initOperation.operationBody.get("target"));
		ans.setValue((String) initOperation.operationBody.get("value"));
		ans.setSize((int[]) initOperation.operationBody.get("size"));
		return ans;
	}
	
	private OP_Read unpackRead(Operation readOperation){
		OP_Read ans = new OP_Read();
		ans.setSource((ArrayVariable) readOperation.operationBody.get("source"));
		ans.setTarget((ArrayVariable) readOperation.operationBody.get("target"));
		ans.setValue((String) readOperation.operationBody.get("value"));
		return ans;
	}
	
	private OP_Write unpackWrite(Operation readOperation){
		OP_Write ans = new OP_Write();
		ans.setSource((ArrayVariable) readOperation.operationBody.get("source"));
		ans.setTarget((ArrayVariable) readOperation.operationBody.get("target"));
		ans.setValue((String) readOperation.operationBody.get("value"));
		return ans;
	}
	
	private OP_Message unpackMessage(Operation op){
		OP_Message ans = new OP_Message();
		ans.setMessage((String) op.operationBody.get("value"));
		return ans;
	}
}


