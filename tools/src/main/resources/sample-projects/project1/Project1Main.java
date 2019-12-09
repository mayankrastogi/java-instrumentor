//package sampleprojects.project1;

import com.javainstrumentor.tool.IPC.MessageClient;

public class Project1Main {

    public static final int STATIC_VARIABLE = 0;

    public static void main(String[] args) {
        Project1Main inferredTypeVariable = new Project1Main();
        String staticTypeVariable = "'Test input literal'";

        System.out.println("should print this");
        Project1Class1 obj = staticMethod(STATIC_VARIABLE);

        inferredTypeVariable.voidMethod(obj);
        inferredTypeVariable.stringMethod(Integer.toString(obj.age));
        inferredTypeVariable.voidMethod(obj);

        MessageClient messageClient = new MessageClient("127.0.0.1", 30);
        messageClient.sendMessage("key1-10");
        messageClient.sendMessage("key2-20");
        messageClient.sendMessage("key1-30");
        messageClient.sendMessage("!!-!!");
    }

    static Project1Class1 staticMethod(int inputInt) {
        Project1Class1 obj = new Project1Class1();
        obj.name = "Hard coded name literal";
        obj.age = inputInt > 50 ? inputInt + 23 : inputInt - 23;

        return obj;
    }

    String stringMethod(String input) {
        return input + input;
    }

    void voidMethod(Object input) {
        System.out.println("Print literal");
        System.out.println("Print literal + input: " + input);
    }


}