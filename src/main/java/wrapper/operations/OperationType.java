package wrapper.operations;

/**
 * The name of the operation.
 */
public enum OperationType{
    message(false), read, write, swap;

    public boolean consolidable;

    OperationType (boolean consolidable){
        this.consolidable = consolidable;
    }
    
    OperationType (){
        this(false);
    }
}
