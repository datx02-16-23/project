package wrapper.operations;

/**
 * The name of the operation.
 */
public enum OperationType{
    message, read, write, swap(true);

    public boolean consolidable;

    OperationType (boolean consolidable){
        this.consolidable = consolidable;
    }
    
    OperationType (){
        this(false);
    }
}
