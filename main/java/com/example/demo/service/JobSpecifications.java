package com.example.demo.service;

import com.example.demo.model.entity.Job;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class JobSpecifications {

    public static Specification<Job> withLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("location")),
                    "%" + location.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Job> withJobType(String jobType) {
        return (root, query, criteriaBuilder) -> {
            if (jobType == null || jobType.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("jobType"), jobType);
        };
    }

    public static Specification<Job> withExperienceLevel(String experienceLevel) {
        return (root, query, criteriaBuilder) -> {
            if (experienceLevel == null || experienceLevel.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("experienceLevel"), experienceLevel);
        };
    }

    public static Specification<Job> withIndustry(String industry) {
        return (root, query, criteriaBuilder) -> {
            if (industry == null || industry.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("industry")),
                    "%" + industry.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Job> withTags(List<String> tags) {
        return (root, query, criteriaBuilder) -> {
            if (tags == null || tags.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            for (String tag : tags) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("tags")),
                                "%" + tag.toLowerCase() + "%"
                        )
                );
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Job> withSalaryRange(Integer minSalary, Integer maxSalary) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salaryMax"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salaryMin"), maxSalary));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }}
