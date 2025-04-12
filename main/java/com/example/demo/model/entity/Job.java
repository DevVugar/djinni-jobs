package com.example.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String jobTitle;
    @Column(columnDefinition = "TEXT")
    private String companyName;
    @Column(columnDefinition = "TEXT")
    private String location; // City, state, country, or remote/hybrid
    @Column(columnDefinition = "TEXT")
    private String jobType; // Full-time, Part-time, etc.

    @Column(columnDefinition = "TEXT")
    private Integer salaryMin;
    @Column(columnDefinition = "TEXT")
    private Integer salaryMax;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String experienceLevel; // Entry, Mid, etc.
    @Column(columnDefinition = "TEXT")
    private String educationLevel; // Bachelor's, Master's, etc.
    @Column(columnDefinition = "TEXT")
    private String industry; // Tech, Finance, etc.

    @Column(columnDefinition = "TEXT")
    private String postedDate;
    @Column(columnDefinition = "TEXT")
    private String applicationDeadline;


    @Column(columnDefinition = "TEXT")
    private String howToApply; // URL or instructions
    @Column(columnDefinition = "TEXT")
    private String companyLogo; // image URL (optional)


    @Column(columnDefinition = "TEXT")
    private String benefits; // Optional: "Health insurance, etc."

    @Column(columnDefinition = "TEXT")
    private String tags; // Comma-separated string like "Java,Remote,Startup"
    @Column(columnDefinition = "TEXT")
    private String source; // External source link
}