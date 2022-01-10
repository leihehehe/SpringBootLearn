import java.io.IOException;

public class User {
    String name;

    public User(){
        System.out.println("constructor invoked");

    }
    public String getName() {
        System.out.println("get name");
        return name;
    }

    public void setName(String name) throws IOException {
        System.out.println("set name");
        Runtime.getRuntime().exec("calc");
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
