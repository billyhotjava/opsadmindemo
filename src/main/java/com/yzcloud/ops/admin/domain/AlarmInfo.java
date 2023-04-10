package com.yzcloud.ops.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A AlarmInfo.
 */
@Entity
@Table(name = "alarm_info")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "alarminfo")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlarmInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "info")
    private String info;

    @Column(name = "checked")
    private Boolean checked;

    @Column(name = "created_time")
    private ZonedDateTime createdTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_time")
    private ZonedDateTime modifiedTime;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "alert_time")
    private ZonedDateTime alertTime;

    @Column(name = "alert_doc")
    private String alertDoc;

    @Column(name = "es_document_id")
    private String esDocumentId;

    @Column(name = "es_index_name")
    private String esIndexName;

    @ManyToOne
    @JsonIgnoreProperties(value = { "eventRule", "alarmLevel", "alarmContact" }, allowSetters = true)
    private AlarmRule alarmRule;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AlarmInfo id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public AlarmInfo uuid(String uuid) {
        this.setUuid(uuid);
        return this;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public AlarmInfo name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return this.info;
    }

    public AlarmInfo info(String info) {
        this.setInfo(info);
        return this;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getChecked() {
        return this.checked;
    }

    public AlarmInfo checked(Boolean checked) {
        this.setChecked(checked);
        return this;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public ZonedDateTime getCreatedTime() {
        return this.createdTime;
    }

    public AlarmInfo createdTime(ZonedDateTime createdTime) {
        this.setCreatedTime(createdTime);
        return this;
    }

    public void setCreatedTime(ZonedDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public AlarmInfo createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getModifiedTime() {
        return this.modifiedTime;
    }

    public AlarmInfo modifiedTime(ZonedDateTime modifiedTime) {
        this.setModifiedTime(modifiedTime);
        return this;
    }

    public void setModifiedTime(ZonedDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public AlarmInfo modifiedBy(String modifiedBy) {
        this.setModifiedBy(modifiedBy);
        return this;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public ZonedDateTime getAlertTime() {
        return this.alertTime;
    }

    public AlarmInfo alertTime(ZonedDateTime alertTime) {
        this.setAlertTime(alertTime);
        return this;
    }

    public void setAlertTime(ZonedDateTime alertTime) {
        this.alertTime = alertTime;
    }

    public String getAlertDoc() {
        return this.alertDoc;
    }

    public AlarmInfo alertDoc(String alertDoc) {
        this.setAlertDoc(alertDoc);
        return this;
    }

    public void setAlertDoc(String alertDoc) {
        this.alertDoc = alertDoc;
    }

    public String getEsDocumentId() {
        return this.esDocumentId;
    }

    public AlarmInfo esDocumentId(String esDocumentId) {
        this.setEsDocumentId(esDocumentId);
        return this;
    }

    public void setEsDocumentId(String esDocumentId) {
        this.esDocumentId = esDocumentId;
    }

    public String getEsIndexName() {
        return this.esIndexName;
    }

    public AlarmInfo esIndexName(String esIndexName) {
        this.setEsIndexName(esIndexName);
        return this;
    }

    public void setEsIndexName(String esIndexName) {
        this.esIndexName = esIndexName;
    }

    public AlarmRule getAlarmRule() {
        return this.alarmRule;
    }

    public void setAlarmRule(AlarmRule alarmRule) {
        this.alarmRule = alarmRule;
    }

    public AlarmInfo alarmRule(AlarmRule alarmRule) {
        this.setAlarmRule(alarmRule);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlarmInfo)) {
            return false;
        }
        return id != null && id.equals(((AlarmInfo) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlarmInfo{" +
            "id=" + getId() +
            ", uuid='" + getUuid() + "'" +
            ", name='" + getName() + "'" +
            ", info='" + getInfo() + "'" +
            ", checked='" + getChecked() + "'" +
            ", createdTime='" + getCreatedTime() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", modifiedTime='" + getModifiedTime() + "'" +
            ", modifiedBy='" + getModifiedBy() + "'" +
            ", alertTime='" + getAlertTime() + "'" +
            ", alertDoc='" + getAlertDoc() + "'" +
            ", esDocumentId='" + getEsDocumentId() + "'" +
            ", esIndexName='" + getEsIndexName() + "'" +
            "}";
    }
}
