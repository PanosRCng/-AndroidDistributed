package helloworld;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/23/13
 * Time: 3:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class PluginList
{
    private ArrayList<MyPlugInfo> plugList;

    public PluginList()
    {
        plugList = new ArrayList<MyPlugInfo>();
    }

    public void setPluginList(ArrayList<MyPlugInfo> plugList)
    {
        this.plugList = plugList;
    }

    public ArrayList<MyPlugInfo> getPluginList()
    {
        return this.plugList;
    }
}
