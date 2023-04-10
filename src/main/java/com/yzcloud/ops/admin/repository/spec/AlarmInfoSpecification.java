package com.yzcloud.ops.admin.repository.spec;

import com.yzcloud.ops.admin.domain.AlarmInfo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

/**
 * To implement dynamic SQL queries, you can use JPA Criteria API
 */
public class AlarmInfoSpecification implements Specification<AlarmInfo> {

    private final Logger log = LoggerFactory.getLogger(AlarmInfoSpecification.class);

    private Long alarmRuleId;

    private Long eventRuleId;

    private Long alarmLevelId;

    private Long categoryId;

    private String alarmInfoKeywords;

    public AlarmInft oSpecification(Long alarmRuleId, Long eventRuleId, Long alarmLevelId, Long categoryId, String alarmInfoKeywords) {
        this.alarmRuleId = alarmRuleId;
        this.eventRuleId = eventRuleId;
        this.alarmLevelId = alarmLevelId;
        this.categoryId = categoryId;
        this.alarmInfoKeywords = alarmInfoKeywords;
    }

    @Override
    public Predicate toPredicate(Root<AlarmInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (alarmRuleId != null) {
            predicates.add(criteriaBuilder.equal(root.join("alarmRule").get("id"), alarmRuleId));
        }

        if (eventRuleId != null) {
            predicates.add(criteriaBuilder.equal(root.join("alarmRule").join("eventRule").get("id"), eventRuleId));
        }

        if (alarmLevelId != null) {
            predicates.add(criteriaBuilder.equal(root.join("alarmRule").join("alarmLevel").get("id"), alarmLevelId));
        }

        if (categoryId != null) {
            predicates.add(criteriaBuilder.equal(root.join("alarmRule").join("eventRule").join("category").get("id"), categoryId));
        }

        if (alarmInfoKeywords != null) {
            predicates.add(
                criteriaBuilder.like(
                    root.join("alarmRule").join("eventRule").join("category").join("alarmInfoKeywords").get("name"),
                    "%" + alarmInfoKeywords + "%"
                )
            );
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
