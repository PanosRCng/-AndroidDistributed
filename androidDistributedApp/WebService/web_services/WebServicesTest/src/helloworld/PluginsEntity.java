package helloworld;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/23/13
 * Time: 4:56 AM
 * To change this template use File | Settings | File Templates.
 */
@Table(name = "plugins", schema = "", catalog = "androidDistributed")
@Entity
public class PluginsEntity {
    private int id;
    private String pluginId;
    private String runtimeFactoryClass;
    private String name;
    private String description;
    private String installUrl;

    @Column(name = "id")
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "plugin_id")
    @Basic
    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Column(name = "runtimeFactoryClass")
    @Basic
    public String getRuntimeFactoryClass() {
        return runtimeFactoryClass;
    }

    public void setRuntimeFactoryClass(String runtimeFactoryClass) {
        this.runtimeFactoryClass = runtimeFactoryClass;
    }

    @Column(name = "name")
    @Basic
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description")
    @Basic
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "installUrl")
    @Basic
    public String getInstallUrl() {
        return installUrl;
    }

    public void setInstallUrl(String installUrl) {
        this.installUrl = installUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginsEntity that = (PluginsEntity) o;

        if (id != that.id) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (installUrl != null ? !installUrl.equals(that.installUrl) : that.installUrl != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (pluginId != null ? !pluginId.equals(that.pluginId) : that.pluginId != null) return false;
        if (runtimeFactoryClass != null ? !runtimeFactoryClass.equals(that.runtimeFactoryClass) : that.runtimeFactoryClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (pluginId != null ? pluginId.hashCode() : 0);
        result = 31 * result + (runtimeFactoryClass != null ? runtimeFactoryClass.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (installUrl != null ? installUrl.hashCode() : 0);
        return result;
    }
}
