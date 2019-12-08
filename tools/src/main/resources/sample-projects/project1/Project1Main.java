package sampleprojects.project1;

public class Project1Main {

    public static final int STATIC_VARIABLE = 0;

    public static void main(String[] args) {
        var inferredTypeVariable = new Project1Main();
        String staticTypeVariable = "'Test input literal'";

        var obj = staticMethod(STATIC_VARIABLE);

        inferredTypeVariable.voidMethod(obj);
        inferredTypeVariable.stringMethod(obj.age);
        inferredTypeVariable.voidMethod(obj);
    }

    static Project1Class1 staticMethod(int inputInt) {
        Project1Class1 obj = new Project1Main();
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