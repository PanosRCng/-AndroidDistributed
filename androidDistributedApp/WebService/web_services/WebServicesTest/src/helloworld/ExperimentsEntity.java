package helloworld;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/12/13
 * Time: 2:42 AM
 * To change this template use File | Settings | File Templates.
 */
@Table(name = "experiments", schema = "", catalog = "androidDistributed")
@Entity
public class ExperimentsEntity {
    private int id;
    private String contextType;
    private String name;
    private String sensorDependencies;
    private String timeDependencies;
    private String expires;
    private String status;
    private String url;
private String resultsUrl;
    private String userEmail;
    private int executedBy;

    @Column(name = "id")
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "contextType")
    @Basic
    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    @Column(name = "name")
    @Basic
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "sensorDependencies")
    @Basic
    public String getSensorDependencies() {
        return sensorDependencies;
    }

    public void setSensorDependencies(String sensorDependencies) {
        this.sensorDependencies = sensorDependencies;
    }

    @Column(name = "timeDependencies")
    @Basic
    public String getTimeDependencies() {
        return timeDependencies;
    }

    public void setTimeDependencies(String timeDependencies) {
        this.timeDependencies = timeDependencies;
    }

    @Column(name = "expires")
    @Basic
    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

 //   public void setUrl(String url) {
  //      this.url = url;
 //   }

    @Column(name = "status")
    @Basic
    public String getStatus() {
        return status;
    }

        public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "url")
    @Basic
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentsEntity that = (ExperimentsEntity) o;

        if (id != that.id) return false;
        if (contextType != null ? !contextType.equals(that.contextType) : that.contextType != null) return false;
        if (expires != null ? !expires.equals(that.expires) : that.expires != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (resultsUrl != null ? !resultsUrl.equals(that.resultsUrl) : that.resultsUrl != null) return false;
        if (sensorDependencies != null ? !sensorDependencies.equals(that.sensorDependencies) : that.sensorDependencies != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (timeDependencies != null ? !timeDependencies.equals(that.timeDependencies) : that.timeDependencies != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (contextType != null ? contextType.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (sensorDependencies != null ? sensorDependencies.hashCode() : 0);
        result = 31 * result + (timeDependencies != null ? timeDependencies.hashCode() : 0);
        result = 31 * result + (expires != null ? expires.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (resultsUrl != null ? resultsUrl.hashCode() : 0);
        return result;
    }

    @Column(name = "user_email")
    @Basic
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Column(name = "executedBy")
    @Basic
    public int getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(int executedBy) {
        this.executedBy = executedBy;
    }
}
