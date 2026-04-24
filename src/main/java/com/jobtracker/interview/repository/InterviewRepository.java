package com.jobtracker.interview.repository;

import com.jobtracker.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(Long appId, Long userId);

    Optional<Interview> findByIdAndJobApplicationUserId(Long id, Long userId);

    @Query("SELECT i FROM Interview i WHERE i.jobApplication.user.id = :userId " +
           "AND i.scheduledAt >= :from AND i.scheduledAt <= :to ORDER BY i.scheduledAt ASC")
    List<Interview> findUpcoming(@Param("userId") Long userId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);
}
