package helloworld;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/23/13
 * Time: 3:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyPlugInfo
{
    String id;
    String runtimeFactoryClass;
    String name;
    String description;
    String installUrl;

    public MyPlugInfo(String id, String runtimeFactoryClass, String name, String description, String installUrl)
    {
        this.id = id;
        this.runtimeFactoryClass = runtimeFactoryClass;
        this.name = name;
        this.description = description;
        this.installUrl = installUrl;
    }
}
