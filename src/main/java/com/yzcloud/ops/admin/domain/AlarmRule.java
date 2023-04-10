package com.yzcloud.ops.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A AlarmRule.
 */
@Entity
@Table(name = "alarm_rule")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "alarmrule")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlarmRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "alarm_type")
    private String alarmType;

    @Column(name = "conf")
    private String conf;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_time")
    private ZonedDateTime modifiedTime;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JsonIgnoreProperties(value = { "dataSource", "category", "alarmRules" }, allowSetters = true)
    private EventRule eventRule;

    @ManyToOne
    private AlarmLevel alarmLevel;

    @ManyToOne
    private AlarmContact alarmContact;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AlarmRule id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public AlarmRule name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public AlarmRule description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlarmType() {
        return this.alarmType;
    }

    public AlarmRule alarmType(String alarmType) {
        this.setAlarmType(alarmType);
        return this;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getConf() {
        return this.conf;
    }

    public AlarmRule conf(String conf) {
        this.setConf(conf);
        return this;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public AlarmRule createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getCreateTime() {
        return this.createTime;
    }

    public AlarmRule createTime(ZonedDateTime createTime) {
        this.setCreateTime(createTime);
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public AlarmRule modifiedBy(String modifiedBy) {
        this.setModifiedBy(modifiedBy);
        return this;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public ZonedDateTime getModifiedTime() {
        return this.modifiedTime;
    }

    public AlarmRule modifiedTime(ZonedDateTime modifiedTime) {
        this.setModifiedTime(modifiedTime);
        return this;
    }

    public void setModifiedTime(ZonedDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getStatus() {
        return this.status;
    }

    public AlarmRule status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EventRule getEventRule() {
        return this.eventRule;
    }

    public void setEventRule(EventRule eventRule) {
        this.eventRule = eventRule;
    }

    public AlarmRule eventRule(EventRule eventRule) {
        this.setEventRule(eventRule);
        return this;
    }

    public AlarmLevel getAlarmLevel() {
        return this.alarmLevel;
    }

    public void setAlarmLevel(AlarmLevel alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public AlarmRule alarmLevel(AlarmLevel alarmLevel) {
        this.setAlarmLevel(alarmLevel);
        return this;
    }

    public AlarmContact getAlarmContact() {
        return this.alarmContact;
    }

    public void setAlarmContact(AlarmContact alarmContact) {
        this.alarmContact = alarmContact;
    }

    public AlarmRule alarmContact(AlarmContact alarmContact) {
        this.setAlarmContact(alarmContact);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlarmRule)) {
            return false;
        }
        return id != null && id.equals(((AlarmRule) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlarmRule{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", alarmType='" + getAlarmType() + "'" +
            ", conf='" + getConf() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", modifiedBy='" + getModifiedBy() + "'" +
            ", modifiedTime='" + getModifiedTime() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
