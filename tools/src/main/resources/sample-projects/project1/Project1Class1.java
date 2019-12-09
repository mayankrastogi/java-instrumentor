//package sampleprojects.project1;

/**
 * Javadoc comment for {@link Project1Class1}.
 */

public class Project1Class1 {
    // Normal comment
    String name;
    int age;

//    float testFloat;

    public String doSomething() {

//        testFloat = 23f;
        if(age < 10) {
            float testFloat = 2f;
            age += 10;
            testFloat = 35f;
            return name + age;
        }
        else {
            age++;
//            testFloat = 1f;
            return "" + age + name;
        }
    }

    @Override
    public String toString() {
        return String.format("Project1Class1(name=%s, age=%d)", this.name, this.age);
    }
}