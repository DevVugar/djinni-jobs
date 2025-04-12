package com.example.demo.service;

import com.example.demo.mapper.JobMapper;
import com.example.demo.model.dto.JobDTO;
import com.example.demo.model.dto.JobFilterRequest;
import com.example.demo.model.dto.PaginatedResponse;
import com.example.demo.model.entity.Job;
import com.example.demo.repository.JobRepository;
import com.example.demo.scraper.DjinniJobScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService{

    private final JobRepository jobRepository;
    private final DjinniJobScraper djinniJobScraper;
    private final JobMapper jobMapper;



    @Transactional(readOnly = true)
    public PaginatedResponse<JobDTO> getAllJobs(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all jobs - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Job> jobPage = jobRepository.findAll(pageable);

        log.info("Found {} jobs in total", jobPage.getTotalElements());
        return jobMapper.toPaginatedResponse(jobPage);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<JobDTO> filterJobs(JobFilterRequest filterRequest) {
        log.info("Filtering jobs with request: {}", filterRequest);

        Sort.Direction direction = filterRequest.getSortDirection().equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable;
        Page<Job> jobPage;

        // Create the specification for filtering
        Specification<Job> spec = Specification.where(JobSpecifications.withLocation(filterRequest.getLocation()))
                .and(JobSpecifications.withJobType(filterRequest.getJobType()))
                .and(JobSpecifications.withExperienceLevel(filterRequest.getExperienceLevel()))
                .and(JobSpecifications.withIndustry(filterRequest.getIndustry()))
                .and(JobSpecifications.withTags(filterRequest.getTags()))
                .and(JobSpecifications.withSalaryRange(filterRequest.getMinSalary(), filterRequest.getMaxSalary()));

        // Special handling for sorting by salary range
        if (filterRequest.getSortBy().equalsIgnoreCase("salaryRange")) {
            // First get filtered results without sorting
            pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize());
            List<Job> filteredJobs = jobRepository.findAll(spec);

            // Sort by maxSalary, or minSalary if maxSalary is null
            filteredJobs.sort((job1, job2) -> {
                Integer salary1 = job1.getSalaryMax() != null ? job1.getSalaryMax() :
                        (job1.getSalaryMin() != null ? job1.getSalaryMin() : 0);
                Integer salary2 = job2.getSalaryMax() != null ? job2.getSalaryMin() :
                        (job2.getSalaryMin() != null ? job2.getSalaryMin() : 0);

                return direction == Sort.Direction.ASC ?
                        salary1.compareTo(salary2) : salary2.compareTo(salary1);
            });

            // Paginate the sorted results
            int start = filterRequest.getPage() * filterRequest.getSize();
            int end = Math.min((start + filterRequest.getSize()), filteredJobs.size());

            if (start > filteredJobs.size()) {
                jobPage = new PageImpl<>(java.util.Collections.emptyList(), pageable, filteredJobs.size());
            } else {
                jobPage = new PageImpl<>(
                        filteredJobs.subList(start, end),
                        pageable,
                        filteredJobs.size()
                );
            }
        } else {
            // For all other fields, use the normal sorting
            pageable = PageRequest.of(
                    filterRequest.getPage(),
                    filterRequest.getSize(),
                    Sort.by(direction, filterRequest.getSortBy())
            );
            jobPage = jobRepository.findAll(spec, pageable);
        }

        log.info("Filtered jobs count: {}", jobPage.getTotalElements());
        return jobMapper.toPaginatedResponse(jobPage);
    }

    @Transactional(readOnly = true)
    public JobDTO getJobById(Long id) {
        log.info("Getting job by ID: {}", id);
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Job not found with id: {}", id);
                    return new RuntimeException("Job not found with id: " + id);
                });
        log.info("Found job: {} (ID: {})", job.getJobTitle(), job.getId());
        return jobMapper.toDto(job);
    }

//    @Transactional
//    public List<JobDTO> scrapeAndSaveJobs() {
//        log.info("Starting job scraping process");
//        List<Job> scrapedJobs = djinniJobScraper.scrapeJobs();
//        log.info("Scraped {} jobs from Djinni", scrapedJobs.size());
//
//        if (!scrapedJobs.isEmpty()) {
//            log.info("Saving scraped jobs to database");
//            jobRepository.saveAll(scrapedJobs);
//            log.info("Successfully saved {} jobs", scrapedJobs.size());
//        } else {
//            log.info("No jobs were scraped");
//        }
//
//        return jobMapper.toDtoList(scrapedJobs);
//    }


    @Transactional
    public List<JobDTO> scrapeAndSaveJobs() {
        log.info("Starting job scraping process");
        List<Job> scrapedJobs = djinniJobScraper.main();
        log.info("Scraped {} jobs from Djinni", scrapedJobs.size());

        if (!scrapedJobs.isEmpty()) {
            log.info("Saving scraped jobs to database");
            jobRepository.saveAll(scrapedJobs);
            log.info("Successfully saved {} jobs", scrapedJobs.size());
        } else {
            log.info("No jobs were scraped");
        }

        return jobMapper.toDtoList(scrapedJobs);
    }
}