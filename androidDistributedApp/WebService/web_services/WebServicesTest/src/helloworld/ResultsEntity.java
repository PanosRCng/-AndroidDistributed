package helloworld;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: panos
 * Date: 6/21/13
 * Time: 1:32 AM
 * To change this template use File | Settings | File Templates.
 */
@Table(name = "results", schema = "", catalog = "androidDistributed")
@Entity
public class ResultsEntity {
    private int id;
    private int experimentId;
    private int sourceId;
private String value;

    @Column(name = "id")
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "experiment_id")
    @Basic
    public int getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(int experimentId) {
        this.experimentId = experimentId;
    }

        @Column(name = "source_id")
        @Basic
    public int getSourceId() {
        return sourceId;
    }

public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    @Column(name = "value")
    @Basic
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultsEntity that = (ResultsEntity) o;

        if (experimentId != that.experimentId) return false;
        if (id != that.id) return false;
        if (sourceId != that.sourceId) return false;
        if (!(value != null ? !value.equals(that.value) : that.value != null)) return true;
        else return false;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + experimentId;
        result = 31 * result + sourceId;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
