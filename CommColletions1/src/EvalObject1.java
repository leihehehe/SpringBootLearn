import org.apache.commons.collections.functors.InvokerTransformer;

public class EvalObject1 {
    public static void main(String[] args) {
        InvokerTransformer transformer = new InvokerTransformer("exec", new Class[]{String.class}, new String[]{"calc"});
        transformer.transform(Runtime.getRuntime());
    }

}
