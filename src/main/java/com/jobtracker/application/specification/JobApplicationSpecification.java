package com.jobtracker.application.specification;

import com.jobtracker.application.dto.JobApplicationFilter;
import com.jobtracker.application.entity.JobApplication;
import org.springframework.data.jpa.domain.Specification;

public final class JobApplicationSpecification {

    private JobApplicationSpecification() {}

    public static Specification<JobApplication> withFilter(Long userId, JobApplicationFilter filter) {
        return Specification.where(hasUserId(userId))
                .and(hasStatus(filter))
                .and(matchesSearch(filter))
                .and(applicationDateFrom(filter))
                .and(applicationDateTo(filter))
                .and(matchesLocation(filter));
    }

    private static Specification<JobApplication> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    private static Specification<JobApplication> hasStatus(JobApplicationFilter filter) {
        return (root, query, cb) ->
                filter.status() == null ? null : cb.equal(root.get("status"), filter.status());
    }

    private static Specification<JobApplication> matchesSearch(JobApplicationFilter filter) {
        return (root, query, cb) -> {
            String raw = filter.search();
            if (raw == null || raw.isBlank()) return null;
            String pattern = "%" + raw.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("companyName")), pattern),
                    cb.like(cb.lower(root.get("position")), pattern)
            );
        };
    }

    private static Specification<JobApplication> applicationDateFrom(JobApplicationFilter filter) {
        return (root, query, cb) ->
                filter.startDate() == null ? null
                        : cb.greaterThanOrEqualTo(root.get("applicationDate"), filter.startDate());
    }

    private static Specification<JobApplication> applicationDateTo(JobApplicationFilter filter) {
        return (root, query, cb) ->
                filter.endDate() == null ? null
                        : cb.lessThanOrEqualTo(root.get("applicationDate"), filter.endDate());
    }

    private static Specification<JobApplication> matchesLocation(JobApplicationFilter filter) {
        return (root, query, cb) -> {
            String raw = filter.location();
            if (raw == null || raw.isBlank()) return null;
            String pattern = "%" + raw.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("location")), pattern);
        };
    }
}
