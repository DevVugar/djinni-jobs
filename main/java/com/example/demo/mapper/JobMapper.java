package com.example.demo.mapper;


import com.example.demo.model.dto.JobDTO;
import com.example.demo.model.dto.PaginatedResponse;
import com.example.demo.model.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobMapper {

    public JobDTO toDto(Job job) {
        return JobDTO.builder()
                .id(job.getId())
                .jobTitle(job.getJobTitle())
                .companyName(job.getCompanyName())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .jobDescription(job.getJobDescription())
                .requirements(job.getRequirements())
                .experienceLevel(job.getExperienceLevel())
                .educationLevel(job.getEducationLevel())
                .industry(job.getIndustry())
                .postedDate(job.getPostedDate())
                .applicationDeadline(job.getApplicationDeadline())
                .howToApply(job.getHowToApply())
                .companyLogo(job.getCompanyLogo())
                .benefits(job.getBenefits())
                .tags(job.getTags())
                .source(job.getSource())
                .build();
    }

    public List<JobDTO> toDtoList(List<Job> jobs) {
        return jobs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Job toEntity(JobDTO jobDTO) {
        return Job.builder()
                .id(jobDTO.getId())
                .jobTitle(jobDTO.getJobTitle())
                .companyName(jobDTO.getCompanyName())
                .location(jobDTO.getLocation())
                .jobType(jobDTO.getJobType())
                .salaryMin(jobDTO.getSalaryMin())
                .salaryMax(jobDTO.getSalaryMax())
                .jobDescription(jobDTO.getJobDescription())
                .requirements(jobDTO.getRequirements())
                .experienceLevel(jobDTO.getExperienceLevel())
                .educationLevel(jobDTO.getEducationLevel())
                .industry(jobDTO.getIndustry())
                .postedDate(jobDTO.getPostedDate())
                .applicationDeadline(jobDTO.getApplicationDeadline())
                .howToApply(jobDTO.getHowToApply())
                .companyLogo(jobDTO.getCompanyLogo())
                .benefits(jobDTO.getBenefits())
                .tags(jobDTO.getTags())
                .source(jobDTO.getSource())
                .build();
    }

    public PaginatedResponse<JobDTO> toPaginatedResponse(Page<Job> jobPage) {
        List<JobDTO> jobDTOs = jobPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<JobDTO>builder()
                .content(jobDTOs)
                .pageNumber(jobPage.getNumber())
                .pageSize(jobPage.getSize())
                .totalElements(jobPage.getTotalElements())
                .totalPages(jobPage.getTotalPages())
                .last(jobPage.isLast())
                .build();
    }
}