package com.example.demo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDTO {
    private Long id;
    private String jobTitle;
    private String companyName;
    private String location;
    private String jobType;
    private Integer salaryMin;
    private Integer salaryMax;
    private String jobDescription;
    private String requirements;
    private String experienceLevel;
    private String educationLevel;
    private String industry;
    private String postedDate;
    private String applicationDeadline;
    private String howToApply;
    private String companyLogo;
    private String benefits;
    private String tags;
    private String source;
}