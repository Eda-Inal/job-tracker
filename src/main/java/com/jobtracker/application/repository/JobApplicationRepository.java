package com.jobtracker.application.repository;

import com.jobtracker.application.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    // Native queries — bypass @SQLRestriction to access soft-deleted records
    @Query(
        value = "SELECT * FROM job_applications WHERE user_id = :userId AND deleted = true ORDER BY deleted_at DESC",
        countQuery = "SELECT COUNT(*) FROM job_applications WHERE user_id = :userId AND deleted = true",
        nativeQuery = true
    )
    Page<JobApplication> findTrashedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
        value = "SELECT * FROM job_applications WHERE id = :id AND user_id = :userId AND deleted = true",
        nativeQuery = true
    )
    Optional<JobApplication> findTrashedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
