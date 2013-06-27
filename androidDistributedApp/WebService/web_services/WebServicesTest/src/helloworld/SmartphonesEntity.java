package helloworld;

import javax.persistence.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/25/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "smartphones", schema = "", catalog = "androidDistributed")
@Entity
public class SmartphonesEntity {
    private int id;

    @javax.persistence.Column(name = "id")
    @javax.persistence.Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int phoneId;

    @javax.persistence.Column(name = "phone_id")
    @javax.persistence.Basic
    public int getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(int phoneId) {
        this.phoneId = phoneId;
    }

    private String sensorsRules;

    @javax.persistence.Column(name = "sensors_rules")
    @javax.persistence.Basic
    public String getSensorsRules() {
        return sensorsRules;
    }

    public void setSensorsRules(String sensorsRules) {
        this.sensorsRules = sensorsRules;
    }

    private String timeRules;

    @javax.persistence.Column(name = "time_rules")
    @javax.persistence.Basic
    public String getTimeRules() {
        return timeRules;
    }

    public void setTimeRules(String timeRules) {
        this.timeRules = timeRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartphonesEntity that = (SmartphonesEntity) o;

        if (id != that.id) return false;
        if (phoneId != that.phoneId) return false;
        if (sensorsRules != null ? !sensorsRules.equals(that.sensorsRules) : that.sensorsRules != null) return false;
        if (timeRules != null ? !timeRules.equals(that.timeRules) : that.timeRules != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + phoneId;
        result = 31 * result + (sensorsRules != null ? sensorsRules.hashCode() : 0);
        result = 31 * result + (timeRules != null ? timeRules.hashCode() : 0);
        return result;
    }
}
