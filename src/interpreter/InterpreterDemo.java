package interpreter;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import manager.LogStreamManager;
import manager.datastructures.DataStructure;
import manager.operations.OP_Init;
import wrapper.Operation;

/** 
 * Demo for the Interpreter.
 * @author Richard
 */
public class InterpreterDemo {
	public static void main(String[] args) throws Exception{
	
		LogStreamManager lfm = new LogStreamManager();
		List<Operation> operations = lfm.getOperations();
		Interpreter interpreter = new Interpreter();
		lfm.PRETTY_PRINTING = true;
		
		File lowOrderGrammarFile;
		if (args.length > 0){
			lowOrderGrammarFile = new File(args[0]);
		} else {
			JFileChooser jfc = new JFileChooser();
			jfc.showOpenDialog(null);
			lowOrderGrammarFile = jfc.getSelectedFile();
			System.out.println("Low-Order grammar file: " + lowOrderGrammarFile);
			
			if (lowOrderGrammarFile == null){
				System.out.println("No file selected.");
				System.exit(0);
			}
		}
		lfm.readLog(lowOrderGrammarFile);
		
		//Consolidate into a combination of high and low level operations
		interpreter.setOperations(operations);
		operations = interpreter.getConsolidatedOperations();
		//Change to list of consolidated operations, then create a high-order grammar log file
		lfm.setOperations(operations);
		lfm.streamAndClearLogData();
//		logFM.printLog("C:\\Users\\Richard\\Desktop\\");
		while(true){
			System.out.println(lfm.getOperations());	
			Thread.sleep(1500);
		}
	}
}
