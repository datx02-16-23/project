import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import operations.Operation;

/**
 * 
 * @author Richard
 * Demo klass för tolken.
 */
public class Main {
	public static void main(String[] args) throws Exception{
		
		File lowOrderGrammarFile;
		if (args.length > 0){
			lowOrderGrammarFile = new File(args[0]);
		} else {
			JFileChooser jfc = new JFileChooser();
			jfc.showOpenDialog(null);
			lowOrderGrammarFile = jfc.getSelectedFile();
			System.out.println(lowOrderGrammarFile);
			
			if (lowOrderGrammarFile == null){
				System.out.println("No file selected.");
				System.exit(0);
			}
		}
		
		//Unpack low level operations
		LogFileManager logFM = new LogFileManager();
		logFM.readLog(lowOrderGrammarFile);
		List<Operation> operations = logFM.getOperations();
		
		//Consolidate into a combination of high and low level operations
		Interpreter interpreter = new Interpreter();
		interpreter.setLowLevelOperations(operations);
		operations = interpreter.getConsolidatedOperations();
		
		//Change to list of consolidated operations, then create a high-order grammar log file
		logFM.setOperations(operations);
		logFM.printLog("C:\\Users\\Richard\\Desktop\\");
	}
}
