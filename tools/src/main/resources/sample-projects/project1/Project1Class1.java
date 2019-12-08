package sampleprojects.project1;

/**
 * Javadoc comment for {@link Project1Class1}.
 */
public class Project1Class1 {
    // Normal comment
    String name;
    int age;

    public String doSomething() {
        if(age < 10) {
            age += 10;
            return name + age;
        }
        else {
            age++;
            return "" + age + name;
        }
    }

    @Override
    public String toString() {
        return String.format("Project1Class1(name=%s, age=%d)", this.name, this.age);
    }
}