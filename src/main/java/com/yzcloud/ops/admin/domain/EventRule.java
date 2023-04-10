package com.yzcloud.ops.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A EventRule.
 */
@Entity
@Table(name = "event_rule")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "eventrule")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "alias")
    private String alias;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "description")
    private String description;

    @Column(name = "event_sample")
    private String eventSample;

    @Column(name = "event_rule")
    private String eventRule;

    @Column(name = "created_time")
    private ZonedDateTime createdTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_time")
    private ZonedDateTime modifiedTime;

    @Column(name = "modified_by")
    private String modifiedBy;

    @OneToOne
    @JoinColumn(unique = true)
    private DataSource dataSource;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "eventRule")
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "eventRule", "alarmLevel", "alarmContact" }, allowSetters = true)
    private Set<AlarmRule> alarmRules = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public EventRule id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public EventRule name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return this.alias;
    }

    public EventRule alias(String alias) {
        this.setAlias(alias);
        return this;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEventType() {
        return this.eventType;
    }

    public EventRule eventType(String eventType) {
        this.setEventType(eventType);
        return this;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return this.description;
    }

    public EventRule description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventSample() {
        return this.eventSample;
    }

    public EventRule eventSample(String eventSample) {
        this.setEventSample(eventSample);
        return this;
    }

    public void setEventSample(String eventSample) {
        this.eventSample = eventSample;
    }

    public String getEventRule() {
        return this.eventRule;
    }

    public EventRule eventRule(String eventRule) {
        this.setEventRule(eventRule);
        return this;
    }

    public void setEventRule(String eventRule) {
        this.eventRule = eventRule;
    }

    public ZonedDateTime getCreatedTime() {
        return this.createdTime;
    }

    public EventRule createdTime(ZonedDateTime createdTime) {
        this.setCreatedTime(createdTime);
        return this;
    }

    public void setCreatedTime(ZonedDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public EventRule createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getModifiedTime() {
        return this.modifiedTime;
    }

    public EventRule modifiedTime(ZonedDateTime modifiedTime) {
        this.setModifiedTime(modifiedTime);
        return this;
    }

    public void setModifiedTime(ZonedDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public EventRule modifiedBy(String modifiedBy) {
        this.setModifiedBy(modifiedBy);
        return this;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EventRule dataSource(DataSource dataSource) {
        this.setDataSource(dataSource);
        return this;
    }

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public EventRule category(Category category) {
        this.setCategory(category);
        return this;
    }

    public Set<AlarmRule> getAlarmRules() {
        return this.alarmRules;
    }

    public void setAlarmRules(Set<AlarmRule> alarmRules) {
        if (this.alarmRules != null) {
            this.alarmRules.forEach(i -> i.setEventRule(null));
        }
        if (alarmRules != null) {
            alarmRules.forEach(i -> i.setEventRule(this));
        }
        this.alarmRules = alarmRules;
    }

    public EventRule alarmRules(Set<AlarmRule> alarmRules) {
        this.setAlarmRules(alarmRules);
        return this;
    }

    public EventRule addAlarmRule(AlarmRule alarmRule) {
        this.alarmRules.add(alarmRule);
        alarmRule.setEventRule(this);
        return this;
    }

    public EventRule removeAlarmRule(AlarmRule alarmRule) {
        this.alarmRules.remove(alarmRule);
        alarmRule.setEventRule(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventRule)) {
            return false;
        }
        return id != null && id.equals(((EventRule) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventRule{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", alias='" + getAlias() + "'" +
            ", eventType='" + getEventType() + "'" +
            ", description='" + getDescription() + "'" +
            ", eventSample='" + getEventSample() + "'" +
            ", eventRule='" + getEventRule() + "'" +
            ", createdTime='" + getCreatedTime() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", modifiedTime='" + getModifiedTime() + "'" +
            ", modifiedBy='" + getModifiedBy() + "'" +
            "}";
    }
}
