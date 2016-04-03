package wrapper;

import java.io.Serializable;

public class WrapperMessage implements Serializable{
	public final Wrapper wrapper;
	public final int senderId;

	public WrapperMessage(Wrapper wrapper, int senderId){
		this.wrapper = wrapper;
		this.senderId = senderId;
	}
}
