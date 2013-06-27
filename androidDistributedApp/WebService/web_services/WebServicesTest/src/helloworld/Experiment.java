package helloworld;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/18/13
 * Time: 8:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class Experiment
{
    private String contextType;
    private String userEmail;
    private String name;
    private String sensorDependencies;
    private String timeDependencies;
    private String expires;
    private String url;

    public Experiment(String contextType, String userEmail, String name, String sensorDependencies,
                      String timeDependencies, String expires, String url)
    {
        this.contextType = contextType;
        this.userEmail = userEmail;
        this.name = name;
        this.sensorDependencies = sensorDependencies;
        this.timeDependencies = timeDependencies;
        this.expires = expires;
        this.url = url;
    }

    public String getContextType()
    {
        return this.contextType;
    }

    public String getUserEmail()
    {
        return this.userEmail;
    }

    public String getName()
    {
        return this.name;
    }

    public String getSensorDependencies()
    {
        return this.sensorDependencies;
    }

    public String getTimeDependencies()
    {
        return this.timeDependencies;
    }

    public String getExpires()
    {
        return this.expires;
    }

    public String getUrl()
    {
        return this.url;
    }
}
