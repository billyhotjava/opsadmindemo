package com.yzcloud.ops.admin.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A AlarmContact.
 */
@Entity
@Table(name = "alarm_contact")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "alarmcontact")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlarmContact implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "name")
    private String name;

    @Column(name = "content")
    private String content;

    @Column(name = "type")
    private ZonedDateTime type;

    @Column(name = "contact_way")
    private String contactWay;

    @Column(name = "created_time")
    private ZonedDateTime createdTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_time")
    private String modifiedTime;

    @Column(name = "modified_by")
    private String modifiedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AlarmContact id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public AlarmContact title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return this.name;
    }

    public AlarmContact name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return this.content;
    }

    public AlarmContact content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ZonedDateTime getType() {
        return this.type;
    }

    public AlarmContact type(ZonedDateTime type) {
        this.setType(type);
        return this;
    }

    public void setType(ZonedDateTime type) {
        this.type = type;
    }

    public String getContactWay() {
        return this.contactWay;
    }

    public AlarmContact contactWay(String contactWay) {
        this.setContactWay(contactWay);
        return this;
    }

    public void setContactWay(String contactWay) {
        this.contactWay = contactWay;
    }

    public ZonedDateTime getCreatedTime() {
        return this.createdTime;
    }

    public AlarmContact createdTime(ZonedDateTime createdTime) {
        this.setCreatedTime(createdTime);
        return this;
    }

    public void setCreatedTime(ZonedDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public AlarmContact createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedTime() {
        return this.modifiedTime;
    }

    public AlarmContact modifiedTime(String modifiedTime) {
        this.setModifiedTime(modifiedTime);
        return this;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public AlarmContact modifiedBy(String modifiedBy) {
        this.setModifiedBy(modifiedBy);
        return this;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlarmContact)) {
            return false;
        }
        return id != null && id.equals(((AlarmContact) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlarmContact{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", name='" + getName() + "'" +
            ", content='" + getContent() + "'" +
            ", type='" + getType() + "'" +
            ", contactWay='" + getContactWay() + "'" +
            ", createdTime='" + getCreatedTime() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", modifiedTime='" + getModifiedTime() + "'" +
            ", modifiedBy='" + getModifiedBy() + "'" +
            "}";
    }
}
