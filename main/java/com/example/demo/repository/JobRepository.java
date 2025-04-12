package com.example.demo.repository;

import com.example.demo.model.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;



@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    // The JpaSpecificationExecutor interface allows us to use Specifications
    // for complex filtering operations
}