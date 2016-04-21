/*
	Compile Wrapper with this line
	javac -cp ../../../build/classes/main:jars/gson.jar:jars/jgroups.jar:. Wrapper.java 
*/
import io.LogStreamManager;

/*
	Wrapper
	A class for wrapping LogStreamManager
*/
public class Wrapper {
	
	private LogStreamManager lsm;

	public Wrapper() {
		lsm = new LogStreamManager("Python Annotations");
	}

	public boolean send(String jsonString) {
		return lsm.stream(jsonString);
	}
}