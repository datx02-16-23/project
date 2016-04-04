package wrapper;

import java.io.Serializable;

public class StringMessage implements Serializable{
	public final String gsonString;
	public final int senderId;

	public StringMessage(String gsonString, int senderId){
		this.gsonString = gsonString;
		this.senderId = senderId;
	}
}
