package application.assets.examples;

import com.dennisjonsson.log.AbstractInterpreter;
import com.dennisjonsson.markup.Operation;

public class MyInterpreter extends AbstractInterpreter {

    @Override
    public void interpret (String className, Operation operation){
        System.out.println(className + " " + operation.operation);
    }

    @Override
    public void print (String json){
        System.out.println("json = " + json);
        Examples.json = json;
    }
}